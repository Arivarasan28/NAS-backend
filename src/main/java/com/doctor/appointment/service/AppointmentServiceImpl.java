package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.DTO.AppointmentSlotCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentStatusHistoryDTO;
import com.doctor.appointment.model.DTO.AppointmentStatusUpdateDTO;
import com.doctor.appointment.model.Appointment;
import com.doctor.appointment.model.AppointmentStatus;
import com.doctor.appointment.model.AppointmentStatusHistory;
import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.repository.AppointmentRepository;
import com.doctor.appointment.repository.AppointmentStatusHistoryRepository;
import com.doctor.appointment.repository.DoctorRepository;
import com.doctor.appointment.repository.PatientRepository;
// ModelMapper not needed anymore as we use custom mapping
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;

    // ModelMapper removed as we use custom mapping

    @Override
    public List<AppointmentDTO> findAll() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentStatusHistoryDTO> getStatusHistory(int appointmentId) {
        // Ensure appointment exists
        appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));
        return appointmentStatusHistoryRepository
                .findByAppointmentIdOrderByChangedAtAsc(appointmentId)
                .stream()
                .map(h -> new AppointmentStatusHistoryDTO(
                        h.getFromStatus(),
                        h.getToStatus(),
                        h.getChangedAt(),
                        h.getChangedBy(),
                        h.getNote()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentDTO findById(int theId) {
        Appointment appointment = appointmentRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + theId));
        return convertToDTO(appointment);
    }

    @Override
    public AppointmentDTO save(AppointmentCreateDTO appointmentCreateDTO) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(appointmentCreateDTO.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.AVAILABLE); // Default to AVAILABLE for new appointments

        // Set doctor
        Doctor doctor = doctorRepository.findById(appointmentCreateDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + appointmentCreateDTO.getDoctorId()));
        appointment.setDoctor(doctor);

        // Set patient if provided
        Patient patient = patientRepository.findById(appointmentCreateDTO.getPatientId())
                .orElse(null);
        appointment.setPatient(patient);
        
        // Set reason if provided
        appointment.setReason(appointmentCreateDTO.getReason());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
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
        dto.setStatus(appointment.getStatus());
        
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
    
    @Override
    public List<AppointmentDTO> findByDoctorId(int doctorId) {
        // First check if doctor exists
        boolean doctorExists = doctorRepository.findById(doctorId).isPresent();
        
        if (!doctorExists) {
            // Log the issue but don't throw exception
            System.out.println("Warning: Doctor not found with id: " + doctorId + ". Returning empty appointment list.");
            // Return an empty list instead of throwing an exception
            return new ArrayList<>();
        }
        
        // Find appointments for the doctor
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AppointmentDTO> findByDoctorIdAndDate(int doctorId, String dateString) {
        // First check if doctor exists
        boolean doctorExists = doctorRepository.findById(doctorId).isPresent();
        
        if (!doctorExists) {
            // Log the issue but don't throw exception
            System.out.println("Warning: Doctor not found with id: " + doctorId + ". Returning empty appointment list for date: " + dateString);
            // Return an empty list instead of throwing an exception
            return new ArrayList<>();
        }
        
        // Parse the date string to LocalDate
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        
        // Calculate start and end of the day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1);
        
        // Find appointments for the doctor on the specified date
        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AppointmentDTO updateStatus(int appointmentId, AppointmentStatusUpdateDTO statusUpdateDTO) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        AppointmentStatus fromStatus = appointment.getStatus();
        AppointmentStatus toStatus = statusUpdateDTO.getStatus();

        // Define allowed transitions
        Map<AppointmentStatus, List<AppointmentStatus>> allowed = new EnumMap<>(AppointmentStatus.class);
        allowed.put(AppointmentStatus.AVAILABLE, Arrays.asList(AppointmentStatus.BOOKED));
        allowed.put(AppointmentStatus.BOOKED, Arrays.asList(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED));
        allowed.put(AppointmentStatus.CONFIRMED, Arrays.asList(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED));
        allowed.put(AppointmentStatus.CANCELLED, List.of());
        allowed.put(AppointmentStatus.COMPLETED, List.of());

        // Validate transition (allow idempotent: from == to)
        if (fromStatus != toStatus && (!allowed.containsKey(fromStatus) || !allowed.get(fromStatus).contains(toStatus))) {
            throw new RuntimeException("Invalid status transition from " + fromStatus + " to " + toStatus);
        }

        // Update the status only if changed
        if (fromStatus != toStatus) {
            appointment.setStatus(toStatus);
            Appointment saved = appointmentRepository.save(appointment);

            // Write history record
            AppointmentStatusHistory history = new AppointmentStatusHistory();
            history.setAppointment(saved);
            history.setFromStatus(fromStatus);
            history.setToStatus(toStatus);
            history.setChangedAt(java.time.LocalDateTime.now());
            history.setChangedBy(statusUpdateDTO.getChangedBy());
            history.setNote(statusUpdateDTO.getNote());
            appointmentStatusHistoryRepository.save(history);

            return convertToDTO(saved);
        }

        // No change, but still return DTO
        return convertToDTO(appointment);
    }
    
    @Override
    public List<AppointmentDTO> createAppointmentSlots(AppointmentSlotCreateDTO slotCreateDTO) {
        try {
            // First check if doctor exists
            Optional<Doctor> doctorOptional = doctorRepository.findById(slotCreateDTO.getDoctorId());
            
            if (!doctorOptional.isPresent()) {
                // Log the error and throw a more descriptive exception
                String errorMsg = "Doctor not found with id: " + slotCreateDTO.getDoctorId();
                System.out.println("Error creating appointment slots: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            Doctor doctor = doctorOptional.get();
            
            // Parse the date
            LocalDate date = LocalDate.parse(slotCreateDTO.getDate(), DateTimeFormatter.ISO_DATE);
            
            // Variables to store the parsed time values
            LocalTime startTime;
            LocalTime endTime;
            
            // Parse start and end times from ISO format timestamps with timezone
            try {
                // Try to parse as ZonedDateTime first (handles formats with Z or timezone offset)
                ZonedDateTime startZonedDateTime = ZonedDateTime.parse(slotCreateDTO.getStartTime());
                ZonedDateTime endZonedDateTime = ZonedDateTime.parse(slotCreateDTO.getEndTime());
                
                // Extract just the time part
                startTime = startZonedDateTime.toLocalDateTime().toLocalTime();
                endTime = endZonedDateTime.toLocalDateTime().toLocalTime();
            } catch (DateTimeParseException e) {
                // Fallback to LocalDateTime parsing if ZonedDateTime fails
                try {
                    LocalDateTime startDateTime = LocalDateTime.parse(slotCreateDTO.getStartTime());
                    LocalDateTime endDateTime = LocalDateTime.parse(slotCreateDTO.getEndTime());
                    
                    // Extract just the time part
                    startTime = startDateTime.toLocalTime();
                    endTime = endDateTime.toLocalTime();
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Invalid date/time format. Expected ISO format like '2025-06-25T09:00:00Z'", e2);
                }
            }
            
            // Calculate slot duration in minutes
            int durationMinutes = slotCreateDTO.getDurationMinutes();
            
            // Create a list to hold all the appointments
            List<Appointment> createdAppointments = new ArrayList<>();
            
            // Generate appointment slots
            LocalTime currentSlotStart = startTime;
            while (currentSlotStart.plusMinutes(durationMinutes).compareTo(endTime) <= 0) {
                LocalTime currentSlotEnd = currentSlotStart.plusMinutes(durationMinutes);
                
                // Create a new appointment slot
                Appointment slot = new Appointment();
                slot.setDoctor(doctor);
                slot.setAppointmentTime(LocalDateTime.of(date, currentSlotStart));
                slot.setStatus(AppointmentStatus.AVAILABLE); // Set as available by default
                slot.setReason("Available Appointment Slot");
                
                // Save the appointment slot
                createdAppointments.add(appointmentRepository.save(slot));
                
                // Move to the next slot
                currentSlotStart = currentSlotEnd;
            }
            
            // Convert all created appointments to DTOs and return
            return createdAppointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (DateTimeParseException e) {
            // Handle date/time parsing errors
            throw new RuntimeException("Invalid date or time format: " + e.getMessage(), e);
        } catch (Exception e) {
            // Log and rethrow other exceptions
            System.err.println("Error creating appointment slots: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public boolean deleteAvailableSlot(int appointmentId, int doctorId) {
        // Find the appointment
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        
        // Check if appointment exists, belongs to the doctor, and is in AVAILABLE status
        if (appointment == null) {
            return false;
        }
        
        if (appointment.getDoctor().getId() != doctorId) {
            // Appointment doesn't belong to this doctor
            return false;
        }
        
        if (appointment.getStatus() != AppointmentStatus.AVAILABLE) {
            // Can only delete available slots, not booked or confirmed ones
            return false;
        }
        
        // Delete the appointment slot
        appointmentRepository.deleteById(appointmentId);
        return true;
    }
    
    @Override
    public List<AppointmentDTO> getAvailableSlotsByDoctorAndDate(int doctorId, LocalDate date) {
        try {
            // Check if doctor exists
            Optional<Doctor> doctorOptional = doctorRepository.findById(doctorId);
            
            if (!doctorOptional.isPresent()) {
                // Log the issue but don't throw exception
                System.out.println("Warning: Doctor not found with id: " + doctorId + ". Returning empty appointment list for date: " + date);
                // Return an empty list instead of throwing an exception
                return new ArrayList<>();
            }
            
            Doctor doctor = doctorOptional.get();
            System.out.println("Found doctor: " + doctor.getId() + " - " + doctor.getName());
            
            // Calculate start and end of the day
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1);
            
            System.out.println("Searching for slots between: " + startOfDay + " and " + endOfDay);
            
            // Find available appointments for the doctor on the specified date
            List<Appointment> appointments = appointmentRepository.findByDoctorIdAndStatusAndAppointmentTimeBetween(
                    doctorId, 
                    AppointmentStatus.AVAILABLE, 
                    startOfDay, 
                    endOfDay);
            
            System.out.println("Found " + appointments.size() + " available appointments");
            
            return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAvailableSlotsByDoctorAndDate: " + e.getMessage());
            e.printStackTrace();
            // Return empty list instead of propagating the exception
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<AppointmentDTO> findByPatientId(int patientId) {
        // First check if the patient exists
        patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
        
        // Find all appointments for this patient
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        
        // Convert all entities to DTOs
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AppointmentDTO bookAppointment(int appointmentId, int patientId) {
        // Find the appointment slot
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment slot not found: " + appointmentId));
        
        // Check if the slot is available
        if (appointment.getStatus() != AppointmentStatus.AVAILABLE) {
            throw new RuntimeException("This appointment slot is not available for booking");
        }
        
        // Find the patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
        
        // Update the appointment
        AppointmentStatus fromStatus = appointment.getStatus();
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.BOOKED);
        
        // Save the updated appointment
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // Write history record for booking
        AppointmentStatusHistory history = new AppointmentStatusHistory();
        history.setAppointment(updatedAppointment);
        history.setFromStatus(fromStatus);
        history.setToStatus(AppointmentStatus.BOOKED);
        history.setChangedAt(java.time.LocalDateTime.now());
        history.setChangedBy("PATIENT:" + patientId);
        history.setNote("Booked appointment");
        appointmentStatusHistoryRepository.save(history);

        return convertToDTO(updatedAppointment);
    }
}
