-- 데이터베이스 선택
USE gubaekpro;

DROP TABLE IF EXISTS
    password_reset_token,
    email_verification,
    chatbot_problem,
    chatbot,
    code_highlight,
    code,
    test_problem,
    test,
    message,
    chatroom,
    team_member,
    team,
    problem,
    user;

SET
FOREIGN_KEY_CHECKS = 1;

-- ✅ 사용자 테이블
CREATE TABLE user
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    email      VARCHAR(255) NOT NULL UNIQUE,
    user_name  VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active  BOOLEAN  DEFAULT TRUE
);

-- ✅ 팀 테이블
CREATE TABLE team
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_name       VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    level           ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    problem_count   INT          NOT NULL,
    start_time      DATETIME     NOT NULL,
    duration_time   INT          NOT NULL,
    current_members INT          NOT NULL CHECK (current_members BETWEEN 1 AND 10),
    leader_id       BIGINT       NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN  DEFAULT TRUE,
    FOREIGN KEY (leader_id) REFERENCES user (id)
);

-- ✅ 팀 멤버 테이블
CREATE TABLE team_member
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    team_id   BIGINT NOT NULL,
    is_leader BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES user (id),
    FOREIGN KEY (team_id) REFERENCES team (id)
);

-- ✅ 문제 테이블
CREATE TABLE problem
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    baek_num     INT          NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL,
    level ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    ex_input     TEXT,
    ex_output    TEXT,
    time_limit   INT,
    memory_limit INT
);

-- ✅ 채팅방 테이블
CREATE TABLE chatroom
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    FOREIGN KEY (team_id) REFERENCES team (id)
);

-- ✅ 메시지 테이블
CREATE TABLE message
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    user_id      BIGINT,
    content      TEXT   NOT NULL,
    send_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chat_room_id) REFERENCES chatroom (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- ✅ 테스트(코딩 테스트) 테이블
CREATE TABLE test
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id    BIGINT UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team (id)
);

-- ✅ 테스트-문제 매핑 테이블
CREATE TABLE test_problem
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_id    BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    FOREIGN KEY (test_id) REFERENCES test (id),
    FOREIGN KEY (problem_id) REFERENCES problem (id)
);

-- ✅ 코드 제출 테이블
CREATE TABLE code
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_id      BIGINT NOT NULL,
    problem_id   BIGINT NOT NULL,
    user_id      BIGINT NOT NULL,
    language     VARCHAR(50),
    submit_code  TEXT   NOT NULL,
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'ABSENT') NOT NULL,
    FOREIGN KEY (test_id) REFERENCES test (id),
    FOREIGN KEY (problem_id) REFERENCES problem (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- ✅ 코드 하이라이트 테이블
CREATE TABLE code_highlight
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    code_id   BIGINT NOT NULL,
    start_pos TEXT   NOT NULL,
    end_pos   TEXT   NOT NULL,
    color     ENUM('red', 'yellow', 'green', 'blue', 'pink', 'orange') DEFAULT 'yellow',
    memo      TEXT,
    is_active BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (code_id) REFERENCES code (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- ✅ 알림봇 테이블
CREATE TABLE chatbot
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id   BIGINT NOT NULL,
    test_date DATE   NOT NULL,
    message   TEXT   NOT NULL,
    send_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES team (id)
);

-- ✅ 알림봇-문제 매핑 테이블
CREATE TABLE chatbot_problem
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    chatbot_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    FOREIGN KEY (chatbot_id) REFERENCES chatbot (id),
    FOREIGN KEY (problem_id) REFERENCES problem (id)
);

-- ✅ 이메일 인증 테이블
CREATE TABLE email_verification
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    code        INT          NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    expired_at  DATETIME     NOT NULL,
    is_verified BOOLEAN  DEFAULT FALSE
);

-- ✅ 비밀번호 초기화 토큰 테이블
CREATE TABLE password_reset_token
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    tempPassword    VARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- 테스트 쿼리 넣기
INSERT INTO gubaekpro.user
(id, email, user_name, password, created_at, is_active)
VALUES(0, 'test123@example.com', 'test123', 'test123@!!', current_timestamp(), 1);