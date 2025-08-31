package com.doctor.appointment.repository;

import com.doctor.appointment.model.Appointment;
import com.doctor.appointment.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    
    // Find appointments by doctor ID
    List<Appointment> findByDoctorId(int doctorId);
    
    // Find appointments by doctor ID and appointment time between start and end
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            int doctorId, 
            LocalDateTime start, 
            LocalDateTime end);
    
    // Find appointments by doctor ID, status, and appointment time between start and end
    List<Appointment> findByDoctorIdAndStatusAndAppointmentTimeBetween(
            int doctorId,
            AppointmentStatus status,
            LocalDateTime start, 
            LocalDateTime end);
            
    // Find appointments by patient ID
    List<Appointment> findByPatientId(int patientId);
}
