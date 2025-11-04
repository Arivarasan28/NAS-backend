package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO {
    private int id;
    // Common user attributes (from User table)
    private String name;
    private String email;
    private String phone;
    private String profilePictureUrl;
    // Patient-specific attributes
    private String address;
}
