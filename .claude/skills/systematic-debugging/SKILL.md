---
name: systematic-debugging
description: Root-cause debugging procedure for JEBRIS (Tomcat, JSP, Servlet, JDBC, Oracle, Jeb API). Use when an error, stack trace, ORA- code, HTTP 4xx/5xx, or unexpected behavior is reported (오류, 에러, 안 돼, 디버깅). Analyze cause, form a hypothesis and verify it before changing any code.
allowed-tools: Read, Grep, Glob, Bash
---

# 체계적 디버깅

**원인을 확인하기 전에는 코드를 고치지 않는다.** 아래 4단계를 순서대로 진행하고 단계마다 사용자에게 짧게 공유한다.

## 1. 증상 정리
- 정확한 오류 메시지, 발생 화면/주소, 재현 순서를 적는다.
- 어느 층에서 났는지 분류한다.

| 층 | 단서 |
|---|---|
| 브라우저(JS) | 개발자도구 Console/Network, 마이크 권한, `fetch` 실패 |
| JSP | `JasperException`, 줄 번호가 있는 컴파일 오류, 한글 깨짐 |
| Servlet/필터 | 404(주소·@WebServlet), 405(GET/POST), 302 반복(LoginFilter) |
| JDBC/Oracle | `SQLException`, `ORA-xxxxx`, `ClassNotFoundException: oracle.jdbc` |
| 제브 API | 401(키), 403(권한), 429(사용량·동시 요청), 502/503(서버) |
| 환경 | Tomcat 버전(jakarta/javax), JDK 버전, 포트 충돌(8080, 1521) |

## 2. 가설 세우기
- 가능한 원인을 1~3개 적고, 각각 **어떻게 확인할지**를 함께 적는다.
- 자주 나는 원인
  - `ORA-01017`: DB 계정/비밀번호 → `config.properties`
  - `ORA-00942`: 테이블 없음 → `setup_db.bat` 실행 여부
  - `ORA-12505`/`12514`: SID·서비스 이름 → 11g XE는 `@localhost:1521:XE`
  - `ORA-12541`: 리스너 꺼짐 → services.msc의 OracleXETNSListener
  - `jakarta.servlet` 컴파일 오류: Targeted Runtime이 Tomcat 10이 아님
  - 한글 깨짐: EncodingFilter, JSP pageEncoding, Eclipse 워크스페이스 인코딩

## 3. 검증
- 코드 수정 없이 확인할 수 있는 방법을 먼저 쓴다: 로그 읽기, 설정 파일 확인, 작은 SQL 실행, 주소 직접 호출.
- 가설이 틀렸으면 2단계로 돌아간다. 추측으로 여러 곳을 한꺼번에 바꾸지 않는다.

## 4. 수정과 확인
- 원인이 확인된 곳만 최소한으로 고친다.
- 고친 뒤 같은 재현 순서로 다시 확인하고, 비슷한 코드(다른 DAO, 다른 Servlet)에도 같은 문제가 있는지 찾는다.

## 보고 형식
```
증상: ...
원인: ... (근거: 로그/파일:줄)
수정: 파일 - 무엇을 왜
확인: 재현 순서로 다시 해본 결과
```
