package com.doctor.appointment.service;

import com.doctor.appointment.model.*;
import com.doctor.appointment.model.DTO.*;
import com.doctor.appointment.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;
    private final DoctorLeaveService doctorLeaveService;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional
    public PaymentDTO processPayment(PaymentCreateDTO paymentCreateDTO) {
        // Find the appointment
        Appointment appointment = appointmentRepository.findById(paymentCreateDTO.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + paymentCreateDTO.getAppointmentId()));
        
        // Check if payment already exists
        if (paymentRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new RuntimeException("Payment already exists for this appointment");
        }
        
        // Create payment
        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setPaymentMethod(paymentCreateDTO.getPaymentMethod());
        payment.setAmount(paymentCreateDTO.getAmount());
        payment.setCardDetails(paymentCreateDTO.getCardDetails());
        payment.setNotes(paymentCreateDTO.getNotes());
        
        // Generate transaction ID
        payment.setTransactionId(generateTransactionId());
        
        // Set payment status based on payment method
        if (paymentCreateDTO.getPaymentMethod() == PaymentMethod.CASH) {
            payment.setPaymentStatus(PaymentStatus.PENDING); // Cash payment pending at clinic
        } else {
            // For card payment, simulate payment processing
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment processed successfully for appointment ID: {}", appointment.getId());
        
        return convertToDTO(savedPayment);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponseDTO bookAppointmentWithPayment(BookingWithPaymentDTO bookingWithPaymentDTO) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                return attemptBookingWithPayment(bookingWithPaymentDTO);
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                logger.warn("Optimistic locking failure on attempt {} for appointment ID: {}. Retrying...", 
                           retryCount, bookingWithPaymentDTO.getAppointmentId());
                
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("This appointment slot was just booked by another user. Please select a different time slot.");
                }
                
                // Brief pause before retry
                try {
                    Thread.sleep(100 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Booking process interrupted");
                }
            }
        }
        
        throw new RuntimeException("Failed to book appointment after multiple attempts");
    }
    
    private BookingResponseDTO attemptBookingWithPayment(BookingWithPaymentDTO bookingWithPaymentDTO) {
        // Find the appointment slot with pessimistic write lock
        Appointment appointment = appointmentRepository.findById(bookingWithPaymentDTO.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment slot not found: " + bookingWithPaymentDTO.getAppointmentId()));
        
        // Check if the slot is available
        if (appointment.getStatus() != AppointmentStatus.AVAILABLE) {
            throw new RuntimeException("This appointment slot is not available for booking. Current status: " + appointment.getStatus());
        }
        
        // Verify reservation - slot must be reserved by this patient
        if (appointment.getReservedByPatientId() != null) {
            if (!appointment.getReservedByPatientId().equals(bookingWithPaymentDTO.getPatientId())) {
                throw new RuntimeException("This slot is reserved by another user. Please select a different time slot.");
            }
            // Check if reservation expired
            if (appointment.getReservationExpiresAt() != null && 
                appointment.getReservationExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Your reservation has expired. Please reserve the slot again.");
            }
        } else {
            // Slot not reserved - this shouldn't happen if frontend is working correctly
            logger.warn("Attempting to book slot {} without reservation for patient {}", 
                       appointment.getId(), bookingWithPaymentDTO.getPatientId());
        }
        
        // Check if doctor is on leave
        LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();
        if (doctorLeaveService.isDoctorOnLeave(appointment.getDoctor().getId(), appointmentDate)) {
            throw new RuntimeException("Cannot book appointment. Doctor is on leave for the selected date: " + appointmentDate);
        }
        
        // Find the patient
        Patient patient = patientRepository.findById(bookingWithPaymentDTO.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + bookingWithPaymentDTO.getPatientId()));
        
        // Check if payment already exists for this appointment
        if (paymentRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new RuntimeException("Payment already exists for this appointment");
        }
        
        // Update appointment status to BOOKED
        AppointmentStatus fromStatus = appointment.getStatus();
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.BOOKED);
        
        // Clear reservation fields since booking is confirmed
        appointment.setReservedByPatientId(null);
        appointment.setReservationExpiresAt(null);
        
        // Save appointment (this will trigger optimistic locking check)
        Appointment bookedAppointment = appointmentRepository.save(appointment);
        
        // Create appointment status history
        AppointmentStatusHistory history = new AppointmentStatusHistory();
        history.setAppointment(bookedAppointment);
        history.setFromStatus(fromStatus);
        history.setToStatus(AppointmentStatus.BOOKED);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy("PATIENT:" + bookingWithPaymentDTO.getPatientId());
        history.setNote("Booked appointment with payment");
        appointmentStatusHistoryRepository.save(history);
        
        // Create payment record
        Payment payment = new Payment();
        payment.setAppointment(bookedAppointment);
        payment.setPaymentMethod(bookingWithPaymentDTO.getPaymentMethod());
        payment.setAmount(bookingWithPaymentDTO.getAmount());
        payment.setCardDetails(bookingWithPaymentDTO.getCardDetails());
        payment.setNotes(bookingWithPaymentDTO.getNotes());
        payment.setTransactionId(generateTransactionId());
        
        // Set payment status based on payment method
        if (bookingWithPaymentDTO.getPaymentMethod() == PaymentMethod.CASH) {
            payment.setPaymentStatus(PaymentStatus.PENDING); // Cash payment pending at clinic
        } else {
            // For card payment, simulate payment processing
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        logger.info("Successfully booked appointment ID: {} for patient ID: {} with payment", 
                   bookedAppointment.getId(), patient.getId());
        
        // Prepare response
        BookingResponseDTO response = new BookingResponseDTO();
        response.setAppointment(convertAppointmentToDTO(bookedAppointment));
        response.setPayment(convertToDTO(savedPayment));
        response.setMessage("Appointment booked successfully with " + 
                           (payment.getPaymentMethod() == PaymentMethod.CASH ? "cash payment (pending at clinic)" : "card payment"));
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(int paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        return convertToDTO(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByAppointmentId(int appointmentId) {
        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for appointment id: " + appointmentId));
        return convertToDTO(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByPatientId(int patientId) {
        return paymentRepository.findByPatientId(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByDoctorId(int doctorId) {
        return paymentRepository.findByDoctorId(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public PaymentDTO cancelPayment(int paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        
        if (payment.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new RuntimeException("Payment is already cancelled");
        }
        
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        Payment updatedPayment = paymentRepository.save(payment);
        
        logger.info("Payment cancelled for payment ID: {}", paymentId);
        return convertToDTO(updatedPayment);
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = modelMapper.map(payment, PaymentDTO.class);
        dto.setAppointmentId(payment.getAppointment().getId());
        return dto;
    }
    
    private AppointmentDTO convertAppointmentToDTO(Appointment appointment) {
        return modelMapper.map(appointment, AppointmentDTO.class);
    }
}
