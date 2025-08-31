package com.doctor.appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = true)
    private Patient patient; // Optional for available slots

    private LocalDateTime appointmentTime;
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.BOOKED; // Default for patient bookings


}
