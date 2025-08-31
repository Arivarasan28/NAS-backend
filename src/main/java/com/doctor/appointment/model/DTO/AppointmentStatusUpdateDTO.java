package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusUpdateDTO {
    
    @NotNull(message = "Status cannot be null")
    private AppointmentStatus status;
}
