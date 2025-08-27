-- V5__Add_rating_validation_to_reviews.sql
-- Add rating validation constraint to reviews table
-- Created: 2025-08-27

-- Add check constraint for rating field (1-5 integer values)
ALTER TABLE reviews 
ADD CONSTRAINT chk_rating_range 
CHECK (rating >= 1 AND rating <= 5);