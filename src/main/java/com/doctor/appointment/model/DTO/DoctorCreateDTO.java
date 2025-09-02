package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorCreateDTO {
    private String name;
    private String specialization;
    private String email;
    private String phone;
    private BigDecimal fee;
    private MultipartFile profilePicture; // Uploaded file
}
