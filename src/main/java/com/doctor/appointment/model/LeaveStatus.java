package com.doctor.appointment.model;

/**
 * Enum representing the status of a leave request
 */
public enum LeaveStatus {
    PENDING,    // Leave request submitted, awaiting approval
    APPROVED,   // Leave approved by admin
    REJECTED,   // Leave rejected
    CANCELLED   // Leave cancelled by doctor
}
