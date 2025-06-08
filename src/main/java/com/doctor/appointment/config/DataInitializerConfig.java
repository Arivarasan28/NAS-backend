package com.doctor.appointment.config;

import com.doctor.appointment.model.Role;
import com.doctor.appointment.model.User;
import com.doctor.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class to initialize default data when the application starts
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializerConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Initialize default admin user if it doesn't exist
     */
    @Bean
    public CommandLineRunner initializeDefaultAdmin() {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@doctorappointment.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole(Role.ADMIN);
                
                userRepository.save(adminUser);
                log.info("Default admin user created");
            } else {
                log.info("Default admin user already exists");
            }
        };
    }
}
