-- V4__Add_content_status_fields.sql
-- posts: is_visible, is_deleted
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'posts'
    AND COLUMN_NAME = 'is_visible'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE posts ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'posts'
    AND COLUMN_NAME = 'is_deleted'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE posts ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- comments: is_visible, original_content
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'comments'
    AND COLUMN_NAME = 'is_visible'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE comments ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'comments'
    AND COLUMN_NAME = 'original_content'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE comments ADD COLUMN original_content TEXT',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- comments.is_deleted → NOT NULL 보정
UPDATE comments SET is_deleted = FALSE WHERE is_deleted IS NULL;
ALTER TABLE comments MODIFY COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
