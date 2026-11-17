---
name: security-checklist
description: Security review checklist for the JEBRIS JSP/Servlet + JDBC + Oracle project. Use before a demo, before committing, or when the user asks for a security check (보안 점검, 보안 검사). Checks hardcoded secrets, SQL injection, XSS in JSP, session/authorization, password storage and error leakage.
allowed-tools: Read, Grep, Glob
---

# 보안 점검 (JSP · Servlet · JDBC · Oracle)

변경된 파일이나 프로젝트 전체를 아래 순서로 점검하고, 항목마다 **통과 / 문제(파일:줄) / 해당 없음**으로 보고한다. 고칠 때는 사용자에게 먼저 알리고 최소한으로 수정한다.

## 1. 비밀 정보
- `jeb.apiKey`, DB 비밀번호가 Java·JSP·JS 코드에 직접 들어가 있지 않은지. 설정은 `src/main/java/config.properties` 또는 환경변수에서만 읽는다.
- API Key가 브라우저(JS, JSP 출력, JSON 응답)로 나가지 않는지. 제브 호출은 `JebService`(서버)에서만 한다.
- `config.properties`에 실제 키가 들어간 상태로 커밋하지 않는지.

## 2. SQL 인젝션
- 모든 SQL이 `PreparedStatement` + `?` 바인딩인지. 문자열 더하기로 사용자 입력을 SQL에 넣지 않는다.
- `LIKE` 검색은 `%`, `_`, `\`를 이스케이프하고 `ESCAPE '\'`를 쓰는지 (`ScheduleDAO.escapeLike`).
- `Connection`, `PreparedStatement`, `ResultSet`을 try-with-resources로 닫는지.

## 3. XSS (JSP 출력)
- 사용자 입력·DB 값·LLM 답변을 JSP에 출력할 때 `H.e(...)`로 이스케이프하는지. `<%= 값 %>`을 그대로 쓰면 문제.
- JS에서 서버 응답을 화면에 넣을 때 `textContent`를 쓰는지. `innerHTML`에 응답 값을 넣지 않는다.

## 4. 인증·권한
- 로그인이 필요한 주소가 `LoginFilter`의 urlPatterns에 모두 있는지 (`/main`, `/schedule`, `/history`, `/mypage`, `/api/*`).
- DAO의 조회·수정·삭제가 모두 `member_id = ?` 조건으로 **본인 데이터만** 다루는지 (다른 사람 일정 번호로 요청해도 안 바뀌어야 함).
- 로그인 성공 시 세션을 새로 만드는지(세션 고정 방지), 세션에 비밀번호 해시를 넣지 않는지.
- 상태를 바꾸는 요청(삭제, 탈퇴, 로그아웃)이 POST인지.

## 5. 비밀번호
- `PasswordUtil.hash`(PBKDF2 + salt)로 저장하는지. 평문·MD5·SHA 한 번만 쓰지 않는다.
- 비밀번호 변경·탈퇴 시 현재 비밀번호를 다시 확인하는지.

## 6. 입력 검증
- 서버에서 길이·형식을 검증하는지 (HTML `required`만으로는 부족). 아이디 정규식, 제목 100자, 메모 500자, 질문 500자.
- LLM이 준 일정 JSON(title, start_at)을 검증한 뒤에만 DB에 저장하는지.

## 7. 오류 노출
- 화면에 스택 트레이스·SQL·ORA 코드가 그대로 나가지 않는지. 사용자에게는 안내 문구, 자세한 내용은 서버 콘솔에만.
- 로그에 API Key, 비밀번호가 찍히지 않는지.

## 보고 형식
```
[보안 점검 결과]
1. 비밀 정보 ............ 통과
2. SQL 인젝션 ........... 문제 - ScheduleDAO.java:45 문자열 결합
...
요약: 문제 N건 (높음 a / 보통 b /낮음 c)
```
