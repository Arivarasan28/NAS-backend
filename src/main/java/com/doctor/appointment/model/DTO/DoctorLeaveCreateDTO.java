package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLeaveCreateDTO {
    private int doctorId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Boolean isHalfDay = false;
}
