package com.doctor.appointment.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "profile_picture_name")
    private String profilePictureName; // File name of the profile picture
}
