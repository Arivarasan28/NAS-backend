package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.PatientCreateDTO;
import com.doctor.appointment.model.DTO.PatientDTO;
import com.doctor.appointment.model.Patient;

import java.util.List;

public interface PatientService {
    List<PatientDTO> findAll();

    PatientDTO findById(int theId);

    Patient save(PatientCreateDTO patientCreateDTO);

    void deleteById(int theId);

    PatientDTO update(int patientId, PatientCreateDTO patientCreateDTO);
}
