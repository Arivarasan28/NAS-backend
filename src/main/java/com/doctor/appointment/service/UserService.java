package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.DTO.UserDTO;
import com.doctor.appointment.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    List<UserDTO> findAll();

    UserDTO findById(int theId);

    User save(UserCreateDTO theUserCreateDTO);

    void deleteById(int theId);

    UserDTO update(int userId, UserCreateDTO userCreateDTO);

    Optional<User> findByUsername(String username);
}