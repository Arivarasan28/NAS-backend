package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing an available appointment slot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private boolean isAvailable;
    private String unavailabilityReason; // e.g., "On Leave", "Outside Working Hours", "Already Booked"
    
    // Optional: If the slot is already created in the database
    private Integer appointmentId;
    
    // Doctor information
    private int doctorId;
    private String doctorName;
    
    public AvailableSlotDTO(LocalDateTime startTime, LocalDateTime endTime, int durationMinutes, 
                           boolean isAvailable, int doctorId, String doctorName) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.isAvailable = isAvailable;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
    }
}
