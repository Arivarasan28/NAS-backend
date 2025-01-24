package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.UserCreateDTO;
import com.doctor.appointment.model.DTO.UserDTO;
import com.doctor.appointment.model.User;
import com.doctor.appointment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public List<UserDTO> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable int userId) {
        UserDTO theUser = userService.findById(userId);

        if (theUser == null) {
            throw new RuntimeException("User id not found: " + userId);
        }

        return theUser;
    }

    @PostMapping("/create")
    public UserDTO addUser(@RequestBody UserCreateDTO theUserCreateDTO) {
        User savedUser = userService.save(theUserCreateDTO);
        return userService.findById(savedUser.getId());
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable int userId) {
        UserDTO tempUser = userService.findById(userId);

        if (tempUser == null) {
            throw new RuntimeException("User id not found: " + userId);
        }

        userService.deleteById(userId);

        return "Deleted user id: " + userId;
    }

    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable int userId, @RequestBody UserCreateDTO userCreateDTO) {
        return userService.update(userId, userCreateDTO);
    }
}
