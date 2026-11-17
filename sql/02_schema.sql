-- =====================================================
--  2단계: jebris 계정으로 실행 — 테이블과 시퀀스 만들기 (Oracle 11g XE 호환)
--  MEMBER(회원) 1 : N SCHEDULE(일정), MEMBER 1 : N CHAT_LOG(대화)
--  다시 실행하면 기존 테이블을 지우고 새로 만듭니다.
-- =====================================================
WHENEVER SQLERROR CONTINUE
DROP TABLE CHAT_LOG CASCADE CONSTRAINTS PURGE;
DROP TABLE SCHEDULE CASCADE CONSTRAINTS PURGE;
DROP TABLE MEMBER CASCADE CONSTRAINTS PURGE;
DROP SEQUENCE seq_member;
DROP SEQUENCE seq_schedule;
DROP SEQUENCE seq_chat_log;

WHENEVER SQLERROR EXIT FAILURE
CREATE SEQUENCE seq_member   START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_schedule START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_chat_log START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE MEMBER (
    member_id    NUMBER             NOT NULL,
    login_id     VARCHAR2(20 CHAR)  NOT NULL,
    password     VARCHAR2(200)      NOT NULL,
    member_name  VARCHAR2(20 CHAR)  NOT NULL,
    voice        VARCHAR2(2)        DEFAULT 'M1' NOT NULL,
    created_at   TIMESTAMP          DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uq_member_login UNIQUE (login_id)
);

CREATE TABLE SCHEDULE (
    schedule_id  NUMBER             NOT NULL,
    member_id    NUMBER             NOT NULL,
    title        VARCHAR2(100 CHAR) NOT NULL,
    start_at     TIMESTAMP          NOT NULL,
    memo         VARCHAR2(500 CHAR),
    status       VARCHAR2(10)       DEFAULT 'PLANNED' NOT NULL,
    created_at   TIMESTAMP          DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_schedule PRIMARY KEY (schedule_id),
    CONSTRAINT fk_schedule_member FOREIGN KEY (member_id) REFERENCES MEMBER (member_id) ON DELETE CASCADE,
    CONSTRAINT ck_schedule_status CHECK (status IN ('PLANNED', 'DONE', 'CANCELED'))
);

CREATE TABLE CHAT_LOG (
    chat_id      NUMBER              NOT NULL,
    member_id    NUMBER              NOT NULL,
    question     VARCHAR2(1000 CHAR) NOT NULL,
    answer       VARCHAR2(1300 CHAR) NOT NULL,
    intent       VARCHAR2(20)        DEFAULT 'chat' NOT NULL,
    created_at   TIMESTAMP           DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_chat_log PRIMARY KEY (chat_id),
    CONSTRAINT fk_chat_member FOREIGN KEY (member_id) REFERENCES MEMBER (member_id) ON DELETE CASCADE
);

CREATE INDEX idx_schedule_member_start ON SCHEDULE (member_id, start_at);
CREATE INDEX idx_chat_member_created ON CHAT_LOG (member_id, created_at);

COMMIT;
SELECT table_name FROM user_tables ORDER BY table_name;
EXIT
