-- V4__Add_admin_tracking_to_penalty_history.sql
-- Add admin tracking fields to user penalty history
-- Created: 2025-08-26

-- Add admin_id and reason columns to user_penalty_histories table
ALTER TABLE user_penalty_histories 
ADD COLUMN admin_id BIGINT,
ADD COLUMN reason VARCHAR(500);

-- Add foreign key constraint for admin_id
ALTER TABLE user_penalty_histories 
ADD CONSTRAINT fk_penalty_history_admin_id 
FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL;

-- Add index for admin_id for better query performance
CREATE INDEX idx_penalty_history_admin_id ON user_penalty_histories(admin_id);