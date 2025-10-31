package com.doctor.appointment.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String specialization;
    private String email;
    private String phone;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "profile_picture_name")
    private String profilePictureName; // File name of the profile picture
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkingHour> workingHours = new ArrayList<>();

    // New: Many-to-many relationship to support multiple specializations per doctor
    @ManyToMany
    @JoinTable(
        name = "doctor_specializations",
        joinColumns = @JoinColumn(name = "doctor_id"),
        inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private Set<Specialization> specializations = new HashSet<>();

    // Appointment duration in minutes for each slot (default 15 minutes)
    @Column(name = "appointment_duration_minutes")
    private Integer appointmentDurationMinutes = 15;
}
