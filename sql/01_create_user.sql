-- =====================================================
--  1단계: 관리자(SYSTEM)로 실행 — jebris 계정 만들기 (Oracle 11g XE)
-- =====================================================
WHENEVER SQLERROR CONTINUE
CREATE USER jebris IDENTIFIED BY jebris
    DEFAULT TABLESPACE users
    QUOTA UNLIMITED ON users;

GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO jebris;

EXIT
