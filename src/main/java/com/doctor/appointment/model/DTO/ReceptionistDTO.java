package com.doctor.appointment.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Receptionist data transfer object")
public class ReceptionistDTO {
    
    @Schema(description = "Receptionist ID", example = "1")
    private int id;
    
    // Common user attributes (from User table)
    @Schema(description = "Receptionist name", example = "John Doe")
    private String name;
    
    @Schema(description = "Receptionist email", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Receptionist phone number", example = "1234567890")
    private String phone;
    
    @Schema(description = "Profile picture URL")
    private String profilePictureUrl;
    
    // Receptionist-specific attributes
    @Schema(description = "Receptionist department", example = "General Medicine")
    private String department;
    
    @Schema(description = "User ID associated with this receptionist", example = "5")
    private int userId;
}
