package com.doctor.appointment.service;

import com.doctor.appointment.model.Appointment;
import com.doctor.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service to handle temporary slot reservations during payment process
 * Prevents race conditions when multiple users try to book the same slot
 */
@Service
public class AppointmentReservationService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    private static final int RESERVATION_DURATION_MINUTES = 5; // Slot reserved for 5 minutes

    /**
     * Reserve a slot temporarily for a patient during payment process
     * @param appointmentId The appointment slot to reserve
     * @param patientId The patient reserving the slot
     * @return true if reservation successful, false if already reserved by someone else
     */
    @Transactional
    public boolean reserveSlot(int appointmentId, int patientId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        
        if (optionalAppointment.isEmpty()) {
            return false;
        }

        Appointment appointment = optionalAppointment.get();
        LocalDateTime now = LocalDateTime.now();

        // Check if slot is already booked
        if (appointment.getPatient() != null) {
            return false;
        }

        // Check if slot is reserved by another patient
        if (appointment.getReservedByPatientId() != null) {
            // Check if reservation has expired
            if (appointment.getReservationExpiresAt() != null && 
                appointment.getReservationExpiresAt().isAfter(now)) {
                // Still reserved by someone else
                if (!appointment.getReservedByPatientId().equals(patientId)) {
                    return false;
                }
            }
        }

        // Reserve the slot
        appointment.setReservedByPatientId(patientId);
        appointment.setReservationExpiresAt(now.plusMinutes(RESERVATION_DURATION_MINUTES));
        appointmentRepository.save(appointment);

        return true;
    }

    /**
     * Release a reservation (called when payment is cancelled or completed)
     * @param appointmentId The appointment to release
     * @param patientId The patient who reserved it
     */
    @Transactional
    public void releaseReservation(int appointmentId, int patientId) {
        try {
            Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
            
            if (optionalAppointment.isEmpty()) {
                // Appointment doesn't exist (deleted or never existed)
                return;
            }

            Appointment appointment = optionalAppointment.get();

            // If appointment is already booked, no need to release reservation
            if (appointment.getPatient() != null) {
                return;
            }

            // Only release if this patient made the reservation
            if (appointment.getReservedByPatientId() != null && 
                appointment.getReservedByPatientId().equals(patientId)) {
                appointment.setReservedByPatientId(null);
                appointment.setReservationExpiresAt(null);
                appointmentRepository.save(appointment);
            }
        } catch (Exception e) {
            // Silently handle optimistic locking failures or other exceptions
            // If we can't release, it's likely already booked or deleted
            // which achieves the same goal
        }
    }

    /**
     * Check if a slot is available (not booked and not reserved)
     * @param appointmentId The appointment to check
     * @param patientId The patient checking (can access their own reservations)
     * @return true if available or reserved by this patient
     */
    public boolean isSlotAvailable(int appointmentId, int patientId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        
        if (optionalAppointment.isEmpty()) {
            return false;
        }

        Appointment appointment = optionalAppointment.get();
        LocalDateTime now = LocalDateTime.now();

        // Check if already booked
        if (appointment.getPatient() != null) {
            return false;
        }

        // Check if reserved by another patient
        if (appointment.getReservedByPatientId() != null) {
            // Check if reservation expired
            if (appointment.getReservationExpiresAt() != null && 
                appointment.getReservationExpiresAt().isBefore(now)) {
                // Reservation expired, slot is available
                return true;
            }
            // Reserved by this patient
            if (appointment.getReservedByPatientId().equals(patientId)) {
                return true;
            }
            // Reserved by someone else
            return false;
        }

        return true;
    }

    /**
     * Clean up expired reservations (can be called periodically)
     */
    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        appointmentRepository.findAll().forEach(appointment -> {
            try {
                if (appointment.getReservationExpiresAt() != null && 
                    appointment.getReservationExpiresAt().isBefore(now) &&
                    appointment.getPatient() == null) {
                    appointment.setReservedByPatientId(null);
                    appointment.setReservationExpiresAt(null);
                    appointmentRepository.save(appointment);
                }
            } catch (Exception e) {
                // Silently ignore optimistic locking failures
                // Appointment may have been deleted or updated by another transaction
            }
        });
    }
}
