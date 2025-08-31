package com.doctor.appointment.model.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotCreateDTO {
    
    @NotNull(message = "Doctor ID is required")
    private Integer doctorId;
    
    @NotEmpty(message = "Date is required")
    private String date;
    
    @NotEmpty(message = "Start time is required")
    private String startTime;
    
    @NotEmpty(message = "End time is required")
    private String endTime;
    
    @NotNull(message = "Duration in minutes is required")
    @Min(value = 5, message = "Duration must be at least 5 minutes")
    private Integer durationMinutes;
}
