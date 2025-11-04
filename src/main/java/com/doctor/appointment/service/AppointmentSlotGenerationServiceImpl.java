package com.doctor.appointment.service;

import com.doctor.appointment.model.*;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.DTO.AvailableSlotDTO;
import com.doctor.appointment.repository.AppointmentRepository;
import com.doctor.appointment.repository.DoctorRepository;
import com.doctor.appointment.repository.WorkingHourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentSlotGenerationServiceImpl implements AppointmentSlotGenerationService {

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private WorkingHourRepository workingHourRepository;
    
    @Autowired
    private DoctorLeaveService doctorLeaveService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public List<AvailableSlotDTO> generateAvailableSlots(int doctorId, LocalDate date) {
        List<AvailableSlotDTO> availableSlots = new ArrayList<>();
        
        // 1. Get doctor information
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        // 2. Check if doctor is on leave
        if (doctorLeaveService.isDoctorOnLeave(doctorId, date)) {
            // Return empty list if doctor is on leave
            return availableSlots;
        }
        
        // 3. Get working hours for the day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<WorkingHour> workingHours = workingHourRepository
                .findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(doctorId, dayOfWeek);
        
        if (workingHours.isEmpty()) {
            // No working hours defined for this day
            return availableSlots;
        }
        
        // 4. Get appointment duration (default 30 minutes if not set)
        int durationMinutes = doctor.getAppointmentDurationMinutes() != null 
                ? doctor.getAppointmentDurationMinutes() 
                : 30;
        
        // 5. Get existing appointments for the date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1);
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        
        // 6. Generate slots for each working hour block
        for (WorkingHour workingHour : workingHours) {
            // Check if working hour is effective for this date
            if (!isWorkingHourEffective(workingHour, date)) {
                continue;
            }
            
            LocalTime currentTime = workingHour.getStartTime();
            LocalTime endTime = workingHour.getEndTime();
            
            while (currentTime.plusMinutes(durationMinutes).compareTo(endTime) <= 0) {
                LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
                LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);
                
                // Check if slot overlaps with existing appointments
                boolean isOccupied = existingAppointments.stream()
                        .anyMatch(apt -> isTimeOverlapping(slotStart, slotEnd, apt.getAppointmentTime(), 
                                apt.getAppointmentTime().plusMinutes(durationMinutes)));
                
                AvailableSlotDTO slot = new AvailableSlotDTO();
                slot.setStartTime(slotStart);
                slot.setEndTime(slotEnd);
                slot.setDurationMinutes(durationMinutes);
                slot.setDoctorId(doctorId);
                slot.setDoctorName(doctor.getUser() != null ? doctor.getUser().getName() : "Unknown");
                slot.setAvailable(!isOccupied);
                
                if (isOccupied) {
                    slot.setUnavailabilityReason("Already Booked");
                    // Find the appointment ID if it exists
                    existingAppointments.stream()
                            .filter(apt -> apt.getAppointmentTime().equals(slotStart))
                            .findFirst()
                            .ifPresent(apt -> slot.setAppointmentId(apt.getId()));
                }
                
                availableSlots.add(slot);
                currentTime = currentTime.plusMinutes(durationMinutes);
            }
        }
        
        return availableSlots;
    }

    @Override
    public List<AvailableSlotDTO> generateAvailableSlotsForDateRange(int doctorId, LocalDate startDate, LocalDate endDate) {
        List<AvailableSlotDTO> allSlots = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            allSlots.addAll(generateAvailableSlots(doctorId, currentDate));
            currentDate = currentDate.plusDays(1);
        }
        
        return allSlots;
    }

    @Override
    public boolean isSlotAvailable(int doctorId, LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        
        // Check if doctor is on leave
        if (doctorLeaveService.isDoctorOnLeave(doctorId, date)) {
            return false;
        }
        
        // Check if time falls within working hours
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<WorkingHour> workingHours = workingHourRepository
                .findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(doctorId, dayOfWeek);
        
        LocalTime time = dateTime.toLocalTime();
        boolean withinWorkingHours = workingHours.stream()
                .filter(wh -> isWorkingHourEffective(wh, date))
                .anyMatch(wh -> !time.isBefore(wh.getStartTime()) && time.isBefore(wh.getEndTime()));
        
        if (!withinWorkingHours) {
            return false;
        }
        
        // Check if slot is already booked
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        int durationMinutes = doctor.getAppointmentDurationMinutes() != null 
                ? doctor.getAppointmentDurationMinutes() 
                : 30;
        
        LocalDateTime slotEnd = dateTime.plusMinutes(durationMinutes);
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, dateTime.minusMinutes(durationMinutes), slotEnd);
        
        return existingAppointments.stream()
                .noneMatch(apt -> isTimeOverlapping(dateTime, slotEnd, apt.getAppointmentTime(), 
                        apt.getAppointmentTime().plusMinutes(durationMinutes)));
    }

    @Override
    @Transactional
    public List<AppointmentDTO> autoGenerateAndCreateSlots(int doctorId, LocalDate date) {
        List<AppointmentDTO> createdSlots = new ArrayList<>();
        
        // Generate available slots
        List<AvailableSlotDTO> availableSlots = generateAvailableSlots(doctorId, date);
        
        // Filter only truly available slots (not occupied)
        List<AvailableSlotDTO> slotsToCreate = availableSlots.stream()
                .filter(AvailableSlotDTO::isAvailable)
                .collect(Collectors.toList());
        
        // Get doctor
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        // Create appointment entities for each available slot
        for (AvailableSlotDTO slotDTO : slotsToCreate) {
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setAppointmentTime(slotDTO.getStartTime());
            appointment.setStatus(AppointmentStatus.AVAILABLE);
            appointment.setReason("Available Appointment Slot");
            
            Appointment saved = appointmentRepository.save(appointment);
            createdSlots.add(convertToDTO(saved));
        }
        
        return createdSlots;
    }
    
    /**
     * Check if a working hour is effective for a given date
     */
    private boolean isWorkingHourEffective(WorkingHour workingHour, LocalDate date) {
        if (workingHour.getEffectiveStartDate() != null && date.isBefore(workingHour.getEffectiveStartDate())) {
            return false;
        }
        if (workingHour.getEffectiveEndDate() != null && date.isAfter(workingHour.getEffectiveEndDate())) {
            return false;
        }
        return true;
    }
    
    /**
     * Check if two time ranges overlap
     * Two appointments overlap only if one starts before the other ends
     * Adjacent appointments (one ends when other starts) do NOT overlap
     */
    private boolean isTimeOverlapping(LocalDateTime start1, LocalDateTime end1, 
                                     LocalDateTime start2, LocalDateTime end2) {
        // Overlap occurs if: start1 < end2 AND end1 > start2
        // This excludes cases where end1 == start2 (adjacent appointments)
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    /**
     * Convert Appointment entity to AppointmentDTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setReason(appointment.getReason());
        dto.setStatus(appointment.getStatus());
        
        Doctor doctor = appointment.getDoctor();
        if (doctor != null) {
            dto.setDoctorId(doctor.getId());
            dto.setDoctorName(doctor.getUser() != null ? doctor.getUser().getName() : "Unknown");
            dto.setDoctorSpecialization(doctor.getSpecialization());
        }
        
        Patient patient = appointment.getPatient();
        if (patient != null) {
            dto.setPatientId(patient.getId());
            dto.setPatientName(patient.getUser() != null ? patient.getUser().getName() : "Unknown");
        }
        
        return dto;
    }
}
