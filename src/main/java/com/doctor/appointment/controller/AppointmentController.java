package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.Appointment;
import com.doctor.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "APIs for managing appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Get all appointments", description = "Returns a list of all appointments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointments"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> findAll() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @Operation(summary = "Get appointment by ID", description = "Returns a specific appointment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointment"),
            @ApiResponse(responseCode = "404", description = "Appointment not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDTO> getAppointment(
            @Parameter(description = "ID of the appointment to retrieve") 
            @PathVariable int appointmentId) {
        return ResponseEntity.ok(appointmentService.findById(appointmentId));
    }

    @Operation(summary = "Create a new appointment", description = "Creates a new appointment and returns the created appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<AppointmentDTO> addAppointment(
            @Parameter(description = "Appointment details", required = true,
                    content = @Content(schema = @Schema(implementation = AppointmentCreateDTO.class)))
            @Valid @RequestBody AppointmentCreateDTO appointmentCreateDTO) {
        Appointment savedAppointment = appointmentService.save(appointmentCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.findById(savedAppointment.getId()));
    }

    @Operation(summary = "Delete an appointment", description = "Deletes an appointment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Appointment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Appointment not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(
            @Parameter(description = "ID of the appointment to delete") 
            @PathVariable int appointmentId) {
        appointmentService.deleteById(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update an appointment", description = "Updates an existing appointment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Appointment not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @Parameter(description = "ID of the appointment to update") 
            @PathVariable int appointmentId,
            @Parameter(description = "Updated appointment details", required = true,
                    content = @Content(schema = @Schema(implementation = AppointmentCreateDTO.class)))
            @Valid @RequestBody AppointmentCreateDTO appointmentCreateDTO) {
        return ResponseEntity.ok(appointmentService.update(appointmentId, appointmentCreateDTO));
    }
}
