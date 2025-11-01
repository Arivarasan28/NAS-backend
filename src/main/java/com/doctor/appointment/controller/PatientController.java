package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.PatientCreateDTO;
import com.doctor.appointment.model.DTO.PatientDTO;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.model.User;
import com.doctor.appointment.service.PatientService;
import com.doctor.appointment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public List<PatientDTO> findAll() {
        return patientService.findAll();
    }

    @GetMapping("/me")
    public PatientDTO getCurrentPatient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Get user by username
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        
        // Get patient by user ID
        return patientService.findByUserId(userOpt.get().getId());
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
    
    @GetMapping("/user/{userId}")
    public PatientDTO getPatientByUserId(@PathVariable int userId) {
        return patientService.findByUserId(userId);
    }

    @PutMapping("/{patientId}")
    public PatientDTO updatePatient(@PathVariable int patientId, @RequestBody PatientCreateDTO patientCreateDTO) {
        return patientService.update(patientId, patientCreateDTO);
    }
}
