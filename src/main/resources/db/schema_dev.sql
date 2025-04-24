-- 데이터베이스 선택
USE
mydb;

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
    created_at DATE    DEFAULT CURRENT_TIMESTAMP,
    is_active  BOOLEAN DEFAULT TRUE
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
    created_at      DATE    DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN DEFAULT TRUE,
    is_chat_sent    BOOLEAN DEFAULT FALSE,
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
    level        ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    ex_input     TEXT,
    ex_output    TEXT,
    input_des    TEXT,
    output_des   TEXT,
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
    team_id    BIGINT,
    created_at DATE DEFAULT CURRENT_TIMESTAMP,
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
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_id     BIGINT NOT NULL,
    problem_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    language    VARCHAR(50),
    submit_code TEXT   NOT NULL,
    submit_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    status      ENUM('IN_PROGRESS', 'COMPLETED', 'ABSENT') NOT NULL,
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
    color     ENUM('RED', 'YELLOW', 'GREEN', 'BLUE', 'PINK', 'ORANGE') DEFAULT 'YELLOW',
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
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    tempPassword VARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- 테스트 쿼리 넣기
INSERT INTO mydb.user
    (id, email, user_name, password, created_at, is_active)
VALUES (0, 'test123@example.com', 'test123', 'test123@!!', current_timestamp(), 1);

-- 샘플 데이터 삽입
-- ✅ user
INSERT INTO user (email, user_name, password, is_active)
VALUES ('alice@example.com', 'Alice', 'pw1', TRUE),
       ('bob@example.com', 'Bob', 'pw2', TRUE),
       ('charlie@example.com', 'Charlie', 'pw3', TRUE);

-- ✅ team
INSERT INTO team (team_name, description, level, problem_count, start_time, duration_time, current_members, leader_id,
                  is_active)
VALUES ('Team Alpha', 'First team', 'EASY', 2, NOW(), 60, 3, 1, TRUE),
       ('Team Beta', 'Second team', 'MEDIUM', 3, NOW(), 90, 2, 2, TRUE);

-- ✅ team_member
INSERT INTO team_member (user_id, team_id, is_leader)
VALUES (1, 1, TRUE),
       (2, 1, FALSE),
       (3, 1, FALSE),
       (2, 2, TRUE),
       (3, 2, FALSE);

-- ✅ problem
INSERT INTO problem (baek_num, title, description, level, ex_input, ex_output, time_limit, memory_limit)
VALUES (1000, 'A + B', 'Add two numbers', 'EASY', '1 2', '3', 1, 128),
       (1001, 'A - B', 'Subtract two numbers', 'EASY', '3 1', '2', 1, 128),
       (1002, 'Multiply', 'Multiply numbers', 'MEDIUM', '2 3', '6', 2, 256),
       (1002, 'Multiply', 'Multiply numbers', 'MEDIUM', '2 3', '6', 2, 256),
       (1002, 'Multiply', 'Multiply numbers', 'MEDIUM', '2 3', '6', 2, 256),
       (1002, 'Multiply', 'Multiply numbers', 'MEDIUM', '2 3', '6', 2, 256),
       (1002, 'Multiply', 'Multiply numbers', 'MEDIUM', '2 3', '6', 2, 256);

-- ✅ chatroom
INSERT INTO chatroom (team_id)
VALUES (1),
       (2);

-- ✅ message
INSERT INTO message (chat_room_id, user_id, content)
VALUES (1, 1, 'Hello team!'),
       (1, 2, 'Hi Alice!'),
       (2, 2, 'Let’s start the test.');

-- ✅ test
INSERT INTO test (team_id)
VALUES (1),
       (2);

-- ✅ test_problem
INSERT INTO test_problem (test_id, problem_id)
VALUES (1, 1),
       (1, 2),
       (2, 2),
       (2, 3);

-- ✅ code
INSERT INTO code (test_id, problem_id, user_id, language, submit_code, status)
VALUES (1, 1, 1, 'Java', 'public class A {}', 'COMPLETED'),
       (1, 2, 1, 'Java', 'public class A {}', 'COMPLETED'),
       (1, 3, 1, 'Java', 'public class A {}', 'ABSENT'),
       (1, 2, 2, 'Python', 'print(1-2)', 'IN_PROGRESS'),
       (2, 3, 3, 'C++', 'int main() {}', 'ABSENT');

-- ✅ code_highlight
INSERT INTO code_highlight (user_id, code_id, start_pos, end_pos, color, memo)
VALUES (2, 1, '3:5', '10:20', 'BLUE', 'sub logic'),
       (1, 1, '1:1', '1:10', 'YELLOW', 'main logic'),
       (2, 2, '2:5', '2:15', 'RED', 'bug here'),
       (1, 1, '2:5', '2:15', 'RED', 'bug here'),
       (1, 1, '2:5', '2:15', 'RED', 'bug here');


-- ✅ chatbot
INSERT INTO chatbot (team_id, test_date, message)
VALUES (1, CURDATE(), 'Test will begin shortly'),
       (2, CURDATE(), 'Reminder: test today');

-- ✅ chatbot_problem
INSERT INTO chatbot_problem (chatbot_id, problem_id)
VALUES (1, 1),
       (1, 2),
       (2, 3);

-- ✅ email_verification
INSERT INTO email_verification (email, code, expired_at)
VALUES ('alice@example.com', 123456, NOW() + INTERVAL 5 MINUTE),
       ('bob@example.com', 654321, NOW() + INTERVAL 10 MINUTE);

-- ✅ password_reset_token
INSERT INTO password_reset_token (user_id, tempPassword)
VALUES (1, 'tempPw123!'),
       (2, 'resetMe456!');