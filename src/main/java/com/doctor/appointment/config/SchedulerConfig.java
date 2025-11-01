package com.doctor.appointment.config;

import com.doctor.appointment.service.AppointmentReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration for scheduled tasks
 * Automatically cleans up expired reservations every minute
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private AppointmentReservationService reservationService;

    /**
     * Clean up expired reservations every minute
     * This ensures slots are released if payment is not completed within 5 minutes
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds (1 minute)
    public void cleanupExpiredReservations() {
        reservationService.cleanupExpiredReservations();
    }
}
