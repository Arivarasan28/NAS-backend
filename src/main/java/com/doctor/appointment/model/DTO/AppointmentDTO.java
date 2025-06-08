package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDTO {
    private int id;
    
    // Doctor information
    private int doctorId;
    private String doctorName;
    private String doctorSpecialization;
    
    // Patient information
    private int patientId;
    private String patientName;
    
    private LocalDateTime appointmentTime;
    private String reason;
}
