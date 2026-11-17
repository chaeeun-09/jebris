@echo off
setlocal
cd /d "%~dp0"
set "REPO=jebris"
set "LOG=%~dp0publish_log.txt"
echo.
echo  ===== J.E.B.R.I.S GitHub 업로드 =====
echo.

rem ---- git / gh 찾기 ----
where git > nul 2>&1
if errorlevel 1 if exist "%ProgramFiles%\Git\cmd\git.exe" set "PATH=%ProgramFiles%\Git\cmd;%PATH%"
where git > nul 2>&1
if errorlevel 1 (
  echo  !! git 이 설치돼 있지 않아요.
  echo  !! git 없음 > "%LOG%"
  pause
  exit /b 1
)
set "GH="
for /f "delims=" %%G in ('where gh 2^>nul') do if not defined GH set "GH=%%G"
if not defined GH if exist "%ProgramFiles%\GitHub CLI\gh.exe" set "GH=%ProgramFiles%\GitHub CLI\gh.exe"
if not defined GH (
  echo  !! GitHub CLI^(gh^)가 없어요.
  echo  !! gh 없음 > "%LOG%"
  pause
  exit /b 1
)

rem ---- GitHub 로그인 확인 (안 돼 있으면 브라우저 로그인) ----
"%GH%" auth status > nul 2>&1
if errorlevel 1 (
  echo  GitHub 로그인이 필요해요. 브라우저가 열리면 화면에 나온 코드를 넣고 승인해 주세요.
  "%GH%" auth login --hostname github.com --git-protocol https --web
)
"%GH%" auth setup-git > nul 2>&1

call :main > "%LOG%" 2>&1
type "%LOG%"
echo.
pause
exit /b

:main
for /f "delims=" %%U in ('call "%GH%" api user --jq .login') do set "GHUSER=%%U"
if not defined GHUSER (
  echo !! GitHub 로그인 실패
  exit /b 1
)
echo GitHub 계정: %GHUSER%

if not exist ".git" git init -b main
git config user.name > nul 2>&1 || git config user.name "%GHUSER%"
git config user.email > nul 2>&1 || git config user.email "%GHUSER%@users.noreply.github.com"
git config core.autocrlf true

git add -A
rem ---- 비밀 정보 마지막 점검 ----
git ls-files --cached | findstr /i /r "config\.properties$ local\.bat \.env \.jar$" > nul
if not errorlevel 1 (
  echo !! 비밀 파일이 포함돼 있어 중단합니다.
  git ls-files --cached | findstr /i /r "config\.properties$ local\.bat \.env \.jar$"
  exit /b 1
)
git grep --cached -I -n -E "sk-[A-Za-z0-9]{20}" > nul 2>&1
if not errorlevel 1 (
  echo !! API Key 로 보이는 문자열이 있어 중단합니다.
  exit /b 1
)
echo 비밀 정보 점검 통과

git diff --cached --quiet || git commit -q -F tools\commit_message.txt
git log --oneline -1

git remote get-url origin > nul 2>&1
if errorlevel 1 (
  "%GH%" repo create "%REPO%" --private --source . --remote origin --description "J.E.B.R.I.S - 음성 AI 비서 웹 서비스 (JSP/Servlet, Oracle, 제브 AI Hub)"
)
git push -u origin main
if errorlevel 1 (
  echo !! push 실패
  exit /b 1
)
echo.
echo 완료! https://github.com/%GHUSER%/%REPO%
exit /b 0
