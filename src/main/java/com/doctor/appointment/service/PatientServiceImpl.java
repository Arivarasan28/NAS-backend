package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.PatientCreateDTO;
import com.doctor.appointment.model.DTO.PatientDTO;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.PatientRepository;
import com.doctor.appointment.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<PatientDTO> findAll() {
        return patientRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PatientDTO findById(int theId) {
        Patient patient = patientRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + theId));
        return toDTO(patient);
    }

    @Override
    public Patient save(PatientCreateDTO patientCreateDTO) {
        Patient patient = modelMapper.map(patientCreateDTO, Patient.class);
        return patientRepository.save(patient);
    }

    @Override
    public void deleteById(int theId) {
        patientRepository.deleteById(theId);
    }

    @Override
    @Transactional
    public PatientDTO update(int patientId, PatientCreateDTO patientCreateDTO) {
        Patient existingPatient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        // Ensure patient has associated user
        if (existingPatient.getUser() == null) {
            throw new RuntimeException("Patient has no associated user account. Cannot update.");
        }
        
        User user = existingPatient.getUser();
        
        // Update user's common attributes
        if (patientCreateDTO.getName() != null) {
            user.setName(patientCreateDTO.getName());
        }
        if (patientCreateDTO.getEmail() != null) {
            user.setEmail(patientCreateDTO.getEmail());
        }
        if (patientCreateDTO.getPhone() != null) {
            user.setPhone(patientCreateDTO.getPhone());
        }
        
        userRepository.save(user);
        
        // Update patient-specific attributes
        if (patientCreateDTO.getAddress() != null) {
            existingPatient.setAddress(patientCreateDTO.getAddress());
        }

        Patient updatedPatient = patientRepository.save(existingPatient);

        return toDTO(updatedPatient);
    }
    
    @Override
    public PatientDTO findByUserId(int userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found for user ID: " + userId));
        return toDTO(patient);
    }
    
    private PatientDTO toDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        
        // Map common attributes from User
        if (patient.getUser() != null) {
            dto.setName(patient.getUser().getName());
            dto.setEmail(patient.getUser().getEmail());
            dto.setPhone(patient.getUser().getPhone());
            dto.setProfilePictureUrl(patient.getUser().getProfilePictureUrl());
        }
        
        // Map patient-specific attributes
        dto.setAddress(patient.getAddress());
        
        return dto;
    }
}
