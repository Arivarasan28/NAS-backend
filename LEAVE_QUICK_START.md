# Doctor Leave Management - Quick Start Guide

## 🚀 Getting Started

### 1. Start the Application
```bash
cd /Users/arivarasan/Desktop/nas_spring/NAS-backend
./mvnw spring-boot:run
```

### 2. Verify Database Table
The `doctor_leaves` table will be automatically created. Check your PostgreSQL database:
```sql
\dt doctor_leaves
SELECT * FROM doctor_leaves;
```

### 3. Test the API
Run the test script:
```bash
./test_leave_api.sh
```

## 📋 Common Use Cases

### Use Case 1: Doctor Requests Vacation Leave

**Step 1: Login as Doctor**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor_username",
    "password": "password"
  }'
```
Save the JWT token from the response.

**Step 2: Request Leave**
```bash
curl -X POST http://localhost:8081/api/doctor-leave/request \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "leaveType": "VACATION",
    "startDate": "2025-02-01",
    "endDate": "2025-02-07",
    "reason": "Family vacation to Hawaii",
    "isHalfDay": false
  }'
```

**Step 3: View My Leaves**
```bash
curl -X GET http://localhost:8081/api/doctor-leave/doctor/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Use Case 2: Admin Approves Leave

**Step 1: Login as Admin**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_username",
    "password": "password"
  }'
```

**Step 2: View Pending Leaves**
```bash
curl -X GET http://localhost:8081/api/doctor-leave/status/PENDING \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Step 3: Approve Leave**
```bash
curl -X PUT http://localhost:8081/api/doctor-leave/1/approve \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED",
    "approvedBy": "admin",
    "adminNotes": "Approved. Have a great vacation!"
  }'
```

### Use Case 3: Patient Checks Doctor Availability

**No authentication required:**
```bash
curl -X GET "http://localhost:8081/api/doctor-leave/check-availability?doctorId=1&date=2025-02-05"
```

**Response:**
```json
{
  "isOnLeave": true,
  "isAvailable": false
}
```

### Use Case 4: Try to Book Appointment During Leave

**This will fail with an error:**
```bash
curl -X POST http://localhost:8081/api/appointment/book/123/patient/456 \
  -H "Authorization: Bearer PATIENT_JWT_TOKEN"
```

**Error Response:**
```json
{
  "error": "Cannot book appointment. Doctor is on leave for the selected date: 2025-02-05"
}
```

## 🔑 Leave Types

- `SICK_LEAVE` - Medical/sick leave
- `VACATION` - Planned vacation
- `EMERGENCY` - Emergency leave
- `CONFERENCE` - Professional conference/training
- `PERSONAL` - Personal leave
- `MATERNITY` - Maternity leave
- `PATERNITY` - Paternity leave
- `OTHER` - Other types of leave

## 📊 Leave Status Flow

```
PENDING → APPROVED ✓
        → REJECTED ✗
        → CANCELLED (by doctor)

APPROVED → CANCELLED (by doctor)
```

## 🛡️ Role Permissions

| Action | DOCTOR | ADMIN | RECEPTIONIST | PATIENT/PUBLIC |
|--------|--------|-------|--------------|----------------|
| Request Leave | ✓ | ✓ | ✗ | ✗ |
| View Own Leaves | ✓ | ✓ | ✓ | ✗ |
| View All Leaves | ✗ | ✓ | ✓ | ✗ |
| Approve/Reject | ✗ | ✓ | ✗ | ✗ |
| Cancel Own Leave | ✓ | ✓ | ✗ | ✗ |
| Delete Leave | ✗ | ✓ | ✗ | ✗ |
| Check Availability | ✓ | ✓ | ✓ | ✓ |

## 🧪 Testing Checklist

- [ ] Doctor can request leave
- [ ] Cannot create overlapping leaves
- [ ] Cannot create leave for past dates
- [ ] Admin can view pending leaves
- [ ] Admin can approve leave
- [ ] Admin can reject leave
- [ ] Doctor can cancel leave
- [ ] Cannot create appointment slots during leave
- [ ] Cannot book appointments during leave
- [ ] Public can check doctor availability
- [ ] Proper error messages are shown

## 🐛 Troubleshooting

### Problem: "Doctor not found"
**Solution:** Ensure the doctor exists in the database with the correct ID.

### Problem: "There is already an approved leave for this period"
**Solution:** Check existing leaves and choose different dates.

### Problem: "Cannot create leave for past dates"
**Solution:** Use future dates for leave requests.

### Problem: "Can only approve/reject pending leave requests"
**Solution:** Leave must be in PENDING status to approve/reject.

### Problem: 401 Unauthorized
**Solution:** Ensure you're using a valid JWT token in the Authorization header.

### Problem: 403 Forbidden
**Solution:** Check that your user has the correct role for the operation.

## 📱 Frontend Integration Tips

### React Component Example
```javascript
import { useState, useEffect } from 'react';
import axios from 'axios';

function DoctorLeaveRequest({ doctorId, token }) {
  const [leaveData, setLeaveData] = useState({
    doctorId: doctorId,
    leaveType: 'VACATION',
    startDate: '',
    endDate: '',
    reason: '',
    isHalfDay: false
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(
        '/api/doctor-leave/request',
        leaveData,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert('Leave request submitted successfully!');
    } catch (error) {
      alert('Error: ' + error.response.data.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <select 
        value={leaveData.leaveType}
        onChange={(e) => setLeaveData({...leaveData, leaveType: e.target.value})}
      >
        <option value="VACATION">Vacation</option>
        <option value="SICK_LEAVE">Sick Leave</option>
        <option value="EMERGENCY">Emergency</option>
        {/* ... other options */}
      </select>
      
      <input
        type="date"
        value={leaveData.startDate}
        onChange={(e) => setLeaveData({...leaveData, startDate: e.target.value})}
      />
      
      <input
        type="date"
        value={leaveData.endDate}
        onChange={(e) => setLeaveData({...leaveData, endDate: e.target.value})}
      />
      
      <textarea
        value={leaveData.reason}
        onChange={(e) => setLeaveData({...leaveData, reason: e.target.value})}
        placeholder="Reason for leave"
      />
      
      <button type="submit">Submit Leave Request</button>
    </form>
  );
}
```

### Check Availability Before Showing Slots
```javascript
async function loadAvailableSlots(doctorId, date) {
  // First check if doctor is on leave
  const availabilityResponse = await axios.get(
    `/api/doctor-leave/check-availability?doctorId=${doctorId}&date=${date}`
  );
  
  if (availabilityResponse.data.isOnLeave) {
    return {
      slots: [],
      message: 'Doctor is on leave for this date'
    };
  }
  
  // If available, fetch appointment slots
  const slotsResponse = await axios.get(
    `/api/appointment/available-slots?doctorId=${doctorId}&date=${date}`
  );
  
  return {
    slots: slotsResponse.data,
    message: null
  };
}
```

## 📚 Additional Resources

- **Full Documentation:** See `LEAVE_MANAGEMENT_README.md`
- **API Testing:** Run `./test_leave_api.sh`
- **Database Schema:** Check the README for complete schema

## ✅ Success Indicators

Your leave management system is working correctly if:

1. ✓ Doctors can request leaves
2. ✓ Admins can approve/reject leaves
3. ✓ System prevents overlapping leaves
4. ✓ Appointment booking is blocked during approved leaves
5. ✓ Slot creation is blocked during approved leaves
6. ✓ Public can check doctor availability
7. ✓ All role-based permissions work correctly

## 🎉 You're All Set!

The doctor leave management system is now fully functional and integrated with your appointment system. Start testing and enjoy the new feature!

For questions or issues, refer to the main documentation or check the error messages for guidance.
