package com.doctor.appointment.repository;

import com.doctor.appointment.model.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Integer> {
    List<AppointmentStatusHistory> findByAppointmentIdOrderByChangedAtAsc(int appointmentId);
}
