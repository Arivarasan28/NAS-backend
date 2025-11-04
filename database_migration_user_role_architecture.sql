-- ============================================================================
-- DATABASE MIGRATION: User-Role Architecture Refactoring
-- ============================================================================
-- This script migrates the database to the new architecture where:
-- - User table contains all common attributes (name, phone, profilePictureUrl)
-- - Role-specific tables (doctors, patients, receptionists) contain only
--   role-specific attributes and have a OneToOne relationship with users
-- ============================================================================

-- STEP 1: Add new columns to users table
-- ============================================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture_url VARCHAR(500);

-- STEP 2: Migrate data from doctors to users
-- ============================================================================
-- Copy name, phone, and profile picture from doctors to their associated users
UPDATE users u
SET 
    name = COALESCE(d.name, u.username),
    phone = COALESCE(d.phone, ''),
    profile_picture_url = CASE 
        WHEN d.profile_picture_name IS NOT NULL 
        THEN CONCAT('/uploads/profile_pictures/', d.profile_picture_name)
        ELSE NULL
    END
FROM doctors d
WHERE u.id = d.user_id AND d.user_id IS NOT NULL;

-- STEP 3: Migrate data from patients to users
-- ============================================================================
UPDATE users u
SET 
    name = COALESCE(p.name, u.username),
    phone = COALESCE(p.phone, '')
FROM patients p
WHERE u.id = p.user_id AND p.user_id IS NOT NULL;

-- STEP 4: Migrate data from receptionists to users
-- ============================================================================
UPDATE users u
SET 
    name = COALESCE(r.name, u.username),
    phone = COALESCE(r.phone, '')
FROM receptionists r
WHERE u.id = r.user_id AND r.user_id IS NOT NULL;

-- STEP 5: Set default values for any remaining NULL values
-- ============================================================================
UPDATE users SET name = username WHERE name IS NULL OR name = '';
UPDATE users SET phone = '0000000000' WHERE phone IS NULL OR phone = '';

-- STEP 6: Make the new columns NOT NULL
-- ============================================================================
ALTER TABLE users ALTER COLUMN name SET NOT NULL;
ALTER TABLE users ALTER COLUMN phone SET NOT NULL;

-- STEP 7: Drop old columns from doctors table
-- ============================================================================
ALTER TABLE doctors DROP COLUMN IF EXISTS name;
ALTER TABLE doctors DROP COLUMN IF EXISTS email;
ALTER TABLE doctors DROP COLUMN IF EXISTS phone;
ALTER TABLE doctors DROP COLUMN IF EXISTS profile_picture_name;

-- STEP 8: Drop old columns from patients table
-- ============================================================================
ALTER TABLE patients DROP COLUMN IF EXISTS name;
ALTER TABLE patients DROP COLUMN IF EXISTS email;
ALTER TABLE patients DROP COLUMN IF EXISTS phone;

-- STEP 9: Drop old columns from receptionists table
-- ============================================================================
ALTER TABLE receptionists DROP COLUMN IF EXISTS name;
ALTER TABLE receptionists DROP COLUMN IF EXISTS email;
ALTER TABLE receptionists DROP COLUMN IF EXISTS phone;

-- STEP 10: Add constraints to ensure data integrity
-- ============================================================================
-- Make user_id NOT NULL and UNIQUE in all role tables
ALTER TABLE doctors ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE doctors ADD CONSTRAINT IF NOT EXISTS doctors_user_id_unique UNIQUE (user_id);

ALTER TABLE patients ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE patients ADD CONSTRAINT IF NOT EXISTS patients_user_id_unique UNIQUE (user_id);

ALTER TABLE receptionists ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE receptionists ADD CONSTRAINT IF NOT EXISTS receptionists_user_id_unique UNIQUE (user_id);

-- STEP 11: Create indexes for better performance
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_profile_picture_url ON users(profile_picture_url);

-- STEP 12: Verify migration
-- ============================================================================
-- Check if all doctors have associated users
DO $$
DECLARE
    orphan_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO orphan_count FROM doctors WHERE user_id IS NULL;
    IF orphan_count > 0 THEN
        RAISE WARNING 'Found % doctors without associated users', orphan_count;
    ELSE
        RAISE NOTICE 'All doctors have associated users';
    END IF;
END $$;

-- Check if all patients have associated users
DO $$
DECLARE
    orphan_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO orphan_count FROM patients WHERE user_id IS NULL;
    IF orphan_count > 0 THEN
        RAISE WARNING 'Found % patients without associated users', orphan_count;
    ELSE
        RAISE NOTICE 'All patients have associated users';
    END IF;
END $$;

-- Check if all receptionists have associated users
DO $$
DECLARE
    orphan_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO orphan_count FROM receptionists WHERE user_id IS NULL;
    IF orphan_count > 0 THEN
        RAISE WARNING 'Found % receptionists without associated users', orphan_count;
    ELSE
        RAISE NOTICE 'All receptionists have associated users';
    END IF;
END $$;

-- ============================================================================
-- ROLLBACK SCRIPT (Use only if you need to revert the migration)
-- ============================================================================
/*
-- Add back the columns to role tables
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS profile_picture_name VARCHAR(500);

ALTER TABLE patients ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

ALTER TABLE receptionists ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE receptionists ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE receptionists ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

-- Copy data back from users to doctors
UPDATE doctors d
SET 
    name = u.name,
    email = u.email,
    phone = u.phone,
    profile_picture_name = SUBSTRING(u.profile_picture_url FROM '/uploads/profile_pictures/(.*)$')
FROM users u
WHERE d.user_id = u.id;

-- Copy data back from users to patients
UPDATE patients p
SET 
    name = u.name,
    email = u.email,
    phone = u.phone
FROM users u
WHERE p.user_id = u.id;

-- Copy data back from users to receptionists
UPDATE receptionists r
SET 
    name = u.name,
    email = u.email,
    phone = u.phone
FROM users u
WHERE r.user_id = u.id;

-- Remove the new columns from users table
ALTER TABLE users DROP COLUMN IF EXISTS name;
ALTER TABLE users DROP COLUMN IF EXISTS phone;
ALTER TABLE users DROP COLUMN IF EXISTS profile_picture_url;

-- Remove constraints
ALTER TABLE doctors DROP CONSTRAINT IF EXISTS doctors_user_id_unique;
ALTER TABLE doctors ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE patients DROP CONSTRAINT IF EXISTS patients_user_id_unique;
ALTER TABLE patients ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE receptionists DROP CONSTRAINT IF EXISTS receptionists_user_id_unique;
ALTER TABLE receptionists ALTER COLUMN user_id DROP NOT NULL;
*/
