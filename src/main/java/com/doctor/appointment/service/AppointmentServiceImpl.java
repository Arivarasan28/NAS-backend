package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.Appointment;
import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.repository.AppointmentRepository;
import com.doctor.appointment.repository.DoctorRepository;
import com.doctor.appointment.repository.PatientRepository;
// ModelMapper not needed anymore as we use custom mapping
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;

    // ModelMapper removed as we use custom mapping

    @Override
    public List<AppointmentDTO> findAll() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentDTO findById(int theId) {
        Appointment appointment = appointmentRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + theId));
        return convertToDTO(appointment);
    }

    @Override
    public Appointment save(AppointmentCreateDTO appointmentCreateDTO) {
        Appointment appointment = new Appointment();
        
        // Find and set the doctor
        Doctor doctor = doctorRepository.findById(appointmentCreateDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + appointmentCreateDTO.getDoctorId()));
        appointment.setDoctor(doctor);
        
        // Find and set the patient
        Patient patient = patientRepository.findById(appointmentCreateDTO.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + appointmentCreateDTO.getPatientId()));
        appointment.setPatient(patient);
        
        // Set other fields
        appointment.setAppointmentTime(appointmentCreateDTO.getAppointmentTime());
        appointment.setReason(appointmentCreateDTO.getReason());
        
        return appointmentRepository.save(appointment);
    }

    @Override
    public void deleteById(int theId) {
        appointmentRepository.deleteById(theId);
    }
    
    /**
     * Convert Appointment entity to AppointmentDTO with enhanced doctor and patient information
     * @param appointment the appointment entity
     * @return the enhanced AppointmentDTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        
        // Basic appointment info
        dto.setId(appointment.getId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setReason(appointment.getReason());
        
        // Doctor information
        Doctor doctor = appointment.getDoctor();
        if (doctor != null) {
            dto.setDoctorId(doctor.getId());
            dto.setDoctorName(doctor.getName());
            dto.setDoctorSpecialization(doctor.getSpecialization());
        }
        
        // Patient information
        Patient patient = appointment.getPatient();
        if (patient != null) {
            dto.setPatientId(patient.getId());
            dto.setPatientName(patient.getName());
        }
        
        return dto;
    }

    @Override
    public AppointmentDTO update(int appointmentId, AppointmentCreateDTO appointmentCreateDTO) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        // Update doctor if changed
        if (existingAppointment.getDoctor().getId() != appointmentCreateDTO.getDoctorId()) {
            Doctor doctor = doctorRepository.findById(appointmentCreateDTO.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + appointmentCreateDTO.getDoctorId()));
            existingAppointment.setDoctor(doctor);
        }
        
        // Update patient if changed
        if (existingAppointment.getPatient().getId() != appointmentCreateDTO.getPatientId()) {
            Patient patient = patientRepository.findById(appointmentCreateDTO.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + appointmentCreateDTO.getPatientId()));
            existingAppointment.setPatient(patient);
        }
        
        // Update other fields
        existingAppointment.setAppointmentTime(appointmentCreateDTO.getAppointmentTime());
        existingAppointment.setReason(appointmentCreateDTO.getReason());

        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

        return convertToDTO(updatedAppointment);
    }
}
