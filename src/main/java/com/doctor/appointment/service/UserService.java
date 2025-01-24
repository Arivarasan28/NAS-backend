package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.DTO.UserDTO;
import com.doctor.appointment.model.User;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();

    UserDTO findById(int theId);

    User save(UserCreateDTO theUserCreateDTO);

    void deleteById(int theId);

    UserDTO update(int userId, UserCreateDTO userCreateDTO);
}