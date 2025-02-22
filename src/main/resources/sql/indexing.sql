-- ========================================
-- Indexes for Foreign Key Relationships
-- ========================================

-- Optimize joins between schedules and medications
CREATE INDEX IF NOT EXISTS idx_schedules_medication_id
    ON schedules (medication_id);

-- Optimize joins between intakes and schedules
CREATE INDEX IF NOT EXISTS idx_intakes_schedule_id
    ON intakes (schedule_id);

-- Optimize joins between skip_reasons and intakes
CREATE INDEX IF NOT EXISTS idx_skip_reasons_intake_id
    ON skip_reasons (intake_id);


-- ========================================
-- Partial Index for Frequent Status Queries
-- ========================================

-- Speed up queries that filter for pending intakes
CREATE INDEX IF NOT EXISTS idx_intakes_pending
    ON intakes (scheduled_for)
    WHERE status = 'pending';


-- ========================================
-- Composite Index for Scheduled Time Queries
-- ========================================

-- Optimize fetching user schedules based on scheduled time
CREATE INDEX IF NOT EXISTS idx_schedules_user_time
    ON schedules (user_id, scheduled_time);


-- ========================================
-- Expression Index for Case-Insensitive Medication Name Search
-- ========================================

-- Optimize case-insensitive searches for medication names
CREATE INDEX IF NOT EXISTS idx_medications_lower_name
    ON medications (LOWER(name));


-- ========================================
-- Unique Index for Data Integrity
-- ========================================

-- Ensure that a user doesn't have duplicate schedules for the same medication at the same time
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_medication_schedule
    ON schedules (medication_id, user_id, scheduled_time);


-- ========================================
-- BRIN Index for Large Sequential Data
-- ========================================

-- Optimize range queries over large datasets of intake scheduling
CREATE INDEX IF NOT EXISTS idx_intakes_scheduled_for_brin
    ON intakes USING BRIN (scheduled_for);



