package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.LeaveStatus;
import com.doctor.appointment.model.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLeaveDTO {
    private Long id;
    private int doctorId;
    private String doctorName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private LocalDateTime requestedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String adminNotes;
    private Boolean isHalfDay;
}
