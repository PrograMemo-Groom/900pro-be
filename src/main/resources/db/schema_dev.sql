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
    is_active  BOOLEAN DEFAULT TRUE,
    is_coding   BOOLEAN DEFAULT FALSE
);

-- ✅ 팀 테이블
CREATE TABLE team
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_name       VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    level           ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    problem_count   INT          NOT NULL,
    start_time      TEXT     NOT NULL,
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
    test_date_time DATETIME   NOT NULL,
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
VALUES ('Team Alpha', 'First team', 'EASY', 2, "15:00", 60, 3, 1, TRUE),
       ('Team Beta', 'Second team', 'MEDIUM', 3, "16:00", 90, 2, 2, TRUE);

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
INSERT INTO chatbot (team_id, test_date_time, message)
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

-- id = 1 (유기농 배추)
UPDATE problem
SET
    baek_num = 1012,
    title = '유기농 배추',
    description = '차세대 영농인 한나는 강원도 고랭지에서 유기농 배추를 재배하기로 하였다. 농약을 쓰지 않고 배추를 재배하려면 배추를 해충으로부터 보호하는 것이 중요하기 때문에, 한나는 해충 방지에 효과적인 배추흰지렁이를 구입하기로 결심한다. 이 지렁이는 배추근처에 서식하며 해충을 잡아 먹음으로써 배추를 보호한다. 특히, 어떤 배추에 배추흰지렁이가 한 마리라도 살고 있으면 이 지렁이는 인접한 다른 배추로 이동할 수 있어, 그 배추들 역시 해충으로부터 보호받을 수 있다. 한 배추의 상하좌우 네 방향에 다른 배추가 위치한 경우에 서로 인접해있는 것이다. 한나가 배추를 재배하는 땅은 고르지 못해서 배추를 군데군데 심어 놓았다. 배추들이 모여있는 곳에는 배추흰지렁이가 한 마리만 있으면 되므로 서로 인접해있는 배추들이 몇 군데에 퍼져있는지 조사하면 총 몇 마리의 지렁이가 필요한지 알 수 있다.',
    level = 'EASY',
    input_des = '입력의 첫 줄에는 테스트 케이스의 개수 T가 주어진다. 그 다음 줄부터 각각의 테스트 케이스에 대해 첫째 줄에는 배추를 심은 배추밭의 가로길이 M(1 ≤ M ≤ 50)과 세로길이 N(1 ≤ N ≤ 50), 그리고 배추가 심어져 있는 위치의 개수 K(1 ≤ K ≤ 2500)이 주어진다. 그 다음 K줄에는 배추의 위치 X(0 ≤ X ≤ M-1), Y(0 ≤ Y ≤ N-1)가 주어진다. 두 배추의 위치가 같은 경우는 없다.',
    output_des = '각 테스트 케이스에 대해 필요한 최소의 배추흰지렁이 마리 수를 출력한다.',
    ex_input = '2\n10 8 17\n0 0\n1 0\n1 1',
    ex_output = '5 1',
    time_limit = 1,
    memory_limit = 128
WHERE id = 1;

-- id = 2 (상근이의 여행)
UPDATE problem
SET
    baek_num = 9372,
    title = '상근이의 여행',
    description = '상근이는 겨울방학을 맞아 N개국을 여행하면서 자아를 찾기로 마음먹었다. 하지만 상근이는 새로운 비행기를 무서워하기 때문에, 최대한 적은 종류의 비행기를 타고 국가들을 이동하려고 한다. 이번 방학 동안의 비행 스케줄이 주어졌을 때, 상근이가 가장 적은 종류의 비행기를 타고 모든 국가들을 여행할 수 있도록 도와주자. 상근이가 한 국가에서 다른 국가로 이동할 때 다른 국가를 거쳐 가도(심지어 이미 방문한 국가라도) 된다.',
    level = 'EASY',
    input_des = '첫 번째 줄에는 테스트 케이스의 수 T(T ≤ 100)가 주어지고, 각 테스트 케이스마다 다음과 같은 정보가 주어진다. 1. 첫 번째 줄에는 국가의 수 N(2 ≤ N ≤ 1 000)과 비행기의 종류 M(1 ≤ M ≤ 10 000)가 주어진다. 2. 이후 M개의 줄에 a와 b 쌍들이 입력된다. a와 b를 왕복하는 비행기가 있다는 것을 의미한다. (1 ≤ a, b ≤ n; a ≠ b) 3. 주어지는 비행 스케줄은 항상 연결 그래프를 이룬다.',
    output_des = '테스트 케이스마다 한 줄을 출력한다. 상근이가 모든 국가를 여행하기 위해 타야 하는 비행기 종류의 최소 개수를 출력한다.',
    ex_input = '3 1',
    ex_output = '2',
    time_limit = 1,
    memory_limit = 128
WHERE id = 2;

-- id = 3 (세탁소 사장 동혁)
UPDATE problem
SET
    baek_num = 2720,
    title = '세탁소 사장 동혁',
    description = '미국으로 유학간 동혁이는 세탁소를 운영하고 있다. 동혁이는 최근에 아르바이트로 고등학생 리암을 채용했다. 동혁이는 리암에게 실망했다. 리암은 거스름돈을 주는 것을 자꾸 실수한다. 심지어 $0.5달러를 줘야하는 경우에 거스름돈으로 $5달러를 주는것이다! 어쩔수 없이 뛰어난 코딩 실력을 발휘해 리암을 도와주는 프로그램을 작성하려고 하지만, 디아블로를 하느라 코딩할 시간이 없어서 이 문제를 읽고 있는 여러분이 대신 해주어야 한다. 거스름돈의 액수가 주어지면 리암이 줘야할 쿼터(Quarter, $0.25)의 개수, 다임(Dime, $0.10)의 개수, 니켈(Nickel, $0.05)의 개수, 페니(Penny, $0.01)의 개수를 구하는 프로그램을 작성하시오. 거스름돈은 항상 $5.00 이하이고, 손님이 받는 동전의 개수를 최소로 하려고 한다. 예를 들어, $1.24를 거슬러 주어야 한다면, 손님은 4쿼터, 2다임, 0니켈, 4페니를 받게 된다.',
    level = 'MEDIUM',
    input_des = '첫째 줄에 테스트 케이스의 개수 T가 주어진다. 각 테스트 케이스는 거스름돈 C를 나타내는 정수 하나로 이루어져 있다. C의 단위는 센트이다. (1달러 = 100센트) (1<=C<=500)',
    output_des = '각 테스트케이스에 대해 필요한 쿼터의 개수, 다임의 개수, 니켈의 개수, 페니의 개수를 공백으로 구분하여 출력한다.',
    ex_input = '3\n124\n25\n194',
    ex_output = '4 2 0 4\n1 0 0 0\n7 1 1 4',
    time_limit = 1,
    memory_limit = 128
WHERE id = 3;

-- id = 4 (너구리 구구)
UPDATE problem
SET
    baek_num = 18126,
    title = '너구리 구구',
    description = '텔레토비 동산에 사는 너구리 구구는 입구, 거실, 주방, 안방, 공부방, 운동실, 음악실, 음식 창고 등 N개의 방을 가지고 있다. 입구를 포함한 모든 방은 1부터 N까지의 번호가 있고, 입구는 1번이다. 구구의 집으로 들어가는 입구는 한 개이며 입구과 모든 방들은 총 N-1개의 길로 서로 오고 갈 수 있다. 구구는 스머프 동산에서 멜론아 아이스크림을 발견했다. 구구는 무더운 여름 햇살을 피해 최대한 입구에서 먼 방에 아이스크림을 숨기려고 한다. 구구가 집 입구에서 멜론아 아이스크림을 숨기려고 하는 방까지 이동하는 거리를 구하여라.',
    level = 'MEDIUM',
    input_des = '첫째 줄에 정수 N(1 ≤ N ≤ 5,000)이 주어진다. 다음 N-1개의 줄에 구구의 집의 모든 길의 정보가 정수 A, B, C(1 ≤ A, B ≤ N, 1 ≤ C ≤ 1,000,000,000)로 주어진다. A번 방과 B번 방 사이를 양방향으로 연결하는 길의 길이가 C임을 의미한다.',
    output_des = '구구가 집 입구에서 멜론아 아이스크림을 숨기려고 하는 방까지 이동하는 거리를 구하여라.',
    ex_input = '4\n1 2 3\n2 3 2\n2 4 4',
    ex_output = '7',
    time_limit = 1,
    memory_limit = 128
WHERE id = 4;

-- id = 5 (촌수계산)
UPDATE problem
SET
    baek_num = 2644,
    title = '촌수계산',
    description = '우리 나라는 가족 혹은 친척들 사이의 관계를 촌수라는 단위로 표현하는 독특한 문화를 가지고 있다. 이러한 촌수는 다음과 같은 방식으로 계산된다. 기본적으로 부모와 자식 사이를 1촌으로 정의하고 이로부터 사람들 간의 촌수를 계산한다. 예를 들면 나와 아버지, 아버지와 할아버지는 각각 1촌으로 나와 할아버지는 2촌이 되고, 아버지 형제들과 할아버지는 1촌, 나와 아버지 형제들과는 3촌이 된다. 여러 사람들에 대한 부모 자식들 간의 관계가 주어졌을 때, 주어진 두 사람의 촌수를 계산하는 프로그램을 작성하시오.',
    level = 'EASY',
    input_des = '사람들은 1, 2, 3, …, n (1 ≤ n ≤ 100)의 연속된 번호로 각각 표시된다. 입력 파일의 첫째 줄에는 전체 사람의 수 n이 주어지고, 둘째 줄에는 촌수를 계산해야 하는 서로 다른 두 사람의 번호가 주어진다. 그리고 셋째 줄에는 부모 자식들 간의 관계의 개수 m이 주어진다. 넷째 줄부터는 부모 자식간의 관계를 나타내는 두 번호 x,y가 각 줄에 나온다. 이때 앞에 나오는 번호 x는 뒤에 나오는 정수 y의 부모 번호를 나타낸다. 각 사람의 부모는 최대 한 명만 주어진다.',
    output_des = '입력에서 요구한 두 사람의 촌수를 나타내는 정수를 출력한다. 어떤 경우에는 두 사람의 친척 관계가 전혀 없어 촌수를 계산할 수 없을 때가 있다. 이때에는 -1을 출력해야 한다.',
    ex_input = '9\n7 3\n7\n1 2\n1 3\n2 7\n2 8\n2 9\n4 5\n4 6',
    ex_output = '3',
    time_limit = 1,
    memory_limit = 128
WHERE id = 5;
-- id = 6 (도서관)
UPDATE problem
SET
    baek_num = 1461,
    title = '도서관',
    description = '세준이는 도서관에서 일한다. 도서관의 개방시간이 끝나서 세준이는 사람들이 마구 놓은 책을 다시 가져다 놓아야 한다. 세준이는 현재 0에 있고, 사람들이 마구 놓은 책도 전부 0에 있다. 각 책들의 원래 위치가 주어질 때, 책을 모두 제자리에 놔둘 때 드는 최소 걸음 수를 계산하는 프로그램을 작성하시오. 세준이는 한 걸음에 좌표 1칸씩 가며, 책의 원래 위치는 정수 좌표이다. 책을 모두 제자리에 놔둔 후에는 다시 0으로 돌아올 필요는 없다. 그리고 세준이는 한 번에 최대 M권의 책을 들 수 있다.',
    level = 'EASY',
    input_des = '첫째 줄에 책의 개수 N과, 세준이가 한 번에 들 수 있는 책의 개수 M이 주어진다. 둘째 줄에는 책의 위치가 주어진다. N과 M은 50보다 작거나 같은 자연수이다. 책의 위치는 0이 아니며, 절댓값은 10,000보다 작거나 같은 정수이다.',
    output_des = '첫째 줄에 정답을 출력한다.',
    ex_input = '7 2\n-37 2 -6 -39 -29 11 -28',
    ex_output = '131',
    time_limit = 1,
    memory_limit = 128
WHERE id = 6;

-- id = 7 (경로찾기)
UPDATE problem
SET
    baek_num = 11403,
    title = '경로찾기',
    description = '가중치 없는 방향 그래프 G가 주어졌을 때, 모든 정점 (i, j)에 대해서, i에서 j로 가는 길이가 양수인 경로가 있는지 없는지 구하는 프로그램을 작성하시오.',
    level = 'EASY',
    input_des = '첫째 줄에 정점의 개수 N (1 ≤ N ≤ 100)이 주어진다. 둘째 줄부터 N개 줄에는 그래프의 인접 행렬이 주어진다. i번째 줄의 j번째 숫자가 1인 경우에는 i에서 j로 가는 간선이 존재한다는 뜻이고, 0인 경우는 없다는 뜻이다. i번째 줄의 i번째 숫자는 항상 0이다.',
    output_des = '총 N개의 줄에 걸쳐서 문제의 정답을 인접행렬 형식으로 출력한다. 정점 i에서 j로 가는 길이가 양수인 경로가 있으면 i번째 줄의 j번째 숫자를 1로, 없으면 0으로 출력해야 한다.',
    ex_input = '3\n0 1 0\n0 0 1\n1 0 0',
    ex_output = '1 1 1\n1 1 1\n1 1 1',
    time_limit = 1,
    memory_limit = 128
WHERE id = 7;

-- id = 8
UPDATE problem
SET
    baek_num = 12865,
    title = '평범한 배낭',
    description = '이 문제는 아주 평범한 배낭에 관한 문제이다.

한 달 후면 국가의 부름을 받게 되는 준서는 여행을 가려고 한다. 세상과의 단절을 슬퍼하며 최대한 즐기기 위한 여행이기 때문에, 가지고 다닐 배낭 또한 최대한 가치 있게 싸려고 한다.

준서가 여행에 필요하다고 생각하는 N개의 물건이 있다. 각 물건은 무게 W와 가치 V를 가지는데, 해당 물건을 배낭에 넣어서 가면 준서가 V만큼 즐길 수 있다. 아직 행군을 해본 적이 없는 준서는 최대 K만큼의 무게만을 넣을 수 있는 배낭만 들고 다닐 수 있다. 준서가 최대한 즐거운 여행을 하기 위해 배낭에 넣을 수 있는 물건들의 가치의 최댓값을 알려주자.',
    level = 'EASY',
    input_des = '첫 줄에 물품의 수 N(1 ≤ N ≤ 100)과 준서가 버틸 수 있는 무게 K(1 ≤ K ≤ 100,000)가 주어진다. 두 번째 줄부터 N개의 줄에 거쳐 각 물건의 무게 W(1 ≤ W ≤ 100,000)와 해당 물건의 가치 V(0 ≤ V ≤ 1,000)가 주어진다.

입력으로 주어지는 모든 수는 정수이다.',
    output_des = '한 줄에 배낭에 넣을 수 있는 물건들의 가치합의 최댓값을 출력한다.',
    ex_input = '4 7
6 13
4 8
3 6
5 12',
    ex_output = '14',
    time_limit = 1,
    memory_limit = 128
WHERE id = 8;

-- id = 9
UPDATE problem
SET
    baek_num = 1239,
    title = '유니',
    description = '유니콘은 체스에서 나이트와 비슷한 말이다. 단, 나이트는 두 칸을 한 방향으로 움직이고, 또 다른 한 칸을 다른 방향으로 움직이지만, 유니콘은 두 칸보다 많은 칸을 한 방향으로 움직이고, 한 칸보다 많은 칸을 또다른 방향으로 움직인다.

좀 더 정확하게 유니콘이 움직이는 방법을 살펴보면 다음과 같다.

유니콘을 든다.
유니콘을 4개의 기본 방향 중 하나로 두 칸보다 많이 움직인다.
유니콘을 방금 움직인 방향과 수직인 방향 2개 중 하나로 한 칸보다 많이 움직인다.
유니콘을 놓는다.
체스판의 크기는 N \times M이다. 체스판의 각 칸에는 알파벳 대문자의 처음 L개의 문자 중 하나가 쓰여 있다.

N,
M,
L, 그리고 단어가 주어진다. 유니콘이 움직인 경로 (유니콘을 놓은 곳)가 입력으로 주어진 단어와 일치하는 경우의 수를 출력하는 프로그램을 작성하시오.

',level = 'HARD',
    input_des = '첫째 줄에 N, M, L이 주어진다. N과 M은 300보다 작거나 같은 자연수이다. L은 26보다 작거나 같은 자연수이다. 둘째 줄에 단어가 주어진다. 단어의 길이는 최대 50이며, 알파벳 대문자로만 이루어져 있다. 셋째 줄 부터 N개의 줄에 체스판에 쓰여 있는 단어가 주어진다.',
    output_des = '첫째 줄에 경로를 1,000,000,007로 나눈 나머지를 출력한다.',
    ex_input = '4 7
6 13
4 8
3 6
5 12',
    ex_output = '14',
    time_limit = 1,
    memory_limit = 128
WHERE id = 9;

-- id = 10
UPDATE problem
SET
    baek_num = 12865,
    title = '물병',
    description = '지민이는 N개의 물병을 가지고 있다. 각 물병에는 물을 무한대로 부을 수 있다. 처음에 모든 물병에는 물이 1리터씩 들어있다. 지민이는 이 물병을 또 다른 장소로 옮기려고 한다. 지민이는 한 번에 K개의 물병을 옮길 수 있다. 하지만, 지민이는 물을 낭비하기는 싫고, 이동을 한 번보다 많이 하기는 싫다. 따라서, 지민이는 물병의 물을 적절히 재분배해서, K개를 넘지 않는 비어있지 않은 물병을 만들려고 한다.물은 다음과 같이 재분배 한다.먼저 같은 양의 물이 들어있는 물병 두 개를 고른다. 그 다음에 한 개의 물병에 다른 한 쪽에 있는 물을 모두 붓는다. 이 방법을 필요한 만큼 계속 한다. 이런 제약 때문에, N개로 K개를 넘지않는 비어있지 않은 물병을 만드는 것이 불가능할 수도 있다. 다행히도, 새로운 물병을 살 수 있다. 상점에서 사는 물병은 물이 1리터 들어있다.예를 들어, N=3이고, K=1일 때를 보면, 물병 3개로 1개를 만드는 것이 불가능하다. 한 병을 또다른 병에 부으면, 2리터가 들어있는 물병 하나와, 1리터가 들어있는 물병 하나가 남는다. 만약 상점에서 한 개의 물병을 산다면, 2리터가 들어있는 물병 두 개를 만들 수 있고, 마지막으로 4리터가 들어있는 물병 한 개를 만들 수 있다.',
    level = 'HARD',
    input_des = '첫째 줄에 N과 K가 주어진다. N은 107보다 작거나 같은 자연수이고, K는 1,000보다 작거나 같은 자연수이다.',
    output_des = '첫째 줄에 상점에서 사야하는 물병의 최솟값을 출력한다. 만약 정답이 없을 경우에는 -1을 출력한다.',
    ex_input = '1000000 5',
    ex_output = '15808',
    time_limit = 1,
    memory_limit = 128
WHERE id = 10;