package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;
import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.repository.DoctorRepository;
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
    private ModelMapper modelMapper;

    @Override
    public List<DoctorDTO> findAll() {
        return doctorRepository.findAll().stream()
                .map(doctor -> modelMapper.map(doctor, DoctorDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDTO findById(int id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));
        return modelMapper.map(doctor, DoctorDTO.class);
    }

    @Override
    public DoctorDTO save(DoctorCreateDTO doctorCreateDTO) {
        Doctor doctor = modelMapper.map(doctorCreateDTO, Doctor.class);

        if (doctorCreateDTO.getProfilePicture() != null && !doctorCreateDTO.getProfilePicture().isEmpty()) {
            String fileName = saveProfilePicture(doctorCreateDTO.getProfilePicture());
            doctor.setProfilePictureName(fileName);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        return modelMapper.map(savedDoctor, DoctorDTO.class);
    }

    @Override
    public void deleteById(int id) {
        doctorRepository.deleteById(id);
    }

    @Override
    @Transactional
    public DoctorDTO update(int doctorId, DoctorCreateDTO doctorCreateDTO) {
        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

        existingDoctor.setName(doctorCreateDTO.getName());
        existingDoctor.setSpecialization(doctorCreateDTO.getSpecialization());
        existingDoctor.setEmail(doctorCreateDTO.getEmail());
        existingDoctor.setPhone(doctorCreateDTO.getPhone());

        if (doctorCreateDTO.getProfilePicture() != null && !doctorCreateDTO.getProfilePicture().isEmpty()) {
            String fileName = saveProfilePicture(doctorCreateDTO.getProfilePicture());
            existingDoctor.setProfilePictureName(fileName);
        }

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return modelMapper.map(updatedDoctor, DoctorDTO.class);
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
}
