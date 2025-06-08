package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.User;

/**
 * Service for handling user registration with role-specific entity creation
 */
public interface RegistrationService {
    
    /**
     * Register a new user and create the corresponding role-specific entity
     * @param userCreateDTO the user data
     * @return the created user
     */
    User registerUser(UserCreateDTO userCreateDTO);
}
