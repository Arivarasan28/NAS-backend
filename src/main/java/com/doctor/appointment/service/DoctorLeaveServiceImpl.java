package com.doctor.appointment.service;

import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.DoctorLeave;
import com.doctor.appointment.model.LeaveStatus;
import com.doctor.appointment.model.DTO.DoctorLeaveCreateDTO;
import com.doctor.appointment.model.DTO.DoctorLeaveDTO;
import com.doctor.appointment.model.DTO.LeaveApprovalDTO;
import com.doctor.appointment.repository.DoctorLeaveRepository;
import com.doctor.appointment.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorLeaveServiceImpl implements DoctorLeaveService {

    @Autowired
    private DoctorLeaveRepository leaveRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    @Transactional
    public DoctorLeaveDTO createLeaveRequest(DoctorLeaveCreateDTO createDTO) {
        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(createDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + createDTO.getDoctorId()));

        // Validate dates
        if (createDTO.getEndDate().isBefore(createDTO.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }

        if (createDTO.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot create leave for past dates");
        }

        // Check for overlapping approved leaves
        List<DoctorLeave> overlappingLeaves = leaveRepository.findApprovedLeavesByDoctorAndDateRange(
                createDTO.getDoctorId(),
                createDTO.getStartDate(),
                createDTO.getEndDate()
        );

        if (!overlappingLeaves.isEmpty()) {
            throw new RuntimeException("There is already an approved leave for this period");
        }

        // Create leave entity
        DoctorLeave leave = new DoctorLeave();
        leave.setDoctor(doctor);
        leave.setLeaveType(createDTO.getLeaveType());
        leave.setStartDate(createDTO.getStartDate());
        leave.setEndDate(createDTO.getEndDate());
        leave.setReason(createDTO.getReason());
        leave.setIsHalfDay(createDTO.getIsHalfDay());
        leave.setStatus(LeaveStatus.PENDING);

        DoctorLeave savedLeave = leaveRepository.save(leave);
        return convertToDTO(savedLeave);
    }

    @Override
    public List<DoctorLeaveDTO> getLeavesByDoctorId(int doctorId) {
        return leaveRepository.findByDoctorIdOrderByStartDateDesc(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorLeaveDTO> getLeavesByStatus(LeaveStatus status) {
        return leaveRepository.findByStatusOrderByRequestedAtDesc(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorLeaveDTO getLeaveById(Long leaveId) {
        DoctorLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found with id: " + leaveId));
        return convertToDTO(leave);
    }

    @Override
    @Transactional
    public DoctorLeaveDTO updateLeaveStatus(Long leaveId, LeaveApprovalDTO approvalDTO) {
        DoctorLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found with id: " + leaveId));

        // Can only approve/reject pending leaves
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Can only approve/reject pending leave requests");
        }

        // Validate status
        if (approvalDTO.getStatus() != LeaveStatus.APPROVED && 
            approvalDTO.getStatus() != LeaveStatus.REJECTED) {
            throw new RuntimeException("Invalid status. Must be APPROVED or REJECTED");
        }

        leave.setStatus(approvalDTO.getStatus());
        leave.setApprovedBy(approvalDTO.getApprovedBy());
        leave.setApprovedAt(LocalDateTime.now());
        leave.setAdminNotes(approvalDTO.getAdminNotes());

        DoctorLeave updatedLeave = leaveRepository.save(leave);
        return convertToDTO(updatedLeave);
    }

    @Override
    @Transactional
    public DoctorLeaveDTO cancelLeave(Long leaveId, int doctorId) {
        DoctorLeave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found with id: " + leaveId));

        // Verify the leave belongs to the doctor
        if (leave.getDoctor().getId() != doctorId) {
            throw new RuntimeException("This leave does not belong to the specified doctor");
        }

        // Can only cancel pending or approved leaves
        if (leave.getStatus() == LeaveStatus.CANCELLED || leave.getStatus() == LeaveStatus.REJECTED) {
            throw new RuntimeException("Cannot cancel a leave that is already cancelled or rejected");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        DoctorLeave updatedLeave = leaveRepository.save(leave);
        return convertToDTO(updatedLeave);
    }

    @Override
    public boolean isDoctorOnLeave(int doctorId, LocalDate date) {
        return leaveRepository.isDoctorOnLeave(doctorId, date);
    }

    @Override
    public List<DoctorLeaveDTO> getApprovedLeavesByDoctorAndDateRange(int doctorId, LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findApprovedLeavesByDoctorAndDateRange(doctorId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorLeaveDTO> getLeavesByDateRange(LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findLeavesByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLeave(Long leaveId) {
        if (!leaveRepository.existsById(leaveId)) {
            throw new RuntimeException("Leave not found with id: " + leaveId);
        }
        leaveRepository.deleteById(leaveId);
    }

    private DoctorLeaveDTO convertToDTO(DoctorLeave leave) {
        DoctorLeaveDTO dto = new DoctorLeaveDTO();
        dto.setId(leave.getId());
        dto.setDoctorId(leave.getDoctor().getId());
        dto.setDoctorName(leave.getDoctor().getUser() != null ? leave.getDoctor().getUser().getName() : "Unknown");
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setRequestedAt(leave.getRequestedAt());
        dto.setApprovedBy(leave.getApprovedBy());
        dto.setApprovedAt(leave.getApprovedAt());
        dto.setAdminNotes(leave.getAdminNotes());
        dto.setIsHalfDay(leave.getIsHalfDay());
        return dto;
    }
}
