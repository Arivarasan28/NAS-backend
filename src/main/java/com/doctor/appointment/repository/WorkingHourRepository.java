package com.doctor.appointment.repository;

import com.doctor.appointment.model.WorkingHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface WorkingHourRepository extends JpaRepository<WorkingHour, Long> {
    List<WorkingHour> findByDoctorIdOrderByDayOfWeekAscSequenceAsc(int doctorId);
    List<WorkingHour> findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(int doctorId, DayOfWeek dayOfWeek);
    Optional<WorkingHour> findByIdAndDoctorId(Long id, int doctorId);
}
