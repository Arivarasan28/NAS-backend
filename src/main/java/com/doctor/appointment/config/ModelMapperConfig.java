package com.doctor.appointment.config;

import com.doctor.appointment.model.*;
import com.doctor.appointment.model.DTO.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        // DoctorCreateDTO -> Doctor mapping
        modelMapper.addMappings(new PropertyMap<DoctorCreateDTO, Doctor>() {
            @Override
            protected void configure() {
                skip(destination.getId());
            }
        });

        // PatientCreateDTO -> Patient mapping
        modelMapper.addMappings(new PropertyMap<PatientCreateDTO, Patient>() {
            @Override
            protected void configure() {
                skip(destination.getId());
            }
        });

        // UserCreateDTO -> User mapping (Optional)
        modelMapper.addMappings(new PropertyMap<UserCreateDTO, User>() {
            @Override
            protected void configure() {
                skip(destination.getId());
            }
        });

        // AppointmentCreateDTO -> Appointment mapping
        modelMapper.addMappings(new PropertyMap<AppointmentCreateDTO, Appointment>() {
            @Override
            protected void configure() {
                skip(destination.getId());
            }
        });

        return modelMapper;
    }
}
