package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDTO {
    private int id;
    // Common user attributes (from User table)
    private String name;
    private String email;
    private String phone;
    private String profilePictureUrl;
    // Doctor-specific attributes
    private String specialization; // Legacy field
    private List<String> specializations; // Multiple specializations
    private BigDecimal fee;
    private Integer appointmentDurationMinutes;
}
