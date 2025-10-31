package com.doctor.appointment.controller;

import com.doctor.appointment.model.LeaveStatus;
import com.doctor.appointment.model.DTO.DoctorLeaveCreateDTO;
import com.doctor.appointment.model.DTO.DoctorLeaveDTO;
import com.doctor.appointment.model.DTO.LeaveApprovalDTO;
import com.doctor.appointment.service.DoctorLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/doctor-leave")
public class DoctorLeaveController {

    @Autowired
    private DoctorLeaveService leaveService;

    // Create a new leave request
    @PostMapping("/request")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<DoctorLeaveDTO> createLeaveRequest(@RequestBody DoctorLeaveCreateDTO createDTO) {
        try {
            DoctorLeaveDTO leave = leaveService.createLeaveRequest(createDTO);
            return ResponseEntity.ok(leave);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create leave request: " + e.getMessage());
        }
    }

    // Get all leaves for a specific doctor
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<List<DoctorLeaveDTO>> getLeavesByDoctor(@PathVariable int doctorId) {
        List<DoctorLeaveDTO> leaves = leaveService.getLeavesByDoctorId(doctorId);
        return ResponseEntity.ok(leaves);
    }

    // Get leaves by status (for admin)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<List<DoctorLeaveDTO>> getLeavesByStatus(@PathVariable LeaveStatus status) {
        List<DoctorLeaveDTO> leaves = leaveService.getLeavesByStatus(status);
        return ResponseEntity.ok(leaves);
    }

    // Get a specific leave by ID
    @GetMapping("/{leaveId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<DoctorLeaveDTO> getLeaveById(@PathVariable Long leaveId) {
        DoctorLeaveDTO leave = leaveService.getLeaveById(leaveId);
        return ResponseEntity.ok(leave);
    }

    // Approve or reject a leave request (admin only)
    @PutMapping("/{leaveId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorLeaveDTO> updateLeaveStatus(
            @PathVariable Long leaveId,
            @RequestBody LeaveApprovalDTO approvalDTO) {
        try {
            DoctorLeaveDTO leave = leaveService.updateLeaveStatus(leaveId, approvalDTO);
            return ResponseEntity.ok(leave);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update leave status: " + e.getMessage());
        }
    }

    // Cancel a leave request (by doctor)
    @PutMapping("/{leaveId}/cancel")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<DoctorLeaveDTO> cancelLeave(
            @PathVariable Long leaveId,
            @RequestParam int doctorId) {
        try {
            DoctorLeaveDTO leave = leaveService.cancelLeave(leaveId, doctorId);
            return ResponseEntity.ok(leave);
        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel leave: " + e.getMessage());
        }
    }

    // Check if doctor is on leave for a specific date
    @GetMapping("/check-availability")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Boolean>> checkDoctorAvailability(
            @RequestParam int doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean isOnLeave = leaveService.isDoctorOnLeave(doctorId, date);
        return ResponseEntity.ok(Map.of("isOnLeave", isOnLeave, "isAvailable", !isOnLeave));
    }

    // Get approved leaves for a doctor within a date range
    @GetMapping("/doctor/{doctorId}/range")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<DoctorLeaveDTO>> getApprovedLeavesByDateRange(
            @PathVariable int doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DoctorLeaveDTO> leaves = leaveService.getApprovedLeavesByDoctorAndDateRange(doctorId, startDate, endDate);
        return ResponseEntity.ok(leaves);
    }

    // Get all leaves within a date range (for admin dashboard)
    @GetMapping("/range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ResponseEntity<List<DoctorLeaveDTO>> getLeavesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DoctorLeaveDTO> leaves = leaveService.getLeavesByDateRange(startDate, endDate);
        return ResponseEntity.ok(leaves);
    }

    // Delete a leave request (admin only)
    @DeleteMapping("/{leaveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteLeave(@PathVariable Long leaveId) {
        try {
            leaveService.deleteLeave(leaveId);
            return ResponseEntity.ok("Leave deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete leave: " + e.getMessage());
        }
    }
}
