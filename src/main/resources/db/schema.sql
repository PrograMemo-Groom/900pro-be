-- 데이터베이스 선택
USE mydb;

-- 예제 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    flag INT DEFAULT 1
);

-- 예제 데이터 삽입
INSERT INTO users (user_name, email, password, flag) VALUES
    ('user1', 'user1@example.com', 'password123', 1);
