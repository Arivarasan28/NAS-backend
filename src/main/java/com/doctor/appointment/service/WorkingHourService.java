package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.WorkingHourCreateDTO;
import com.doctor.appointment.model.DTO.WorkingHourDTO;

import java.util.List;

public interface WorkingHourService {
    List<WorkingHourDTO> list(int doctorId);
    WorkingHourDTO create(int doctorId, WorkingHourCreateDTO dto);
    WorkingHourDTO update(int doctorId, Long id, WorkingHourCreateDTO dto);
    void delete(int doctorId, Long id);
}
