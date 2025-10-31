package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalDTO {
    private LeaveStatus status; // APPROVED or REJECTED
    private String adminNotes;
    private String approvedBy; // Username of admin
}
