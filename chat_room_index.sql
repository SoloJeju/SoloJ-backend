-- 채팅방 만료 처리 스케줄러용 인덱스 추가
-- is_completed와 join_date에 복합 인덱스 생성하여 쿼리 성능 최적화

-- 기존 인덱스 확인 및 생성
-- 이미 존재하면 중복 생성 방지
CREATE INDEX IF NOT EXISTS idx_chat_rooms_completed_join_date 
ON chat_rooms (is_completed, join_date);

-- 또는 순서를 바꾼 인덱스 (어느 것이 더 효율적인지 확인 후 선택)
-- CREATE INDEX IF NOT EXISTS idx_chat_rooms_join_date_completed 
-- ON chat_rooms (join_date, is_completed);

-- 인덱스 생성 확인
SHOW INDEX FROM chat_rooms WHERE Key_name LIKE '%completed%' OR Key_name LIKE '%join_date%';