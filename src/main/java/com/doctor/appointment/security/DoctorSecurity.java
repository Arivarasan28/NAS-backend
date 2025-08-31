package com.doctor.appointment.security;

import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security component for doctor-related authorization checks
 */
@Component("doctorSecurity")
public class DoctorSecurity {

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Checks if the currently authenticated user is the doctor with the given ID
     * @param doctorId The doctor ID to check
     * @return true if the current user is the doctor with the given ID
     */
    public boolean isDoctor(int doctorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Get the username from the authenticated user
        String username = authentication.getName();
        
        // Find the doctor by ID
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return false;
        }
        
        // Check if the doctor's username matches the authenticated user's username
        Doctor doctor = doctorOpt.get();
        return doctor.getUser() != null && doctor.getUser().getUsername().equals(username);
    }
}
