-- Drop the unique constraint on user_id in refresh_tokens table
ALTER TABLE refresh_tokens DROP INDEX uk_refresh_token_user_id;