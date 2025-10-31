package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusHistoryDTO {
    private AppointmentStatus fromStatus;
    private AppointmentStatus toStatus;
    private LocalDateTime changedAt;
    private String changedBy;
    private String note;
}
