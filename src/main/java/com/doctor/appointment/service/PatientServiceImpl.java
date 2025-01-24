package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.PatientCreateDTO;
import com.doctor.appointment.model.DTO.PatientDTO;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.repository.PatientRepository;
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
    private ModelMapper modelMapper;

    @Override
    public List<PatientDTO> findAll() {
        return patientRepository.findAll().stream()
                .map(patient -> modelMapper.map(patient, PatientDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public PatientDTO findById(int theId) {
        Patient patient = patientRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + theId));
        return modelMapper.map(patient, PatientDTO.class);
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

        existingPatient.setName(patientCreateDTO.getName());
        existingPatient.setEmail(patientCreateDTO.getEmail());
        existingPatient.setPhone(patientCreateDTO.getPhone());
        existingPatient.setAddress(patientCreateDTO.getAddress());

        Patient updatedPatient = patientRepository.save(existingPatient);

        return modelMapper.map(updatedPatient, PatientDTO.class);
    }
}
