package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentStatusUpdateDTO;
import com.doctor.appointment.model.DTO.AppointmentSlotCreateDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "APIs for managing appointments")
public class AppointmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

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
        AppointmentDTO savedAppointment = appointmentService.save(appointmentCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedAppointment);
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
    
    @Operation(summary = "Get appointments by doctor ID", description = "Returns all appointments for a specific doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointments"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAppointmentsByDoctor(
            @Parameter(description = "ID of the doctor") 
            @PathVariable int doctorId) {
        try {
            List<AppointmentDTO> appointments = appointmentService.findByDoctorId(doctorId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "message", "Error fetching appointments: " + e.getMessage(),
                        "error", e.getClass().getName()
                    ));
        }
    }
    
    @Operation(summary = "Get appointments by doctor ID and date", description = "Returns all appointments for a specific doctor on a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointments"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/doctor/{doctorId}/date/{date}")
    public ResponseEntity<?> getAppointmentsByDoctorAndDate(
            @Parameter(description = "ID of the doctor") 
            @PathVariable int doctorId,
            @Parameter(description = "Date in format YYYY-MM-DD") 
            @PathVariable String date) {
        try {
            List<AppointmentDTO> appointments = appointmentService.findByDoctorIdAndDate(doctorId, date);
            // Always return OK with the appointments list, even if empty
            // The frontend will handle the empty case appropriately
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "message", "Error fetching appointments: " + e.getMessage(),
                        "error", e.getClass().getName()
                    ));
        }
    }

    // Alias 1: Return all slots (all statuses) for a doctor and date under a 'slots' path
    @Operation(summary = "Get all slots for a doctor and date",
            description = "Returns all appointment slots (any status) for a specific doctor on a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved slots"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/slots/doctor/{doctorId}/date/{date}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllSlotsByDoctorAndDateAlias1(
            @PathVariable int doctorId,
            @PathVariable String date) {
        try {
            List<AppointmentDTO> appointments = appointmentService.findByDoctorIdAndDate(doctorId, date);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error fetching slots: " + e.getMessage(),
                            "error", e.getClass().getName()
                    ));
        }
    }

    // Alias 2: Alternate URL shape ending with '/slots'
    @Operation(summary = "Get all slots for a doctor and date (alt)",
            description = "Returns all appointment slots (any status) for a specific doctor on a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved slots"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/doctor/{doctorId}/date/{date}/slots")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllSlotsByDoctorAndDateAlias2(
            @PathVariable int doctorId,
            @PathVariable String date) {
        try {
            List<AppointmentDTO> appointments = appointmentService.findByDoctorIdAndDate(doctorId, date);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error fetching slots: " + e.getMessage(),
                            "error", e.getClass().getName()
                    ));
        }
    }
    
    @Operation(summary = "Update appointment status", description = "Updates the status of an appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Appointment not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @Parameter(description = "ID of the appointment to update") 
            @PathVariable int appointmentId,
            @Parameter(description = "Updated status", required = true)
            @Valid @RequestBody AppointmentStatusUpdateDTO statusUpdateDTO) {
        return ResponseEntity.ok(appointmentService.updateStatus(appointmentId, statusUpdateDTO.getStatus()));
    }
    
    @Operation(summary = "Create appointment slots", description = "Creates multiple appointment slots for a doctor based on start time, end time, and duration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment slots created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/slots")
    public ResponseEntity<?> createAppointmentSlots(
            @Parameter(description = "Slot creation details", required = true,
                    content = @Content(schema = @Schema(implementation = AppointmentSlotCreateDTO.class)))
            @Valid @RequestBody AppointmentSlotCreateDTO slotCreateDTO) {
        try {
            logger.info("Creating appointment slots for doctor ID: {}", slotCreateDTO.getDoctorId());
            List<AppointmentDTO> createdSlots = appointmentService.createAppointmentSlots(slotCreateDTO);
            logger.info("Successfully created {} appointment slots for doctor ID: {}", 
                    createdSlots.size(), slotCreateDTO.getDoctorId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSlots);
        } catch (DateTimeParseException e) {
            logger.error("Date/time parsing error while creating appointment slots: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "message", "Invalid date or time format: " + e.getMessage(),
                        "error", "DateTimeParseException"
                    ));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument while creating appointment slots: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "message", "Invalid input: " + e.getMessage(),
                        "error", "IllegalArgumentException"
                    ));
        } catch (Exception e) {
            logger.error("Error creating appointment slots for doctor ID: {}", slotCreateDTO.getDoctorId(), e);
            if (e.getMessage() != null && e.getMessage().contains("Doctor not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                            "message", "Failed to create appointment slots: " + e.getMessage(),
                            "error", e.getClass().getSimpleName()
                        ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                            "message", "Failed to create appointment slots: " + e.getMessage(),
                            "error", e.getClass().getSimpleName()
                        ));
            }
        }
    }

    @Operation(summary = "Delete available appointment slot", description = "Deletes an available appointment slot that hasn't been booked yet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Slot deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found or not available"),
            @ApiResponse(responseCode = "403", description = "Forbidden - slot belongs to another doctor or is already booked")
    })
    @DeleteMapping("/slots/{appointmentId}/doctor/{doctorId}")
    public ResponseEntity<?> deleteAvailableSlot(
            @PathVariable int appointmentId,
            @PathVariable int doctorId) {
        
        try {
            appointmentService.deleteAvailableSlot(appointmentId, doctorId);
            return ResponseEntity.ok(Map.of("message", "Appointment slot deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @Operation(summary = "Get available appointment slots for a specific doctor and date", description = "Returns all available appointment slots for a specific doctor on a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved available slots"),
            @ApiResponse(responseCode = "404", description = "Doctor not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/slots/available/doctor/{doctorId}/date/{date}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAvailableSlotsByDoctorAndDate(
            @PathVariable int doctorId,
            @PathVariable String date) {
        
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<AppointmentDTO> availableSlots = appointmentService.getAvailableSlotsByDoctorAndDate(doctorId, localDate);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "message", "Error fetching available slots: " + e.getMessage(),
                        "error", e.getClass().getName(),
                        "stackTrace", e.getStackTrace()[0].toString()
                    ));
        }
    }
    
    @Operation(summary = "Book an appointment slot", description = "Books an available appointment slot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment slot booked successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found or not available"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getAppointmentsByPatient(
            @Parameter(description = "ID of the patient") 
            @PathVariable int patientId) {
        
        try {
            List<AppointmentDTO> appointments = appointmentService.findByPatientId(patientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "message", "Error fetching appointments: " + e.getMessage(),
                        "error", e.getClass().getName()
                    ));
        }
    }
    
    @PostMapping("/book/{appointmentId}/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> bookAppointment(
            @PathVariable int appointmentId,
            @PathVariable int patientId) {
        
        try {
            AppointmentDTO bookedAppointment = appointmentService.bookAppointment(appointmentId, patientId);
            return ResponseEntity.ok(bookedAppointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
