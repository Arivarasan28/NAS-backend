package com.doctor.appointment.repository;

import com.doctor.appointment.model.DoctorLeave;
import com.doctor.appointment.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {

    // Find all leaves for a specific doctor
    List<DoctorLeave> findByDoctorIdOrderByStartDateDesc(int doctorId);

    // Find leaves by status
    List<DoctorLeave> findByStatusOrderByRequestedAtDesc(LeaveStatus status);

    // Find leaves for a doctor with a specific status
    List<DoctorLeave> findByDoctorIdAndStatusOrderByStartDateDesc(int doctorId, LeaveStatus status);

    // Check if doctor is on leave for a specific date
    @Query("SELECT CASE WHEN COUNT(dl) > 0 THEN true ELSE false END FROM DoctorLeave dl " +
           "WHERE dl.doctor.id = :doctorId " +
           "AND dl.status = 'APPROVED' " +
           "AND :date BETWEEN dl.startDate AND dl.endDate")
    boolean isDoctorOnLeave(@Param("doctorId") int doctorId, @Param("date") LocalDate date);

    // Find all approved leaves for a doctor within a date range
    @Query("SELECT dl FROM DoctorLeave dl " +
           "WHERE dl.doctor.id = :doctorId " +
           "AND dl.status = 'APPROVED' " +
           "AND ((dl.startDate BETWEEN :startDate AND :endDate) " +
           "OR (dl.endDate BETWEEN :startDate AND :endDate) " +
           "OR (dl.startDate <= :startDate AND dl.endDate >= :endDate))")
    List<DoctorLeave> findApprovedLeavesByDoctorAndDateRange(
            @Param("doctorId") int doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find all leaves within a date range (for admin dashboard)
    @Query("SELECT dl FROM DoctorLeave dl " +
           "WHERE ((dl.startDate BETWEEN :startDate AND :endDate) " +
           "OR (dl.endDate BETWEEN :startDate AND :endDate) " +
           "OR (dl.startDate <= :startDate AND dl.endDate >= :endDate)) " +
           "ORDER BY dl.startDate ASC")
    List<DoctorLeave> findLeavesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
