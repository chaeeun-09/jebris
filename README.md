# J.E.B.R.I.S (제브리스)

말로 묻고 음성으로 답을 듣는 AI 비서 웹 서비스
Java · JSP/Servlet · JDBC · Oracle Database · HTML/CSS/JavaScript · 제브 AI Hub

```
크롬 마이크 ──(Web Speech API)──▶ 텍스트
   └─▶ /api/chat (Servlet) ─▶ AssistantService ─▶ 제브 flash-next (답변 + 일정 추출)
                                   │                └▶ 제브 supertonic (음성 mp3)
                                   └─▶ DAO + JDBC ─▶ Oracle (MEMBER / SCHEDULE / CHAT_LOG)
```

---

## 바로 실행하기 (이 PC 기준: Tomcat 10.1 · Oracle 11g XE)

### 원클릭 실행 (이클립스 없이)

`start_jebris.bat` 더블클릭 → DB 확인/설치 → 컴파일 → Tomcat 배포 → 서버 시작 → 크롬 열기까지 자동으로 해요. 진행 기록은 `run_log.txt`.

처음 받은 PC라면:
- `src/main/java/config.properties.example` → `config.properties`로 복사하고 `jeb.apiKey`에 키 넣기 (bat이 없으면 자동 복사해 줘요)
- `local.bat`을 만들어 `set "SYS_PW=SYSTEM비밀번호"`를 적어 두면 비밀번호를 묻지 않아요 (없으면 실행할 때 물어봐요)
- `ojdbc6.jar`는 Git에 없어요. bat이 Oracle 설치 폴더(`jdbc\lib`)에서 자동으로 복사해요
- Oracle XE가 8080 포트를 쓰기 때문에 Tomcat은 **8090** 포트로 바꿔서 써요 (`C:\tomcat\conf\server.xml`)

> `config.properties`, `local.bat`, `*.jar`는 `.gitignore`에 들어 있어 Git에 올라가지 않아요.

### 이클립스로 실행

**1. DB 만들기** — `setup_db.bat` 더블클릭

**2. 제브 API Key 넣기** — 제브 대시보드에서 `새 키` 발급 → `src/main/java/config.properties`의 `jeb.apiKey=` 뒤에 붙여 넣기

**3. 이클립스로 가져오기** — `File > Import > General > Existing Projects into Workspace` → `바탕화면\J.E.B.R.I.S` 선택 → Finish

**4. 실행** — 프로젝트 우클릭 → `Run As > Run on Server` → Tomcat v10.1 → **크롬**에서 `http://localhost:8090/JEBRIS/`

> 3번에서 "Apache Tomcat v10.1 런타임이 없다"는 오류가 나면: `Window > Preferences > Server > Runtime Environments > Add` → Apache Tomcat v10.1 → 폴더 `C:\tomcat` 지정.
> 다른 이름으로 이미 등록돼 있다면: 프로젝트 우클릭 > `Properties > Targeted Runtimes`에서 그 Tomcat을 체크하세요.

> API Key는 서버(Java)에서만 쓰고 브라우저로 보내지 않아요. config.properties를 Git이나 단톡방에 올리지 마세요.
> 마이크는 `localhost`에서만 http로 허용돼요. 다른 컴퓨터에서 IP로 접속하면 https가 필요해요.

---

## 시연 시나리오

1. 회원가입(목소리 선택) → 로그인
2. "안녕, 제브리스" → 이름을 부르며 음성으로 대답
3. "이번 주 금요일 저녁 7시에 축구 추가해줘" → 왼쪽 일정 패널에 바로 반영
4. "금요일에 무슨 일정 있어?" → DB 일정을 근거로 대답
5. 일정 메뉴 → 검색, 수정, 완료 처리, 삭제 (CRUD)
6. 대화 기록 메뉴 → 검색, 삭제
7. 내 정보 → 이름·목소리 변경 후 다시 대화해서 바뀐 목소리 확인

## 폴더 구조

```
src/main/java
├─ config.properties.example      DB·제브 설정 예시 (복사해서 config.properties로)
└─ jebris
   ├─ controller   Servlet (Login, Join, Logout, Main, Schedule, History, MyPage, ChatApi, TodayApi)
   ├─ filter       EncodingFilter(UTF-8), LoginFilter(로그인 확인)
   ├─ service      JebService(제브 API 호출), AssistantService(대화·일정 처리)
   ├─ dao          MemberDAO, ScheduleDAO, ChatLogDAO (PreparedStatement)
   ├─ dto          Member, Schedule, ChatLog
   └─ util         DBUtil, AppConfig, Json, PasswordUtil, H(화면 출력), Web
src/main/webapp
├─ index.jsp
├─ css/style.css   js/common.js   js/jarvis.js
└─ WEB-INF
   ├─ web.xml
   ├─ lib/         ojdbc6.jar 넣는 곳 (Git 제외)
   └─ views/       login, join, main, schedule, history, mypage, error (+ common/*.jspf)
sql/               01_create_user.sql, 02_schema.sql
```

## 주요 주소

| 주소 | 설명 |
|---|---|
| `/login`, `/join`, `/logout` | 로그인, 회원가입, 로그아웃 |
| `/main` | 제브리스 메인 (음성 대화) |
| `/schedule` | 일정 CRUD + 검색 |
| `/history` | 대화 기록 조회·검색·삭제 |
| `/mypage` | 이름·목소리·비밀번호 변경, 탈퇴 |
| `POST /api/chat` | 질문 → 답변 JSON (+ base64 mp3) |
| `GET /api/today` | 7일 안의 일정 JSON |

## 문제 해결

| 증상 | 확인할 것 |
|---|---|
| `ClassNotFoundException: oracle.jdbc.OracleDriver` | WEB-INF/lib에 ojdbc6.jar가 있는지 |
| 로그인 시 "데이터베이스에 연결하지 못했어요" | 서비스(services.msc)에서 OracleServiceXE, OracleXETNSListener가 실행 중인지 |
| `ORA-01017` | DB 아이디·비밀번호 |
| "제브 API Key가 없어요" | config.properties의 jeb.apiKey |
| 429 오류 | 제브 하루 사용량(TTS 600초, LLM 1,800 크레딧) 소진 또는 동시 요청 |
| 마이크 버튼이 비활성화 | 크롬/엣지로 열었는지, 마이크 권한 허용했는지 |
| 한글 깨짐 | Eclipse `Window > Preferences > General > Workspace > Text file encoding`을 UTF-8로 |
| `jakarta.servlet` 빨간 줄 | 프로젝트 Properties > Targeted Runtimes에서 Tomcat 10 체크 |

## Tomcat 9로 바꾸기

Tomcat 9 이하는 `javax.servlet`을 써요.
1. 모든 Java 파일에서 `jakarta.servlet` → `javax.servlet` 으로 바꾸기 (Eclipse `Search > File...`의 Replace)
2. `error.jsp`의 `jakarta.servlet.error.status_code` → `javax.servlet.error.status_code`
3. `web.xml` 첫 부분을 Servlet 4.0 형식(`http://xmlns.jcp.org/xml/ns/javaee`, version="4.0")으로 바꾸기
