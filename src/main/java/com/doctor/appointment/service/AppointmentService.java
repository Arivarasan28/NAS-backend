package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.DTO.AppointmentSlotCreateDTO;
import com.doctor.appointment.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    List<AppointmentDTO> findAll();

    AppointmentDTO findById(int theId);

    AppointmentDTO save(AppointmentCreateDTO appointmentCreateDTO);

    void deleteById(int theId);

    AppointmentDTO update(int appointmentId, AppointmentCreateDTO appointmentCreateDTO);
    
    List<AppointmentDTO> findByDoctorId(int doctorId);
    
    List<AppointmentDTO> findByDoctorIdAndDate(int doctorId, String date);
    
    AppointmentDTO updateStatus(int appointmentId, AppointmentStatus status);
    
    List<AppointmentDTO> createAppointmentSlots(AppointmentSlotCreateDTO slotCreateDTO);
    
    /**
     * Delete an available appointment slot
     * Only slots with AVAILABLE status can be deleted
     * @param appointmentId The ID of the appointment slot to delete
     * @param doctorId The ID of the doctor who owns the slot
     * @return true if deleted successfully, false if not found or not available
     */
    boolean deleteAvailableSlot(int appointmentId, int doctorId);
    
    /**
     * Get available appointment slots for a specific doctor and date
     * @param doctorId The ID of the doctor
     * @param date The date to check for available slots
     * @return List of available appointment slots
     */
    List<AppointmentDTO> getAvailableSlotsByDoctorAndDate(int doctorId, LocalDate date);
    
    /**
     * Book an appointment slot
     * @param appointmentId The ID of the appointment slot to book
     * @param patientId The ID of the patient booking the appointment
     * @return The updated appointment
     */
    AppointmentDTO bookAppointment(int appointmentId, int patientId);
    
    /**
     * Find all appointments for a specific patient
     * @param patientId The ID of the patient
     * @return List of appointments for the patient
     */
    List<AppointmentDTO> findByPatientId(int patientId);
}
