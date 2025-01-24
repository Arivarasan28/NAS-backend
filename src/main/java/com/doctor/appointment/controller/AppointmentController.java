package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.Appointment;
import com.doctor.appointment.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/")
    public List<AppointmentDTO> findAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{appointmentId}")
    public AppointmentDTO getAppointment(@PathVariable int appointmentId) {
        return appointmentService.findById(appointmentId);
    }

    @PostMapping("/create")
    public AppointmentDTO addAppointment(@RequestBody AppointmentCreateDTO appointmentCreateDTO) {
        Appointment savedAppointment = appointmentService.save(appointmentCreateDTO);
        return appointmentService.findById(savedAppointment.getId());
    }

    @DeleteMapping("/{appointmentId}")
    public String deleteAppointment(@PathVariable int appointmentId) {
        appointmentService.deleteById(appointmentId);
        return "Deleted appointment id: " + appointmentId;
    }

    @PutMapping("/{appointmentId}")
    public AppointmentDTO updateAppointment(@PathVariable int appointmentId, @RequestBody AppointmentCreateDTO appointmentCreateDTO) {
        return appointmentService.update(appointmentId, appointmentCreateDTO);
    }
}
