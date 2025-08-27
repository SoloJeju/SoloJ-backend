-- 외래키 문제 해결 스크립트
-- inquiries 테이블이 users를 참조하는데, 실제로는 user 테이블을 사용 중

-- 1. 기존 외래키 제약 삭제
ALTER TABLE inquiries DROP FOREIGN KEY inquiries_ibfk_1;
ALTER TABLE inquiries DROP FOREIGN KEY inquiries_ibfk_2;

-- 2. user 테이블을 참조하도록 외래키 재생성
ALTER TABLE inquiries 
ADD CONSTRAINT fk_inquiries_user 
FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;

ALTER TABLE inquiries 
ADD CONSTRAINT fk_inquiries_admin 
FOREIGN KEY (assigned_admin_id) REFERENCES user(user_id) ON DELETE SET NULL;

-- 3. reports 테이블도 확인 필요
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_1;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_2;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_3;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_4;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_5;

ALTER TABLE reports 
ADD CONSTRAINT fk_reports_reporter 
FOREIGN KEY (reporter_id) REFERENCES user(user_id) ON DELETE CASCADE;

ALTER TABLE reports 
ADD CONSTRAINT fk_reports_target_user 
FOREIGN KEY (target_user_id) REFERENCES user(user_id) ON DELETE CASCADE;

-- 4. user 테이블 active 필드 활성화
UPDATE user SET active = 1 WHERE active = 0 OR active IS NULL;
ALTER TABLE user MODIFY COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;