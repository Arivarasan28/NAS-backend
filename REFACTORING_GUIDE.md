# Backend Refactoring Guide - User-Role Architecture

## Overview
Refactored the backend to follow modern best practices where:
- **User table**: Contains common attributes (username, email, password, role, name, phone, profilePictureUrl)
- **Role-specific tables** (Doctor, Patient, Receptionist): Contain only role-specific attributes with OneToOne relationship to User

## Changes Made

### 1. Entity Models

#### User.java
```java
- Added: name (String, required)
- Added: phone (String, required)
- Added: profilePictureUrl (String, optional)
```

#### Doctor.java
```java
- Removed: name, email, phone, profilePictureName
- Kept: specialization (legacy), fee, appointmentDurationMinutes
- Updated: @OneToOne relationship to User (unique=true, nullable=false)
- Kept: specializations (Many-to-Many), workingHours (One-to-Many)
```

#### Patient.java
```java
- Removed: name, email, phone
- Kept: address
- Updated: @OneToOne relationship to User (unique=true, nullable=false)
```

#### Receptionist.java
```java
- Removed: name, email, phone
- Kept: department
- Updated: @OneToOne relationship to User (unique=true, nullable=false)
```

### 2. DTOs Updated
- DoctorDTO: Changed profilePictureName → profilePictureUrl
- PatientDTO: Added profilePictureUrl
- ReceptionistDTO: Added profilePictureUrl

### 3. Services to Update

#### Pattern for accessing common attributes:
```java
// OLD (direct access - NO LONGER WORKS)
doctor.getName()
doctor.setEmail(email)

// NEW (through User relationship)
doctor.getUser().getName()
doctor.getUser().setEmail(email)
```

#### Key Services Requiring Updates:
1. **DoctorServiceImpl** - Update all name/email/phone/profilePicture access
2. **PatientServiceImpl** - Update all name/email/phone access
3. **ReceptionistServiceImpl** - Update all name/email/phone access
4. **RegistrationServiceImpl** - Update user creation logic
5. **AppointmentServiceImpl** - Update doctor/patient name access
6. **AppointmentSlotGenerationServiceImpl** - Update doctor/patient name access
7. **DoctorLeaveServiceImpl** - Update doctor name access

### 4. Database Migration Required

```sql
-- Add new columns to users table
ALTER TABLE users ADD COLUMN name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN phone VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN profile_picture_url VARCHAR(500);

-- Migrate data from doctors to users
UPDATE users u
SET name = d.name, 
    phone = d.phone,
    profile_picture_url = CONCAT('/uploads/profile_pictures/', d.profile_picture_name)
FROM doctors d
WHERE u.id = d.user_id AND d.user_id IS NOT NULL;

-- Migrate data from patients to users
UPDATE users u
SET name = p.name,
    phone = p.phone
FROM patients p
WHERE u.id = p.user_id AND p.user_id IS NOT NULL;

-- Migrate data from receptionists to users
UPDATE users u
SET name = r.name,
    phone = r.phone
FROM receptionists r
WHERE u.id = r.user_id AND r.user_id IS NOT NULL;

-- Drop old columns from role tables
ALTER TABLE doctors DROP COLUMN name;
ALTER TABLE doctors DROP COLUMN email;
ALTER TABLE doctors DROP COLUMN phone;
ALTER TABLE doctors DROP COLUMN profile_picture_name;
ALTER TABLE doctors ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE doctors ADD CONSTRAINT doctors_user_id_unique UNIQUE (user_id);

ALTER TABLE patients DROP COLUMN name;
ALTER TABLE patients DROP COLUMN email;
ALTER TABLE patients DROP COLUMN phone;
ALTER TABLE patients ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE patients ADD CONSTRAINT patients_user_id_unique UNIQUE (user_id);

ALTER TABLE receptionists DROP COLUMN name;
ALTER TABLE receptionists DROP COLUMN email;
ALTER TABLE receptionists DROP COLUMN phone;
ALTER TABLE receptionists ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE receptionists ADD CONSTRAINT receptionists_user_id_unique UNIQUE (user_id);
```

## Benefits of This Architecture

1. **Single Source of Truth**: Common attributes stored once in User table
2. **Data Consistency**: No duplicate/conflicting data across tables
3. **Easier Maintenance**: Update user info in one place
4. **Better Security**: Centralized authentication attributes
5. **Scalability**: Easy to add new roles without duplicating common fields
6. **Modern Best Practice**: Follows DRY (Don't Repeat Yourself) principle

## Next Steps

1. ✅ Update entity models
2. ✅ Update DTOs
3. ⏳ Update all service implementations
4. ⏳ Test all API endpoints
5. ⏳ Run database migration
6. ⏳ Update frontend to handle new structure
7. ⏳ Test end-to-end flows
