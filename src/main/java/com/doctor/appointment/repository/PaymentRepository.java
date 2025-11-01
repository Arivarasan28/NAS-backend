package com.doctor.appointment.repository;

import com.doctor.appointment.model.Payment;
import com.doctor.appointment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    Optional<Payment> findByAppointmentId(int appointmentId);
    
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    
    @Query("SELECT p FROM Payment p WHERE p.appointment.patient.id = :patientId")
    List<Payment> findByPatientId(@Param("patientId") int patientId);
    
    @Query("SELECT p FROM Payment p WHERE p.appointment.doctor.id = :doctorId")
    List<Payment> findByDoctorId(@Param("doctorId") int doctorId);
    
    Optional<Payment> findByTransactionId(String transactionId);
}
