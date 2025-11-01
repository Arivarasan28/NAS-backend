package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.BookingResponseDTO;
import com.doctor.appointment.model.DTO.BookingWithPaymentDTO;
import com.doctor.appointment.model.DTO.PaymentCreateDTO;
import com.doctor.appointment.model.DTO.PaymentDTO;

import java.util.List;

public interface PaymentService {
    
    /**
     * Process payment for an appointment
     * @param paymentCreateDTO Payment details
     * @return Created payment
     */
    PaymentDTO processPayment(PaymentCreateDTO paymentCreateDTO);
    
    /**
     * Book appointment with payment in a single transaction
     * This ensures atomicity - either both succeed or both fail
     * @param bookingWithPaymentDTO Booking and payment details
     * @return Booking response with appointment and payment details
     */
    BookingResponseDTO bookAppointmentWithPayment(BookingWithPaymentDTO bookingWithPaymentDTO);
    
    /**
     * Get payment by ID
     * @param paymentId Payment ID
     * @return Payment details
     */
    PaymentDTO getPaymentById(int paymentId);
    
    /**
     * Get payment by appointment ID
     * @param appointmentId Appointment ID
     * @return Payment details
     */
    PaymentDTO getPaymentByAppointmentId(int appointmentId);
    
    /**
     * Get all payments for a patient
     * @param patientId Patient ID
     * @return List of payments
     */
    List<PaymentDTO> getPaymentsByPatientId(int patientId);
    
    /**
     * Get all payments for a doctor
     * @param doctorId Doctor ID
     * @return List of payments
     */
    List<PaymentDTO> getPaymentsByDoctorId(int doctorId);
    
    /**
     * Cancel payment (for cancelled appointments)
     * @param paymentId Payment ID
     * @return Updated payment
     */
    PaymentDTO cancelPayment(int paymentId);
}
