package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDTO {
    private int id;
    private String name;
    private String specialization;
    private List<String> specializations; // new, derived from relation
    private String email;
    private String phone;
    private BigDecimal fee;
    private String profilePictureName; // File name of the profile picture
}
