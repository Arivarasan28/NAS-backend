package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.BookingResponseDTO;
import com.doctor.appointment.model.DTO.BookingWithPaymentDTO;
import com.doctor.appointment.model.DTO.PaymentCreateDTO;
import com.doctor.appointment.model.DTO.PaymentDTO;
import com.doctor.appointment.service.PaymentService;
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

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing payments and bookings with payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    @Operation(summary = "Book appointment with payment", 
               description = "Books an appointment and processes payment in a single atomic transaction. Prevents double bookings using optimistic locking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment booked and payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or slot not available"),
            @ApiResponse(responseCode = "409", description = "Slot was booked by another user (race condition)"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/book-with-payment")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> bookAppointmentWithPayment(
            @Parameter(description = "Booking and payment details", required = true,
                    content = @Content(schema = @Schema(implementation = BookingWithPaymentDTO.class)))
            @Valid @RequestBody BookingWithPaymentDTO bookingWithPaymentDTO) {
        
        try {
            logger.info("Processing booking with payment for appointment ID: {} and patient ID: {}", 
                       bookingWithPaymentDTO.getAppointmentId(), bookingWithPaymentDTO.getPatientId());
            
            BookingResponseDTO response = paymentService.bookAppointmentWithPayment(bookingWithPaymentDTO);
            
            logger.info("Successfully processed booking with payment for appointment ID: {}", 
                       bookingWithPaymentDTO.getAppointmentId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Error processing booking with payment: {}", e.getMessage());
            
            // Check if it's a double booking error
            if (e.getMessage().contains("just booked by another user") || 
                e.getMessage().contains("not available")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                            "message", e.getMessage(),
                            "error", "SLOT_UNAVAILABLE"
                        ));
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "message", e.getMessage(),
                        "error", "BOOKING_FAILED"
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error processing booking with payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "message", "An unexpected error occurred while processing your booking",
                        "error", "INTERNAL_ERROR"
                    ));
        }
    }
    
    @Operation(summary = "Process payment for an existing appointment", 
               description = "Creates a payment record for an already booked appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or payment already exists"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> processPayment(
            @Parameter(description = "Payment details", required = true,
                    content = @Content(schema = @Schema(implementation = PaymentCreateDTO.class)))
            @Valid @RequestBody PaymentCreateDTO paymentCreateDTO) {
        
        try {
            PaymentDTO payment = paymentService.processPayment(paymentCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (RuntimeException e) {
            logger.error("Error processing payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    
    @Operation(summary = "Get payment by ID", description = "Returns payment details by payment ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payment"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentById(@PathVariable int paymentId) {
        try {
            PaymentDTO payment = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    
    @Operation(summary = "Get payment by appointment ID", description = "Returns payment details for a specific appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payment"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentByAppointmentId(@PathVariable int appointmentId) {
        try {
            PaymentDTO payment = paymentService.getPaymentByAppointmentId(appointmentId);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    
    @Operation(summary = "Get payments by patient ID", description = "Returns all payments for a specific patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payments")
    })
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByPatientId(@PathVariable int patientId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByPatientId(patientId);
        return ResponseEntity.ok(payments);
    }
    
    @Operation(summary = "Get payments by doctor ID", description = "Returns all payments for a specific doctor's appointments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved payments")
    })
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByDoctorId(@PathVariable int doctorId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByDoctorId(doctorId);
        return ResponseEntity.ok(payments);
    }
    
    @Operation(summary = "Cancel payment", description = "Cancels a payment (typically when appointment is cancelled)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Payment already cancelled"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PatchMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelPayment(@PathVariable int paymentId) {
        try {
            PaymentDTO payment = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
