package com.doctor.appointment.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new receptionist
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Receptionist creation request DTO")
public class ReceptionistCreateDTO {
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Receptionist name", example = "John Doe")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Schema(description = "Phone number", example = "1234567890")
    private String phone;
    
    @NotBlank(message = "Department is required")
    @Schema(description = "Department", example = "General Medicine")
    private String department;
    
    @Schema(description = "User ID to associate with this receptionist", example = "5")
    private Integer userId;
}
