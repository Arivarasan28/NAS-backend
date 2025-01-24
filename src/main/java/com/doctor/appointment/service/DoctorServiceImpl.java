package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;
import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.repository.DoctorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<DoctorDTO> findAll() {
        return doctorRepository.findAll().stream()
                .map(doctor -> modelMapper.map(doctor, DoctorDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDTO findById(int theId) {
        Doctor doctor = doctorRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + theId));
        return modelMapper.map(doctor, DoctorDTO.class);
    }

    @Override
    public Doctor save(DoctorCreateDTO theDoctorCreateDTO) {
        Doctor doctor = modelMapper.map(theDoctorCreateDTO, Doctor.class);
        return doctorRepository.save(doctor);
    }

    @Override
    public void deleteById(int theId) {
        doctorRepository.deleteById(theId);
    }

    @Override
    @Transactional
    public DoctorDTO update(int doctorId, DoctorCreateDTO doctorCreateDTO) {
        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

        existingDoctor.setName(doctorCreateDTO.getName());
        existingDoctor.setSpecialization(doctorCreateDTO.getSpecialization());
        existingDoctor.setEmail(doctorCreateDTO.getEmail());
        existingDoctor.setPhone(doctorCreateDTO.getPhone());

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);

        return modelMapper.map(updatedDoctor, DoctorDTO.class);
    }
}
