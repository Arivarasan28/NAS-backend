package com.doctor.appointment.repository;

import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Optional<Doctor> findByUser(User user);
    Optional<Doctor> findByUserEmail(String email); // Changed: email is now in User entity
}
