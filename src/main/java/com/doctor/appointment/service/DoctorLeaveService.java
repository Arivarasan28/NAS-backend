package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.DoctorLeaveCreateDTO;
import com.doctor.appointment.model.DTO.DoctorLeaveDTO;
import com.doctor.appointment.model.DTO.LeaveApprovalDTO;
import com.doctor.appointment.model.LeaveStatus;

import java.time.LocalDate;
import java.util.List;

public interface DoctorLeaveService {
    
    // Create a new leave request
    DoctorLeaveDTO createLeaveRequest(DoctorLeaveCreateDTO createDTO);
    
    // Get all leaves for a doctor
    List<DoctorLeaveDTO> getLeavesByDoctorId(int doctorId);
    
    // Get leaves by status (for admin)
    List<DoctorLeaveDTO> getLeavesByStatus(LeaveStatus status);
    
    // Get a specific leave by ID
    DoctorLeaveDTO getLeaveById(Long leaveId);
    
    // Approve or reject a leave request
    DoctorLeaveDTO updateLeaveStatus(Long leaveId, LeaveApprovalDTO approvalDTO);
    
    // Cancel a leave request (by doctor)
    DoctorLeaveDTO cancelLeave(Long leaveId, int doctorId);
    
    // Check if doctor is on leave for a specific date
    boolean isDoctorOnLeave(int doctorId, LocalDate date);
    
    // Get all approved leaves for a doctor within a date range
    List<DoctorLeaveDTO> getApprovedLeavesByDoctorAndDateRange(int doctorId, LocalDate startDate, LocalDate endDate);
    
    // Get all leaves within a date range (for admin dashboard)
    List<DoctorLeaveDTO> getLeavesByDateRange(LocalDate startDate, LocalDate endDate);
    
    // Delete a leave request (admin only)
    void deleteLeave(Long leaveId);
}
