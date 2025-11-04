package com.doctor.appointment.repository;

import com.doctor.appointment.model.Receptionist;
import com.doctor.appointment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Integer> {
    Optional<Receptionist> findByUser(User user);
    Optional<Receptionist> findByUserEmail(String email); // Changed: email is now in User entity
}
