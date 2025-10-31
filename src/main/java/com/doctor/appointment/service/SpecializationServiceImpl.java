package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.SpecializationCreateDTO;
import com.doctor.appointment.model.DTO.SpecializationDTO;
import com.doctor.appointment.model.Specialization;
import com.doctor.appointment.repository.SpecializationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final ModelMapper modelMapper;

    public SpecializationServiceImpl(SpecializationRepository specializationRepository, ModelMapper modelMapper) {
        this.specializationRepository = specializationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<SpecializationDTO> getAll() {
        return specializationRepository.findAll()
                .stream()
                .map(s -> modelMapper.map(s, SpecializationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public SpecializationDTO getById(Integer id) {
        Specialization s = specializationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found: " + id));
        return modelMapper.map(s, SpecializationDTO.class);
    }

    @Override
    @Transactional
    public SpecializationDTO create(SpecializationCreateDTO dto) {
        if (specializationRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Specialization already exists with name: " + dto.getName());
        }
        Specialization s = new Specialization();
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());
        s = specializationRepository.save(s);
        return modelMapper.map(s, SpecializationDTO.class);
    }

    @Override
    @Transactional
    public SpecializationDTO update(Integer id, SpecializationCreateDTO dto) {
        Specialization s = specializationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found: " + id));
        // prevent duplicate names on update
        specializationRepository.findByNameIgnoreCase(dto.getName())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> { throw new RuntimeException("Specialization already exists with name: " + dto.getName()); });
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());
        s = specializationRepository.save(s);
        return modelMapper.map(s, SpecializationDTO.class);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!specializationRepository.existsById(id)) {
            throw new RuntimeException("Specialization not found: " + id);
        }
        specializationRepository.deleteById(id);
    }
}
