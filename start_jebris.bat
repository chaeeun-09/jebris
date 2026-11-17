@echo off
setlocal
cd /d "%~dp0"
set "LOG=%~dp0run_log.txt"
echo.
echo  ===== J.E.B.R.I.S 원클릭 실행 =====
echo  DB 확인 - 빌드 - Tomcat 배포 - 서버 시작 - 크롬 열기를 차례로 합니다. 잠시 기다려 주세요...
echo.
rem ---- SYSTEM 비밀번호: local.bat 에서 읽고, 없으면 물어본다 ----
if exist "%~dp0local.bat" call "%~dp0local.bat"
if not defined SYS_PW set /p "SYS_PW=Oracle SYSTEM 비밀번호: "
if not defined NEW_PW set "NEW_PW=%SYS_PW%"
call :main > "%LOG%" 2>&1
type "%LOG%"
echo.
echo  (이 창은 닫아도 서버는 따로 열린 Tomcat 창에서 계속 돌아갑니다)
pause
exit /b

:main
echo [%date% %time%] 시작
set "TOMCAT=C:\tomcat"
set "ORACLE_HOME=C:\oraclexe\app\oracle\product\11.2.0\server"
set "ORACLE_SID=XE"
set "NLS_LANG=AMERICAN_AMERICA.AL32UTF8"
set "PATH=%ORACLE_HOME%\bin;%PATH%"
set "OJDBC=src\main\webapp\WEB-INF\lib\ojdbc6.jar"

rem ---- 처음 받은 PC 준비: 설정 파일, JDBC 드라이버 ----
if not exist "src\main\java\config.properties" (
  copy /y "src\main\java\config.properties.example" "src\main\java\config.properties" > nul
  echo !! config.properties 를 새로 만들었어요. jeb.apiKey 에 제브 API Key 를 넣어 주세요.
)
if not exist "src\main\webapp\WEB-INF\lib" mkdir "src\main\webapp\WEB-INF\lib"
if not exist "%OJDBC%" if exist "%ORACLE_HOME%\jdbc\lib\ojdbc6.jar" copy /y "%ORACLE_HOME%\jdbc\lib\ojdbc6.jar" "%OJDBC%" > nul
if not exist "%OJDBC%" (
  echo !! %OJDBC% 가 없어요. Oracle 설치 폴더의 jdbc\lib\ojdbc6.jar 를 복사해 주세요.
  exit /b 1
)

rem ---- JDK 찾기 ----
set "JDK="
if exist "%USERPROFILE%\.jdks\ms-17.0.16\bin\javac.exe" set "JDK=%USERPROFILE%\.jdks\ms-17.0.16"
if not defined JDK if exist "%USERPROFILE%\.jdks\corretto-21.0.10\bin\javac.exe" set "JDK=%USERPROFILE%\.jdks\corretto-21.0.10"
if not defined JDK if exist "%USERPROFILE%\.jdks\openjdk-24.0.2\bin\javac.exe" set "JDK=%USERPROFILE%\.jdks\openjdk-24.0.2"
if not defined JDK (
  echo !! JDK를 찾지 못했어요.
  exit /b 1
)
echo JDK: %JDK%
set "JAVA_HOME=%JDK%"
set "JRE_HOME="

rem ---- [1] DB ----
echo.
echo [1/4] DB 확인
if exist build rmdir /s /q build
mkdir build\tools build\classes
"%JDK%\bin\javac.exe" -encoding UTF-8 -cp "%OJDBC%" -d build\tools tools\DbCheck.java tools\DbSetup.java
if errorlevel 1 (
  echo !! 설치 도구 컴파일 실패
  exit /b 1
)
"%JDK%\bin\java.exe" -cp "build\tools;%OJDBC%" DbCheck
if not errorlevel 1 goto db_ok
echo  테이블이 없어서 DB를 설치합니다.
if exist "%ORACLE_HOME%\bin\sqlplus.exe" (
  > build\unexpire.sql echo ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED;
  >> build\unexpire.sql echo ALTER USER system IDENTIFIED BY "%NEW_PW%" ACCOUNT UNLOCK;
  >> build\unexpire.sql echo EXIT;
  "%ORACLE_HOME%\bin\sqlplus.exe" -S -L "/ as sysdba" @build\unexpire.sql < nul
)
"%JDK%\bin\java.exe" -cp "build\tools;%OJDBC%" DbSetup %SYS_PW% %NEW_PW%
"%JDK%\bin\java.exe" -cp "build\tools;%OJDBC%" DbCheck
if errorlevel 1 (
  echo !! DB 준비 실패. 위 메시지를 확인하세요.
  exit /b 1
)
:db_ok

rem ---- [2] 빌드 ----
echo.
echo [2/4] 자바 소스 컴파일
dir /s /b src\main\java\*.java > build\sources.txt
"%JDK%\bin\javac.exe" -encoding UTF-8 --release 17 -cp "%TOMCAT%\lib\servlet-api.jar;%TOMCAT%\lib\jsp-api.jar;%OJDBC%" -d build\classes @build\sources.txt
if errorlevel 1 (
  echo !! 컴파일 실패
  exit /b 1
)
copy /y src\main\java\config.properties build\classes\ > nul
echo  컴파일 성공

rem ---- [3] 배포 ----
echo.
echo [3/4] Tomcat 배포
call "%TOMCAT%\bin\shutdown.bat" > nul 2>&1
ping -n 4 127.0.0.1 > nul
robocopy src\main\webapp "%TOMCAT%\webapps\JEBRIS" /MIR /NFL /NDL /NJH /NJS /NP > nul
robocopy build\classes "%TOMCAT%\webapps\JEBRIS\WEB-INF\classes" /MIR /NFL /NDL /NJH /NJS /NP > nul
if not exist "%TOMCAT%\webapps\JEBRIS\WEB-INF\classes\config.properties" (
  echo !! 배포 실패
  exit /b 1
)
echo  배포 완료: %TOMCAT%\webapps\JEBRIS

rem ---- [4] 서버 시작 ----
echo.
echo [4/4] Tomcat 시작
set "CATALINA_HOME=%TOMCAT%"
call "%TOMCAT%\bin\startup.bat"
set /a TRY=0
:wait
ping -n 3 127.0.0.1 > nul
set /a TRY+=1
curl -s -o nul http://localhost:8090/ > nul 2>&1
if not errorlevel 1 goto up
if %TRY% lss 30 goto wait
echo !! 60초가 지나도 Tomcat이 응답하지 않아요.
exit /b 1
:up
ping -n 4 127.0.0.1 > nul
curl -s -o nul -w " JEBRIS 응답 코드: %%{http_code}\n" http://localhost:8090/JEBRIS/login
set "CHROME="
if exist "%ProgramFiles%\Google\Chrome\Application\chrome.exe" set "CHROME=%ProgramFiles%\Google\Chrome\Application\chrome.exe"
if not defined CHROME if exist "%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe" set "CHROME=%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe"
if not defined CHROME if exist "%LocalAppData%\Google\Chrome\Application\chrome.exe" set "CHROME=%LocalAppData%\Google\Chrome\Application\chrome.exe"
if defined CHROME (start "" "%CHROME%" "http://localhost:8090/JEBRIS/") else (start "" "http://localhost:8090/JEBRIS/")
echo.
echo 완료! 크롬에서 http://localhost:8090/JEBRIS/ 를 열었어요.
exit /b 0
