package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.AppointmentCreateDTO;
import com.doctor.appointment.model.DTO.AppointmentDTO;
import com.doctor.appointment.model.Appointment;
import com.doctor.appointment.repository.AppointmentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<AppointmentDTO> findAll() {
        return appointmentRepository.findAll().stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentDTO findById(int theId) {
        Appointment appointment = appointmentRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + theId));
        return modelMapper.map(appointment, AppointmentDTO.class);
    }

    @Override
    public Appointment save(AppointmentCreateDTO appointmentCreateDTO) {
        Appointment appointment = modelMapper.map(appointmentCreateDTO, Appointment.class);
        return appointmentRepository.save(appointment);
    }

    @Override
    public void deleteById(int theId) {
        appointmentRepository.deleteById(theId);
    }

    @Override
    public AppointmentDTO update(int appointmentId, AppointmentCreateDTO appointmentCreateDTO) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        existingAppointment.setAppointmentTime(appointmentCreateDTO.getAppointmentTime());
        existingAppointment.setReason(appointmentCreateDTO.getReason());

        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

        return modelMapper.map(updatedAppointment, AppointmentDTO.class);
    }
}
