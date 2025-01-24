package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;
import com.doctor.appointment.model.Doctor;

import java.util.List;

public interface DoctorService {
    List<DoctorDTO> findAll();

    DoctorDTO findById(int theId);

    Doctor save(DoctorCreateDTO theDoctorCreateDTO);

    void deleteById(int theId);

    DoctorDTO update(int doctorId, DoctorCreateDTO doctorCreateDTO);
}