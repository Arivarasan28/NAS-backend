package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.Appointment;

import java.util.List;

public interface AppointmentService {
    List<AppointmentDTO> findAll();

    AppointmentDTO findById(int theId);

    Appointment save(AppointmentCreateDTO appointmentCreateDTO);

    void deleteById(int theId);

    AppointmentDTO update(int appointmentId, AppointmentCreateDTO appointmentCreateDTO);
}
