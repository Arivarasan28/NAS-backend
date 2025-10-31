package com.doctor.appointment.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorCreateDTO {
    private String username; // username of linked user account (optional on create)
    private String name;
    private String specialization;
    private List<String> specializationNames; // new, for many-to-many
    private String email;
    private String phone;
    private BigDecimal fee;
    private Integer appointmentDurationMinutes; // duration per slot
    private MultipartFile profilePicture; // Uploaded file
}
