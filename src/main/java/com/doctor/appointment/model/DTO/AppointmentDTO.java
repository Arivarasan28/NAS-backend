package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

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
    private AppointmentStatus status;
    private BigDecimal appointmentFee;
}
