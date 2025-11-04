package com.doctor.appointment.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User creation request DTO")
public class UserCreateDTO {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username for the account", example = "john_doe")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "Password for the account", example = "securePassword123")
    private String password;
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|RECEPTIONIST|DOCTOR|PATIENT", message = "Role must be one of: ADMIN, RECEPTIONIST, DOCTOR, PATIENT")
    @Schema(description = "User role", example = "PATIENT", allowableValues = {"ADMIN", "RECEPTIONIST", "DOCTOR", "PATIENT"})
    private String role;
    
    // Common user attributes (added for User-Role architecture)
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;
    
    @Schema(description = "Phone number", example = "1234567890")
    private String phone;
}
