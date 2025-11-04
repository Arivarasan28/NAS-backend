package com.doctor.appointment.repository;

import com.doctor.appointment.model.Patient;
import com.doctor.appointment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByUser(User user);
    Optional<Patient> findByUserEmail(String email); // Changed: email is now in User entity
    
    // Find patient by user id using JPQL query
    @Query("SELECT p FROM Patient p WHERE p.user.id = :userId")
    Optional<Patient> findByUserId(@Param("userId") int userId);
}
