@echo off
setlocal
cd /d "%~dp0"
rem =====================================================
rem  J.E.B.R.I.S 학교 서버 재배포 (더블클릭)
rem  지금 이 폴더의 소스(커밋 안 한 수정 포함)를 서버로 올려
rem  컴파일 - Tomcat 배포 - 동작 확인까지 한다.
rem  config.properties 는 올리지 않음 (서버는 /etc/jebris/*.env 사용)
rem =====================================================
set "HOST=ubuntu@ssh.gsmsv.site"
set "PORT=24182"
set "TAR=%SystemRoot%\System32\tar.exe"

echo.
echo  ===== J.E.B.R.I.S 학교 서버 배포 =====
echo.
if not exist "src\main\webapp\WEB-INF\lib\ojdbc6.jar" (
  echo !! src\main\webapp\WEB-INF\lib\ojdbc6.jar 가 없어요. start_jebris.bat 을 한 번 실행해 주세요.
  goto fail
)

echo [업로드] 소스를 서버로 보내는 중...
"%TAR%" -cf - --exclude=src/main/java/config.properties src sql tools/server_deploy.sh | ssh -o BatchMode=yes -o ConnectTimeout=15 -p %PORT% %HOST% "rm -rf ~/jebris-src && mkdir -p ~/jebris-src && tar -xf - -C ~/jebris-src 2>/dev/null"
if errorlevel 1 (
  echo !! 업로드 실패. 인터넷 연결과 SSH 키 등록을 확인해 주세요.
  goto fail
)

chcp 65001 >nul
ssh -o BatchMode=yes -p %PORT% %HOST% "tr -d '\r' < ~/jebris-src/tools/server_deploy.sh | sudo bash -s"
chcp 949 >nul
if errorlevel 1 goto fail

echo.
echo  이 PC에서 접속하려면 아래 명령으로 터널을 연 뒤 http://localhost:8091/JEBRIS/ 를 여세요.
echo    ssh -N -o ServerAliveInterval=30 -p %PORT% -L 8091:localhost:8080 %HOST%
echo.
if /i not "%~1"=="nopause" pause
exit /b 0

:fail
echo.
echo !! 배포에 실패했어요. 위 메시지를 확인해 주세요.
if /i not "%~1"=="nopause" pause
exit /b 1
