package com.doctor.appointment.controller;

import com.doctor.appointment.config.JwtConfig;
import com.doctor.appointment.model.DTO.AuthRequest;
import com.doctor.appointment.model.DTO.AuthResponse;
import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.Patient;
import com.doctor.appointment.model.Receptionist;
import com.doctor.appointment.model.Role;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.DoctorRepository;
import com.doctor.appointment.repository.PatientRepository;
import com.doctor.appointment.repository.ReceptionistRepository;
import com.doctor.appointment.service.RegistrationService;
import com.doctor.appointment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReceptionistRepository receptionistRepository;
    
    public AuthController(AuthenticationManager authenticationManager, JwtConfig jwtConfig, 
                         UserService userService, RegistrationService registrationService,
                         DoctorRepository doctorRepository, PatientRepository patientRepository,
                         ReceptionistRepository receptionistRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtConfig = jwtConfig;
        this.userService = userService;
        this.registrationService = registrationService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.receptionistRepository = receptionistRepository;
    }

    @Operation(summary = "Login to the system", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials", required = true,
                    content = @Content(schema = @Schema(implementation = AuthRequest.class)))
            @Valid @RequestBody AuthRequest request) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = authentication.getName();
        
        // Get user details
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate token with additional claims
        String token = jwtConfig.generateToken(username, user.getId(), user.getRole().toString());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        
        // Add role-specific IDs
        if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                response.setDoctorId(doctor.getId());
            }
        } else if (user.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                response.setPatientId(patient.getId());
            }
        } else if (user.getRole() == Role.RECEPTIONIST) {
            Receptionist receptionist = receptionistRepository.findByUser(user).orElse(null);
            if (receptionist != null) {
                response.setReceptionistId(receptionist.getId());
            }
        }
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details", required = true,
                    content = @Content(schema = @Schema(implementation = UserCreateDTO.class)))
            @Valid @RequestBody UserCreateDTO userCreateDTO) {
        
        // Use the RegistrationService to create the user and the corresponding role-specific entity
        User savedUser = registrationService.registerUser(userCreateDTO);
        
        // Generate token with additional claims
        String token = jwtConfig.generateToken(savedUser.getUsername(), savedUser.getId(), savedUser.getRole().toString());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRole(savedUser.getRole());
        response.setUserId(savedUser.getId());
        
        // Add role-specific IDs
        if (savedUser.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(savedUser).orElse(null);
            if (doctor != null) {
                response.setDoctorId(doctor.getId());
            }
        } else if (savedUser.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUser(savedUser).orElse(null);
            if (patient != null) {
                response.setPatientId(patient.getId());
            }
        } else if (savedUser.getRole() == Role.RECEPTIONIST) {
            Receptionist receptionist = receptionistRepository.findByUser(savedUser).orElse(null);
            if (receptionist != null) {
                response.setReceptionistId(receptionist.getId());
            }
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
