package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;

import java.util.List;

public interface DoctorService {
    List<DoctorDTO> findAll();
    DoctorDTO findById(int id);
    DoctorDTO save(DoctorCreateDTO doctorCreateDTO);
    void deleteById(int id);
    DoctorDTO update(int doctorId, DoctorCreateDTO doctorCreateDTO);
}
