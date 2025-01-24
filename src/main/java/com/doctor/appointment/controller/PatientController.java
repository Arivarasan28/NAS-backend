package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.PatientCreateDTO;
import com.doctor.appointment.model.DTO.PatientDTO;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/")
    public List<PatientDTO> findAll() {
        return patientService.findAll();
    }

    @GetMapping("/{patientId}")
    public PatientDTO getPatient(@PathVariable int patientId) {
        return patientService.findById(patientId);
    }

    @PostMapping("/create")
    public PatientDTO addPatient(@RequestBody PatientCreateDTO patientCreateDTO) {
        Patient savedPatient = patientService.save(patientCreateDTO);
        return patientService.findById(savedPatient.getId());
    }

    @DeleteMapping("/{patientId}")
    public String deletePatient(@PathVariable int patientId) {
        patientService.deleteById(patientId);
        return "Deleted patient id: " + patientId;
    }

    @PutMapping("/{patientId}")
    public PatientDTO updatePatient(@PathVariable int patientId, @RequestBody PatientCreateDTO patientCreateDTO) {
        return patientService.update(patientId, patientCreateDTO);
    }
}
