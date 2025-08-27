-- V2__Add_report_tables.sql
-- Report system tables
-- Created: 2025-08-24

-- 신고 테이블
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_user_id BIGINT,
    target_post_id BIGINT,
    target_comment_id BIGINT,
    reason VARCHAR(50) NOT NULL,
    detail TEXT,
    evidence VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    admin_note TEXT,
    processed_at TIMESTAMP NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (target_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_target_user_id (target_user_id),
    INDEX idx_target_post_id (target_post_id),
    INDEX idx_target_comment_id (target_comment_id),
    INDEX idx_status (status),
    INDEX idx_reason (reason),
    INDEX idx_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 제재 테이블
CREATE TABLE IF NOT EXISTS user_penalties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    report_count INT DEFAULT 0,
    penalty_level INT DEFAULT 0,
    restricted_until TIMESTAMP NULL,
    last_report_at TIMESTAMP NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_penalty_level (penalty_level),
    INDEX idx_restricted_until (restricted_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 제재 이력 테이블
CREATE TABLE IF NOT EXISTS user_penalty_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    reason TEXT,
    admin_id BIGINT,
    duration_days INT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;