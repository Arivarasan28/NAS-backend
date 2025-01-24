package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;
import com.doctor.appointment.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/")
    public List<DoctorDTO> findAll() {
        return doctorService.findAll();
    }

    @GetMapping("/{doctorId}")
    public DoctorDTO getDoctor(@PathVariable int doctorId) {
        return doctorService.findById(doctorId);
    }

    @PostMapping("/create")
    public DoctorDTO addDoctor(@RequestPart("doctor") DoctorCreateDTO doctorCreateDTO,
                               @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        doctorCreateDTO.setProfilePicture(profilePicture);
        return doctorService.save(doctorCreateDTO);
    }

    @PutMapping("/{doctorId}")
    public DoctorDTO updateDoctor(@PathVariable int doctorId,
                                  @RequestPart("doctor") DoctorCreateDTO doctorCreateDTO,
                                  @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        doctorCreateDTO.setProfilePicture(profilePicture);
        return doctorService.update(doctorId, doctorCreateDTO);
    }

    @DeleteMapping("/{doctorId}")
    public String deleteDoctor(@PathVariable int doctorId) {
        doctorService.deleteById(doctorId);
        return "Deleted doctor id: " + doctorId;
    }
}
