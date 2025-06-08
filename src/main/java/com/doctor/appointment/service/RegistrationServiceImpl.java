package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.model.Receptionist;
import com.doctor.appointment.model.Role;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.DoctorRepository;
import com.doctor.appointment.repository.PatientRepository;
import com.doctor.appointment.repository.ReceptionistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the RegistrationService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReceptionistRepository receptionistRepository;

    /**
     * Register a new user and create the corresponding role-specific entity
     * @param userCreateDTO the user data
     * @return the created user
     */
    @Override
    @Transactional
    public User registerUser(UserCreateDTO userCreateDTO) {
        // Create the user first
        User user = userService.save(userCreateDTO);
        
        // Based on the role, create the corresponding entity
        Role role = Role.valueOf(userCreateDTO.getRole());
        
        switch (role) {
            case DOCTOR:
                createDoctor(user, userCreateDTO);
                break;
            case PATIENT:
                createPatient(user, userCreateDTO);
                break;
            case RECEPTIONIST:
                createReceptionist(user, userCreateDTO);
                break;
            case ADMIN:
                // No additional entity needed for admin
                log.info("Admin user created: {}", user.getUsername());
                break;
            default:
                log.warn("Unknown role: {}", role);
                break;
        }
        
        return user;
    }
    
    /**
     * Create a doctor entity associated with the user
     */
    private void createDoctor(User user, UserCreateDTO userCreateDTO) {
        Doctor doctor = new Doctor();
        doctor.setName(userCreateDTO.getUsername());
        doctor.setEmail(userCreateDTO.getEmail());
        doctor.setUser(user);
        // Default values for other fields
        doctor.setPhone("");
        doctor.setSpecialization("General");
        
        doctorRepository.save(doctor);
        log.info("Doctor created for user: {}", user.getUsername());
    }
    
    /**
     * Create a patient entity associated with the user
     */
    private void createPatient(User user, UserCreateDTO userCreateDTO) {
        Patient patient = new Patient();
        patient.setName(userCreateDTO.getUsername());
        patient.setEmail(userCreateDTO.getEmail());
        patient.setUser(user);
        // Default values for other fields
        patient.setPhone("");
        patient.setAddress("");
        
        patientRepository.save(patient);
        log.info("Patient created for user: {}", user.getUsername());
    }
    
    /**
     * Create a receptionist entity associated with the user
     */
    private void createReceptionist(User user, UserCreateDTO userCreateDTO) {
        Receptionist receptionist = new Receptionist();
        receptionist.setName(userCreateDTO.getUsername());
        receptionist.setEmail(userCreateDTO.getEmail());
        receptionist.setUser(user);
        // Default values for other fields
        receptionist.setPhone("");
        receptionist.setDepartment("General");
        
        receptionistRepository.save(receptionist);
        log.info("Receptionist created for user: {}", user.getUsername());
    }
}
