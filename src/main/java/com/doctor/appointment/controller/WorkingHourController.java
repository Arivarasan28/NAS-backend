package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.WorkingHourCreateDTO;
import com.doctor.appointment.model.DTO.WorkingHourDTO;
import com.doctor.appointment.service.WorkingHourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/doctor/{doctorId}/working-hours")
public class WorkingHourController {

    @Autowired
    private WorkingHourService workingHourService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<WorkingHourDTO> list(@PathVariable int doctorId) {
        return workingHourService.list(doctorId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST') or @doctorSecurity.isDoctor(#doctorId)")
    public WorkingHourDTO create(@PathVariable int doctorId, @RequestBody WorkingHourCreateDTO dto) {
        return workingHourService.create(doctorId, dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST') or @doctorSecurity.isDoctor(#doctorId)")
    public WorkingHourDTO update(@PathVariable int doctorId, @PathVariable Long id, @RequestBody WorkingHourCreateDTO dto) {
        return workingHourService.update(doctorId, id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST') or @doctorSecurity.isDoctor(#doctorId)")
    public void delete(@PathVariable int doctorId, @PathVariable Long id) {
        workingHourService.delete(doctorId, id);
    }
}
