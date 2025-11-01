package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.SpecializationCreateDTO;
import com.doctor.appointment.model.DTO.SpecializationDTO;
import com.doctor.appointment.service.SpecializationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/specializations")
public class SpecializationController {

    private final SpecializationService specializationService;

    public SpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SpecializationDTO>> getAll() {
        return ResponseEntity.ok(specializationService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SpecializationDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(specializationService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SpecializationDTO> create(@RequestBody SpecializationCreateDTO dto) {
        return ResponseEntity.ok(specializationService.create(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SpecializationDTO> update(@PathVariable Integer id, @RequestBody SpecializationCreateDTO dto) {
        return ResponseEntity.ok(specializationService.update(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        specializationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
