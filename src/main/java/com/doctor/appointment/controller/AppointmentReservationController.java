package com.doctor.appointment.controller;

import com.doctor.appointment.service.AppointmentReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for managing temporary appointment slot reservations
 * Prevents race conditions during payment process
 */
@RestController
@RequestMapping("/api/appointments/reservations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class AppointmentReservationController {

    @Autowired
    private AppointmentReservationService reservationService;

    /**
     * Reserve a slot temporarily for payment process
     * POST /api/appointments/reservations/reserve/{appointmentId}/patient/{patientId}
     */
    @PostMapping("/reserve/{appointmentId}/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> reserveSlot(
            @PathVariable int appointmentId,
            @PathVariable int patientId) {
        
        boolean reserved = reservationService.reserveSlot(appointmentId, patientId);
        
        Map<String, Object> response = new HashMap<>();
        
        if (reserved) {
            response.put("success", true);
            response.put("message", "Slot reserved successfully. Complete payment within 5 minutes.");
            response.put("appointmentId", appointmentId);
            response.put("reservationDurationMinutes", 5);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "This slot is currently reserved by another user or already booked. Please select another slot.");
            response.put("error", "SLOT_UNAVAILABLE");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    /**
     * Release a reservation (when payment is cancelled)
     * DELETE /api/appointments/reservations/release/{appointmentId}/patient/{patientId}
     */
    @DeleteMapping("/release/{appointmentId}/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> releaseReservation(
            @PathVariable int appointmentId,
            @PathVariable int patientId) {
        
        try {
            reservationService.releaseReservation(appointmentId, patientId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reservation released successfully.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // If appointment was already booked or deleted, consider it a success
            // since the goal (slot no longer reserved) is achieved
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reservation already released or slot was booked.");
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Check if a slot is available
     * GET /api/appointments/reservations/check/{appointmentId}/patient/{patientId}
     */
    @GetMapping("/check/{appointmentId}/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> checkSlotAvailability(
            @PathVariable int appointmentId,
            @PathVariable int patientId) {
        
        boolean available = reservationService.isSlotAvailable(appointmentId, patientId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", available);
        response.put("appointmentId", appointmentId);
        
        if (available) {
            response.put("message", "Slot is available for booking.");
        } else {
            response.put("message", "Slot is not available. It may be booked or reserved by another user.");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cleanup expired reservations (admin endpoint)
     * POST /api/appointments/reservations/cleanup
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupExpiredReservations() {
        reservationService.cleanupExpiredReservations();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Expired reservations cleaned up successfully.");
        
        return ResponseEntity.ok(response);
    }
}
