package com.doctor.appointment.model.DTO;

import com.doctor.appointment.model.PaymentMethod;
import com.doctor.appointment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private int id;
    private int appointmentId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String transactionId;
    private String cardDetails;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String notes;
}
