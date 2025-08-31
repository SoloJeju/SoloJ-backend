-- ShedLock용 테이블 생성
-- 분산 환경에서 스케줄러 중복 실행 방지를 위한 락 테이블

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL, 
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), 
    locked_by VARCHAR(255) NOT NULL, 
    PRIMARY KEY (name)
);

-- 인덱스는 PRIMARY KEY로 자동 생성됨
-- 테이블 생성 확인
DESCRIBE shedlock;