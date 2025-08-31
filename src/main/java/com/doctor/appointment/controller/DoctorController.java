package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;
import com.doctor.appointment.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/")
    @PreAuthorize("permitAll()")
    public List<DoctorDTO> findAll() {
        return doctorService.findAll();
    }

    @GetMapping("/{doctorId}")
    @PreAuthorize("permitAll()")
    public DoctorDTO getDoctor(@PathVariable int doctorId) {
        return doctorService.findById(doctorId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("permitAll()")
    public Object getDoctorByUserId(@PathVariable int userId) {
        try {
            DoctorDTO doctor = doctorService.findByUserId(userId);
            return doctor;
        } catch (Exception e) {
            return Map.of(
                "message", "No doctor profile found for user ID: " + userId,
                "error", e.getClass().getName()
            );
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public DoctorDTO addDoctor(@RequestPart("doctor") DoctorCreateDTO doctorCreateDTO,
                               @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        doctorCreateDTO.setProfilePicture(profilePicture);
        return doctorService.save(doctorCreateDTO);
    }

    @PutMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST') or @doctorSecurity.isDoctor(#doctorId)")
    public DoctorDTO updateDoctor(@PathVariable int doctorId,
                                  @RequestPart("doctor") DoctorCreateDTO doctorCreateDTO,
                                  @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        doctorCreateDTO.setProfilePicture(profilePicture);
        return doctorService.update(doctorId, doctorCreateDTO);
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteDoctor(@PathVariable int doctorId) {
        doctorService.deleteById(doctorId);
        return "Deleted doctor id: " + doctorId;
    }
}
