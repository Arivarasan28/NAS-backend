-- Migration: Add appointment_fee to appointments and backfill from doctors
-- Safe to run multiple times: uses IF NOT EXISTS and null-guarded updates

-- 1) Add column if not exists
ALTER TABLE IF EXISTS appointments
    ADD COLUMN IF NOT EXISTS appointment_fee NUMERIC(10,2);

-- 2) Backfill from doctor's consultation fee where missing
UPDATE appointments a
SET appointment_fee = d.consultation_fee
FROM doctors d
WHERE a.doctor_id = d.id
  AND a.appointment_fee IS NULL;

-- Note: keep nullable to allow legacy creation; service now sets fee from doctor.
