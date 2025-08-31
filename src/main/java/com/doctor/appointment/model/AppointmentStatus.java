package com.doctor.appointment.model;

public enum AppointmentStatus {
    AVAILABLE,  // Available slot created by doctor
    BOOKED,     // Booked by patient
    CONFIRMED,  // Confirmed by doctor
    CANCELLED,  // Cancelled by either party
    COMPLETED   // Appointment completed
}
