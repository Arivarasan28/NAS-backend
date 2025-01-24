package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDTO {
    private int id;
    private String name;
    private String specialization;
    private String email;
    private String phone;
    private String profilePictureName; // File name of the profile picture
}
