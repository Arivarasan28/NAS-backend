package com.doctor.appointment.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "receptionists")
public class Receptionist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String email;
    private String phone;
    private String department;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
