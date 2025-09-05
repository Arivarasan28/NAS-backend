package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.SpecializationCreateDTO;
import com.doctor.appointment.model.DTO.SpecializationDTO;

import java.util.List;

public interface SpecializationService {
    List<SpecializationDTO> getAll();
    SpecializationDTO getById(Integer id);
    SpecializationDTO create(SpecializationCreateDTO dto);
    SpecializationDTO update(Integer id, SpecializationCreateDTO dto);
    void delete(Integer id);
}
