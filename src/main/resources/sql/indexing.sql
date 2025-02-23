CREATE INDEX IF NOT EXISTS idx_schedules_medication_id
    ON schedules (medication_id);

CREATE INDEX IF NOT EXISTS idx_intakes_schedule_id
    ON intakes (schedule_id);

CREATE INDEX IF NOT EXISTS idx_skip_reasons_intake_id
    ON skip_reasons (intake_id);

CREATE INDEX IF NOT EXISTS idx_intakes_pending
    ON intakes (scheduled_for)
    WHERE status = 'pending';

CREATE INDEX IF NOT EXISTS idx_schedules_user_time
    ON schedules (user_id, scheduled_time);

CREATE INDEX IF NOT EXISTS idx_medications_lower_name
    ON medications (LOWER(name));

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_medication_schedule
    ON schedules (medication_id, user_id, scheduled_time);

CREATE INDEX IF NOT EXISTS idx_intakes_scheduled_for_brin
    ON intakes USING BRIN (scheduled_for);



