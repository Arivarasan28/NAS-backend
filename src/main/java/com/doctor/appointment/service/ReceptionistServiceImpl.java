package com.doctor.appointment.service;

import com.doctor.appointment.exception.ResourceNotFoundException;
import com.doctor.appointment.model.DTO.ReceptionistDTO;
import com.doctor.appointment.model.Receptionist;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.ReceptionistRepository;
import com.doctor.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceptionistServiceImpl implements ReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ReceptionistDTO> findAll() {
        return receptionistRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReceptionistDTO findById(int id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist", "id", id));
        return toDTO(receptionist);
    }

    @Override
    public Receptionist save(Receptionist receptionist) {
        return receptionistRepository.save(receptionist);
    }

    @Override
    public void deleteById(int id) {
        receptionistRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ReceptionistDTO update(int id, Receptionist receptionist) {
        Receptionist existingReceptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist", "id", id));
        
        // Ensure receptionist has associated user
        if (existingReceptionist.getUser() == null) {
            throw new RuntimeException("Receptionist has no associated user account. Cannot update.");
        }
        
        User user = existingReceptionist.getUser();
        
        // Update user's common attributes if provided
        if (receptionist.getUser() != null) {
            if (receptionist.getUser().getName() != null) {
                user.setName(receptionist.getUser().getName());
            }
            if (receptionist.getUser().getEmail() != null) {
                user.setEmail(receptionist.getUser().getEmail());
            }
            if (receptionist.getUser().getPhone() != null) {
                user.setPhone(receptionist.getUser().getPhone());
            }
            userRepository.save(user);
        }
        
        // Update receptionist-specific attributes
        if (receptionist.getDepartment() != null) {
            existingReceptionist.setDepartment(receptionist.getDepartment());
        }
        
        Receptionist updatedReceptionist = receptionistRepository.save(existingReceptionist);
        return toDTO(updatedReceptionist);
    }

    @Override
    public Optional<Receptionist> findByUser(User user) {
        return receptionistRepository.findByUser(user);
    }

    @Override
    public Optional<Receptionist> findByEmail(String email) {
        return receptionistRepository.findByUserEmail(email); // Updated to use new method name
    }
    
    private ReceptionistDTO toDTO(Receptionist receptionist) {
        ReceptionistDTO dto = new ReceptionistDTO();
        dto.setId(receptionist.getId());
        
        // Map common attributes from User
        if (receptionist.getUser() != null) {
            dto.setName(receptionist.getUser().getName());
            dto.setEmail(receptionist.getUser().getEmail());
            dto.setPhone(receptionist.getUser().getPhone());
            dto.setProfilePictureUrl(receptionist.getUser().getProfilePictureUrl());
            dto.setUserId(receptionist.getUser().getId());
        }
        
        // Map receptionist-specific attributes
        dto.setDepartment(receptionist.getDepartment());
        
        return dto;
    }
}
