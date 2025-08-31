package com.doctor.appointment.service;

import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.DTO.UserDTO;
import com.doctor.appointment.model.Role;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.UserRepository;
import org.modelmapper.ModelMapper;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        if (userCreateDTO.getPassword() != null && !userCreateDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        }
        // Convert String role to Role enum
        if (userCreateDTO.getRole() != null) {
            existingUser.setRole(Role.valueOf(userCreateDTO.getRole()));
        }

        User updatedUser = userRepository.save(existingUser);

        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(authority))
                .build();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}