
package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.DoctorCreateDTO;
import com.doctor.appointment.model.DTO.DoctorDTO;
import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        DoctorDTO theDoctor = doctorService.findById(doctorId);

        if (theDoctor == null) {
            throw new RuntimeException("Doctor id not found: " + doctorId);
        }

        return theDoctor;
    }

    @PostMapping("/create")
    public DoctorDTO addDoctor(@RequestBody DoctorCreateDTO theDoctorCreateDTO) {
        Doctor savedDoctor = doctorService.save(theDoctorCreateDTO);
        return doctorService.findById(savedDoctor.getId());
    }

    @DeleteMapping("/{doctorId}")
    public String deleteDoctor(@PathVariable int doctorId) {
        DoctorDTO tempDoctor = doctorService.findById(doctorId);

        if (tempDoctor == null) {
            throw new RuntimeException("Doctor id not found: " + doctorId);
        }

        doctorService.deleteById(doctorId);

        return "Deleted doctor id: " + doctorId;
    }

    @PutMapping("/{doctorId}")
    public DoctorDTO updateDoctor(@PathVariable int doctorId, @RequestBody DoctorCreateDTO doctorCreateDTO) {
        return doctorService.update(doctorId, doctorCreateDTO);
    }
}