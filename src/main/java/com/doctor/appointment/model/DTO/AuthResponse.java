package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Role role;
    private Integer userId;
    private Integer doctorId;
    private Integer patientId;
    private Integer receptionistId;
}
