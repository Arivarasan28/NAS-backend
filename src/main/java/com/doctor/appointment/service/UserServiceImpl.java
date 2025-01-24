package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.DTO.UserDTO;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO findById(int theId) {
        User user = userRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("User not found: " + theId));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public User save(UserCreateDTO theUserCreateDTO) {
        User user = modelMapper.map(theUserCreateDTO, User.class);
        return userRepository.save(user);
    }

    @Override
    public void deleteById(int theId) {
        userRepository.deleteById(theId);
    }

    @Override
    @Transactional
    public UserDTO update(int userId, UserCreateDTO userCreateDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        existingUser.setUsername(userCreateDTO.getUsername());
        existingUser.setEmail(userCreateDTO.getEmail());
        existingUser.setPassword(userCreateDTO.getPassword());
        existingUser.setRole(userCreateDTO.getRole());

        User updatedUser = userRepository.save(existingUser);

        return modelMapper.map(updatedUser, UserDTO.class);
    }
}