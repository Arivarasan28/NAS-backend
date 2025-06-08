package com.doctor.appointment.service;

import com.doctor.appointment.exception.ResourceNotFoundException;
import com.doctor.appointment.model.DTO.ReceptionistDTO;
import com.doctor.appointment.model.Receptionist;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.ReceptionistRepository;
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
    private final ModelMapper modelMapper;

    @Override
    public List<ReceptionistDTO> findAll() {
        return receptionistRepository.findAll().stream()
                .map(receptionist -> modelMapper.map(receptionist, ReceptionistDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ReceptionistDTO findById(int id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist", "id", id));
        return modelMapper.map(receptionist, ReceptionistDTO.class);
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
        
        existingReceptionist.setName(receptionist.getName());
        existingReceptionist.setEmail(receptionist.getEmail());
        existingReceptionist.setPhone(receptionist.getPhone());
        existingReceptionist.setDepartment(receptionist.getDepartment());
        
        Receptionist updatedReceptionist = receptionistRepository.save(existingReceptionist);
        return modelMapper.map(updatedReceptionist, ReceptionistDTO.class);
    }

    @Override
    public Optional<Receptionist> findByUser(User user) {
        return receptionistRepository.findByUser(user);
    }

    @Override
    public Optional<Receptionist> findByEmail(String email) {
        return receptionistRepository.findByEmail(email);
    }
}
