-- 즉시 실행 가능한 SQL 스크립트
-- MySQL 콘솔이나 워크벤치에서 직접 실행

-- 1단계: inquiries 테이블의 외래키 제약 확인
SHOW CREATE TABLE inquiries;

-- 2단계: 외래키 제약 삭제 (에러 무시하고 계속 진행)
SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE inquiries DROP FOREIGN KEY IF EXISTS inquiries_ibfk_1;
ALTER TABLE inquiries DROP FOREIGN KEY IF EXISTS inquiries_ibfk_2;
ALTER TABLE inquiries DROP FOREIGN KEY IF EXISTS fk_inquiries_user;
ALTER TABLE inquiries DROP FOREIGN KEY IF EXISTS fk_inquiries_admin;

-- 3단계: user 테이블을 참조하는 새 외래키 생성
ALTER TABLE inquiries 
ADD CONSTRAINT fk_inquiries_user_new 
FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;

ALTER TABLE inquiries 
ADD CONSTRAINT fk_inquiries_admin_new 
FOREIGN KEY (assigned_admin_id) REFERENCES user(user_id) ON DELETE SET NULL;

-- 4단계: reports 테이블도 동일하게 처리
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_1;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_2;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_3;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_4;
ALTER TABLE reports DROP FOREIGN KEY IF EXISTS reports_ibfk_5;

ALTER TABLE reports 
ADD CONSTRAINT fk_reports_reporter_new 
FOREIGN KEY (reporter_id) REFERENCES user(user_id) ON DELETE CASCADE;

ALTER TABLE reports 
ADD CONSTRAINT fk_reports_target_new 
FOREIGN KEY (target_user_id) REFERENCES user(user_id) ON DELETE CASCADE;

-- 5단계: 모든 사용자 활성화
UPDATE user SET active = 1;

-- 6단계: 외래키 체크 다시 활성화
SET FOREIGN_KEY_CHECKS=1;

-- 확인
SELECT user_id, email, role, active FROM user;
SHOW CREATE TABLE inquiries;
SHOW CREATE TABLE reports;