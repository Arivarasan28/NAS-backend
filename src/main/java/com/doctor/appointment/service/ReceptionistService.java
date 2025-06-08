package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.ReceptionistDTO;
import com.doctor.appointment.model.Receptionist;
import com.doctor.appointment.model.User;

import java.util.List;
import java.util.Optional;

public interface ReceptionistService {
    List<ReceptionistDTO> findAll();
    ReceptionistDTO findById(int id);
    Receptionist save(Receptionist receptionist);
    void deleteById(int id);
    ReceptionistDTO update(int id, Receptionist receptionist);
    Optional<Receptionist> findByUser(User user);
    Optional<Receptionist> findByEmail(String email);
}
