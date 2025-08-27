-- Add visibility and deletion status fields to posts table
ALTER TABLE posts 
ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Add visibility and deletion status fields to comments table  
ALTER TABLE comments
ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN original_content TEXT;

-- Update existing comments to have is_deleted column as NOT NULL
UPDATE comments SET is_deleted = FALSE WHERE is_deleted IS NULL;
ALTER TABLE comments MODIFY COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;