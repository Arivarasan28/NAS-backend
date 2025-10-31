# Doctor Leave Management System

## Overview
A comprehensive leave management system for the Doctor Appointment Management System that allows doctors to request leaves, admins to approve/reject them, and automatically blocks appointment slots during approved leave periods.

## Features Implemented

### ✅ Core Features
- **Leave Request Management**: Doctors can request different types of leaves
- **Approval Workflow**: Admin approval/rejection with notes
- **Leave Types**: Sick leave, vacation, emergency, conference, personal, maternity, paternity, and other
- **Leave Status Tracking**: Pending, Approved, Rejected, Cancelled
- **Automatic Slot Blocking**: Prevents appointment booking during approved leave periods
- **Date Validation**: Prevents overlapping leaves and past date leaves
- **Half-Day Leave Support**: Option for half-day leaves
- **Role-Based Access Control**: Different permissions for doctors, admins, and receptionists

## Files Created

### Models
1. **LeaveType.java** - Enum for leave types (8 types)
2. **LeaveStatus.java** - Enum for leave status (4 statuses)
3. **DoctorLeave.java** - Entity for doctor leaves with all fields

### Repository
4. **DoctorLeaveRepository.java** - JPA repository with custom queries for:
   - Finding leaves by doctor
   - Finding leaves by status
   - Checking if doctor is on leave for a specific date
   - Finding overlapping leaves
   - Date range queries

### DTOs
5. **DoctorLeaveDTO.java** - DTO for leave data transfer
6. **DoctorLeaveCreateDTO.java** - DTO for creating leave requests
7. **LeaveApprovalDTO.java** - DTO for approving/rejecting leaves

### Services
8. **DoctorLeaveService.java** - Service interface with 10 methods
9. **DoctorLeaveServiceImpl.java** - Service implementation with business logic

### Controller
10. **DoctorLeaveController.java** - REST controller with 10 endpoints

### Integration
11. **AppointmentServiceImpl.java** - Updated to check doctor availability before:
    - Creating appointment slots
    - Booking appointments

## API Endpoints

### Doctor Endpoints

#### 1. Request Leave
```http
POST /api/doctor-leave/request
Authorization: Bearer {token}
Role: DOCTOR, ADMIN

Request Body:
{
  "doctorId": 1,
  "leaveType": "VACATION",
  "startDate": "2025-01-15",
  "endDate": "2025-01-20",
  "reason": "Family vacation",
  "isHalfDay": false
}

Response: DoctorLeaveDTO
```

#### 2. Get My Leaves
```http
GET /api/doctor-leave/doctor/{doctorId}
Authorization: Bearer {token}
Role: DOCTOR, ADMIN, RECEPTIONIST

Response: List<DoctorLeaveDTO>
```

#### 3. Cancel Leave
```http
PUT /api/doctor-leave/{leaveId}/cancel?doctorId={doctorId}
Authorization: Bearer {token}
Role: DOCTOR, ADMIN

Response: DoctorLeaveDTO
```

### Admin Endpoints

#### 4. Get Pending Leaves
```http
GET /api/doctor-leave/status/PENDING
Authorization: Bearer {token}
Role: ADMIN, RECEPTIONIST

Response: List<DoctorLeaveDTO>
```

#### 5. Approve/Reject Leave
```http
PUT /api/doctor-leave/{leaveId}/approve
Authorization: Bearer {token}
Role: ADMIN

Request Body:
{
  "status": "APPROVED",
  "approvedBy": "admin_username",
  "adminNotes": "Approved for vacation"
}

Response: DoctorLeaveDTO
```

#### 6. Delete Leave
```http
DELETE /api/doctor-leave/{leaveId}
Authorization: Bearer {token}
Role: ADMIN

Response: "Leave deleted successfully"
```

#### 7. Get Leaves by Date Range
```http
GET /api/doctor-leave/range?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {token}
Role: ADMIN, RECEPTIONIST

Response: List<DoctorLeaveDTO>
```

### Public Endpoints

#### 8. Check Doctor Availability
```http
GET /api/doctor-leave/check-availability?doctorId=1&date=2025-01-15
Authorization: None (permitAll)

Response:
{
  "isOnLeave": true,
  "isAvailable": false
}
```

#### 9. Get Doctor's Approved Leaves in Date Range
```http
GET /api/doctor-leave/doctor/{doctorId}/range?startDate=2025-01-01&endDate=2025-01-31
Authorization: None (permitAll)

Response: List<DoctorLeaveDTO>
```

#### 10. Get Leave by ID
```http
GET /api/doctor-leave/{leaveId}
Authorization: Bearer {token}
Role: DOCTOR, ADMIN, RECEPTIONIST

Response: DoctorLeaveDTO
```

## Database Schema

### Table: doctor_leaves
```sql
CREATE TABLE doctor_leaves (
    id BIGSERIAL PRIMARY KEY,
    doctor_id INTEGER NOT NULL REFERENCES doctors(id),
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP,
    admin_notes VARCHAR(500),
    is_half_day BOOLEAN DEFAULT false
);

CREATE INDEX idx_doctor_leaves_doctor_id ON doctor_leaves(doctor_id);
CREATE INDEX idx_doctor_leaves_status ON doctor_leaves(status);
CREATE INDEX idx_doctor_leaves_dates ON doctor_leaves(start_date, end_date);
```

## Business Logic

### Leave Request Validation
1. **Date Validation**: End date must be after or equal to start date
2. **Past Date Check**: Cannot create leaves for past dates
3. **Overlap Prevention**: Cannot create overlapping approved leaves
4. **Doctor Existence**: Doctor must exist in the system

### Leave Approval
1. **Status Validation**: Can only approve/reject PENDING leaves
2. **Status Transition**: Must be APPROVED or REJECTED
3. **Audit Trail**: Records who approved and when

### Leave Cancellation
1. **Ownership Check**: Leave must belong to the requesting doctor
2. **Status Check**: Cannot cancel already cancelled or rejected leaves
3. **Flexible**: Can cancel both pending and approved leaves

### Appointment Integration
1. **Slot Creation**: Blocks slot creation if doctor is on approved leave
2. **Appointment Booking**: Prevents booking if doctor is on approved leave
3. **Real-time Check**: Checks leave status at the time of action

## Usage Examples

### Example 1: Doctor Requests Vacation Leave
```bash
curl -X POST http://localhost:8081/api/doctor-leave/request \
  -H "Authorization: Bearer {doctor_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "leaveType": "VACATION",
    "startDate": "2025-02-01",
    "endDate": "2025-02-07",
    "reason": "Annual family vacation",
    "isHalfDay": false
  }'
```

### Example 2: Admin Approves Leave
```bash
curl -X PUT http://localhost:8081/api/doctor-leave/5/approve \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED",
    "approvedBy": "admin",
    "adminNotes": "Approved. Enjoy your vacation!"
  }'
```

### Example 3: Check Doctor Availability
```bash
curl -X GET "http://localhost:8081/api/doctor-leave/check-availability?doctorId=1&date=2025-02-05"
```

### Example 4: Patient Tries to Book During Leave
```bash
# This will fail with error message
curl -X POST http://localhost:8081/api/appointment/book/123/patient/456 \
  -H "Authorization: Bearer {patient_token}"

# Response:
{
  "error": "Cannot book appointment. Doctor is on leave for the selected date: 2025-02-05"
}
```

## Testing the System

### 1. Start the Application
```bash
cd /Users/arivarasan/Desktop/nas_spring/NAS-backend
./mvnw spring-boot:run
```

### 2. Verify Database Table Creation
The `doctor_leaves` table will be automatically created due to `spring.jpa.hibernate.ddl-auto=update`

### 3. Test Leave Request Flow
1. Doctor creates leave request (status: PENDING)
2. Admin views pending leaves
3. Admin approves/rejects leave
4. Doctor views their leaves
5. Try to create appointment slot during leave (should fail)
6. Try to book appointment during leave (should fail)

### 4. Test Validation
1. Try creating overlapping leaves (should fail)
2. Try creating leave for past dates (should fail)
3. Try approving already approved leave (should fail)

## Error Handling

The system provides clear error messages for:
- Doctor not found
- Invalid date ranges
- Overlapping leaves
- Past date leaves
- Invalid status transitions
- Unauthorized access
- Appointment booking during leave
- Slot creation during leave

## Security

### Role-Based Access Control
- **DOCTOR**: Can request, view own leaves, and cancel own leaves
- **ADMIN**: Full access - approve, reject, delete, view all leaves
- **RECEPTIONIST**: Can view leaves (read-only)
- **PUBLIC**: Can check doctor availability

### Authorization
All endpoints use Spring Security's `@PreAuthorize` annotation for role-based access control.

## Future Enhancements (Optional)

1. **Email Notifications**: Notify doctors when leave is approved/rejected
2. **Automatic Appointment Cancellation**: Cancel existing appointments when leave is approved
3. **Leave Balance Tracking**: Track remaining leave days per doctor
4. **Recurring Leaves**: Support for recurring leave patterns
5. **Leave Calendar View**: Visual calendar showing all doctors' leaves
6. **Substitute Doctor**: Assign substitute doctor during leave
7. **Leave Reports**: Generate leave reports for HR/Admin

## Integration with Frontend

### React Components to Create
1. **DoctorLeaveRequestForm**: Form for doctors to request leave
2. **DoctorLeaveList**: Display doctor's leave history
3. **AdminLeaveApproval**: Admin dashboard to approve/reject leaves
4. **DoctorAvailabilityChecker**: Check doctor availability before booking
5. **LeaveCalendar**: Visual calendar showing doctor leaves

### API Integration Example (React)
```javascript
// Check doctor availability before showing slots
const checkDoctorAvailability = async (doctorId, date) => {
  const response = await axios.get(
    `/api/doctor-leave/check-availability?doctorId=${doctorId}&date=${date}`
  );
  return response.data.isAvailable;
};

// Request leave
const requestLeave = async (leaveData) => {
  const response = await axios.post(
    '/api/doctor-leave/request',
    leaveData,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.data;
};
```

## Troubleshooting

### Issue: Table not created
**Solution**: Check `application.properties` has `spring.jpa.hibernate.ddl-auto=update`

### Issue: Circular dependency error
**Solution**: Already handled - `DoctorLeaveService` is injected into `AppointmentServiceImpl`

### Issue: Leave check not working
**Solution**: Ensure leave status is 'APPROVED' and dates are correct

### Issue: Authorization errors
**Solution**: Check JWT token and user roles

## Summary

✅ **10 new files created**  
✅ **1 file updated** (AppointmentServiceImpl)  
✅ **10 REST endpoints** implemented  
✅ **Full CRUD operations** for leave management  
✅ **Automatic appointment blocking** during leaves  
✅ **Role-based security** implemented  
✅ **Comprehensive validation** and error handling  
✅ **Database schema** auto-created  

The doctor leave management system is now fully integrated with your appointment system and ready for testing!
