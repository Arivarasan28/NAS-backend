package com.doctor.appointment.service;

import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.User;
import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;
import com.doctor.appointment.repository.DoctorRepository;
import com.doctor.appointment.repository.UserRepository;
import com.doctor.appointment.model.Specialization;
import com.doctor.appointment.repository.SpecializationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private static final String UPLOAD_DIRECTORY = "uploads/profile_pictures/";

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Override
    public List<DoctorDTO> findAll() {
        return doctorRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDTO findById(int id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));
        return toDTO(doctor);
    }

    @Override
    public DoctorDTO save(DoctorCreateDTO doctorCreateDTO) {
        Doctor doctor = modelMapper.map(doctorCreateDTO, Doctor.class);

        // Map specializations (many-to-many) if provided
        if (doctorCreateDTO.getSpecializationNames() != null && !doctorCreateDTO.getSpecializationNames().isEmpty()) {
            List<Specialization> specs = doctorCreateDTO.getSpecializationNames().stream()
                    .map(name -> specializationRepository.findByNameIgnoreCase(name)
                            .orElseThrow(() -> new RuntimeException("Specialization not found: " + name)))
                    .collect(Collectors.toList());
            doctor.getSpecializations().clear();
            doctor.getSpecializations().addAll(specs);
            // Keep legacy field for compatibility (first selected)
            doctor.setSpecialization(doctorCreateDTO.getSpecializationNames().get(0));
        }

        // Link to existing user account by username (required)
        if (doctorCreateDTO.getUsername() == null || doctorCreateDTO.getUsername().isBlank()) {
            throw new RuntimeException("Username is required to link doctor to user account");
        }
        User linkedUser = userRepository.findByUsername(doctorCreateDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + doctorCreateDTO.getUsername()));
        doctor.setUser(linkedUser);
        
        // Update user's common attributes from DTO
        if (doctorCreateDTO.getName() != null) {
            linkedUser.setName(doctorCreateDTO.getName());
        }
        if (doctorCreateDTO.getEmail() != null) {
            linkedUser.setEmail(doctorCreateDTO.getEmail());
        }
        if (doctorCreateDTO.getPhone() != null) {
            linkedUser.setPhone(doctorCreateDTO.getPhone());
        }
        
        // Handle profile picture upload
        if (doctorCreateDTO.getProfilePicture() != null && !doctorCreateDTO.getProfilePicture().isEmpty()) {
            String fileName = saveProfilePicture(doctorCreateDTO.getProfilePicture());
            linkedUser.setProfilePictureUrl("/uploads/profile_pictures/" + fileName);
        }
        
        userRepository.save(linkedUser);

        // Default appointment duration to 15 minutes if not provided
        if (doctor.getAppointmentDurationMinutes() == null) {
            doctor.setAppointmentDurationMinutes(15);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        return toDTO(savedDoctor);
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));

        User linkedUser = doctor.getUser();

        // Delete doctor first to release FK (doctors.user_id)
        doctorRepository.delete(doctor);

        // Then delete associated user (if any)
        if (linkedUser != null) {
            userRepository.delete(linkedUser);
        }
    }

    @Override
    @Transactional
    public DoctorDTO update(int doctorId, DoctorCreateDTO doctorCreateDTO) {
        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

        // Ensure doctor has associated user
        if (existingDoctor.getUser() == null) {
            throw new RuntimeException("Doctor has no associated user account. Cannot update.");
        }
        
        User user = existingDoctor.getUser();
        
        // Update user's common attributes
        if (doctorCreateDTO.getName() != null) {
            user.setName(doctorCreateDTO.getName());
        }
        if (doctorCreateDTO.getEmail() != null) {
            user.setEmail(doctorCreateDTO.getEmail());
        }
        if (doctorCreateDTO.getPhone() != null) {
            user.setPhone(doctorCreateDTO.getPhone());
        }
        
        // Handle profile picture if provided
        if (doctorCreateDTO.getProfilePicture() != null && !doctorCreateDTO.getProfilePicture().isEmpty()) {
            String fileName = saveProfilePicture(doctorCreateDTO.getProfilePicture());
            user.setProfilePictureUrl("/uploads/profile_pictures/" + fileName);
        }
        
        userRepository.save(user);
        
        // Update doctor-specific fields
        // If list provided, map many-to-many and keep legacy field in sync; else use single string
        if (doctorCreateDTO.getSpecializationNames() != null && !doctorCreateDTO.getSpecializationNames().isEmpty()) {
            List<Specialization> specs = doctorCreateDTO.getSpecializationNames().stream()
                    .map(name -> specializationRepository.findByNameIgnoreCase(name)
                            .orElseThrow(() -> new RuntimeException("Specialization not found: " + name)))
                    .collect(Collectors.toList());
            existingDoctor.getSpecializations().clear();
            existingDoctor.getSpecializations().addAll(specs);
            existingDoctor.setSpecialization(doctorCreateDTO.getSpecializationNames().get(0));
        } else if (doctorCreateDTO.getSpecialization() != null) {
            existingDoctor.setSpecialization(doctorCreateDTO.getSpecialization());
        }
        
        if (doctorCreateDTO.getFee() != null) {
            existingDoctor.setFee(doctorCreateDTO.getFee());
        }

        // Map appointment duration if provided
        if (doctorCreateDTO.getAppointmentDurationMinutes() != null) {
            existingDoctor.setAppointmentDurationMinutes(doctorCreateDTO.getAppointmentDurationMinutes());
        }

        // Optionally re-link to a different user by username (admin action)
        if (doctorCreateDTO.getUsername() != null && !doctorCreateDTO.getUsername().isBlank() 
                && !doctorCreateDTO.getUsername().equals(user.getUsername())) {
            User newLinkedUser = userRepository.findByUsername(doctorCreateDTO.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + doctorCreateDTO.getUsername()));
            existingDoctor.setUser(newLinkedUser);
        }

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return toDTO(updatedDoctor);
    }

    @Override
    public DoctorDTO findByUserId(int userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Find the doctor associated with this user
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No doctor profile found for user ID: " + userId));
        
        return modelMapper.map(doctor, DoctorDTO.class);
    }
    
    private String saveProfilePicture(MultipartFile profilePicture) {
        try {
            // Ensure the upload directory exists
            Path directoryPath = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Generate a unique file name
            String fileName = System.currentTimeMillis() + "_" + profilePicture.getOriginalFilename();
            Path filePath = directoryPath.resolve(fileName);

            // Save the file to the server
            Files.write(filePath, profilePicture.getBytes());

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile picture", e);
        }
    }

    private DoctorDTO toDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        
        // Map common attributes from User
        if (doctor.getUser() != null) {
            dto.setName(doctor.getUser().getName());
            dto.setEmail(doctor.getUser().getEmail());
            dto.setPhone(doctor.getUser().getPhone());
            dto.setProfilePictureUrl(doctor.getUser().getProfilePictureUrl());
        }
        
        // Map doctor-specific attributes
        dto.setSpecialization(doctor.getSpecialization());
        dto.setFee(doctor.getFee());
        dto.setAppointmentDurationMinutes(doctor.getAppointmentDurationMinutes());
        
        // Map specializations list
        if (doctor.getSpecializations() != null && !doctor.getSpecializations().isEmpty()) {
            dto.setSpecializations(
                doctor.getSpecializations().stream()
                        .map(Specialization::getName)
                        .collect(Collectors.toList())
            );
        }
        
        return dto;
    }
}
