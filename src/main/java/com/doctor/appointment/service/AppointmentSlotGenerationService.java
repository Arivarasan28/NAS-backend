package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.DTO.AvailableSlotDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for intelligent appointment slot generation based on:
 * - Doctor's working hours
 * - Doctor's approved leaves
 * - Appointment duration settings
 * - Existing appointments
 */
public interface AppointmentSlotGenerationService {
    
    /**
     * Generate available time slots for a doctor on a specific date
     * considering working hours, leaves, and existing appointments
     * 
     * @param doctorId The ID of the doctor
     * @param date The date for which to generate slots
     * @return List of available time slots
     */
    List<AvailableSlotDTO> generateAvailableSlots(int doctorId, LocalDate date);
    
    /**
     * Generate available time slots for a doctor for a date range
     * 
     * @param doctorId The ID of the doctor
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of available time slots grouped by date
     */
    List<AvailableSlotDTO> generateAvailableSlotsForDateRange(int doctorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Check if a specific time slot is available for booking
     * 
     * @param doctorId The ID of the doctor
     * @param dateTime The date and time to check
     * @return true if the slot is available, false otherwise
     */
    boolean isSlotAvailable(int doctorId, java.time.LocalDateTime dateTime);
    
    /**
     * Automatically create appointment slots for a doctor based on working hours
     * for a specific date
     * 
     * @param doctorId The ID of the doctor
     * @param date The date for which to create slots
     * @return List of created appointment slots
     */
    List<AppointmentDTO> autoGenerateAndCreateSlots(int doctorId, LocalDate date);
}
