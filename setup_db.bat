@echo off
cd /d "%~dp0"
rem ---- SYSTEM 비밀번호: local.bat 에서 읽고, 없으면 물어본다 ----
if exist "%~dp0local.bat" call "%~dp0local.bat"
if not defined SYS_PW set /p "SYS_PW=Oracle SYSTEM 비밀번호: "
if not defined NEW_PW set "NEW_PW=%SYS_PW%"
echo.
echo  ===== J.E.B.R.I.S 데이터베이스 설치 =====
echo.

rem ---- [0] SQL*Plus(관리자 OS 인증)로 SYSTEM 비밀번호 만료 해제 ----
rem   Error 6 / sp1 msb 오류는 ORACLE_HOME, NLS_LANG 이 안 잡혀서 나는 경우가 많아 직접 지정한다.
set "ORACLE_HOME=C:\oraclexe\app\oracle\product\11.2.0\server"
set "ORACLE_SID=XE"
set "NLS_LANG=AMERICAN_AMERICA.AL32UTF8"
set "PATH=%ORACLE_HOME%\bin;%PATH%"
if exist "%ORACLE_HOME%\bin\sqlplus.exe" (
  echo  [0] SQL*Plus 로 SYSTEM 비밀번호 만료를 풉니다...
  > "%TEMP%\jebris_unexpire.sql" echo ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED;
  >> "%TEMP%\jebris_unexpire.sql" echo ALTER USER system IDENTIFIED BY "%NEW_PW%" ACCOUNT UNLOCK;
  >> "%TEMP%\jebris_unexpire.sql" echo EXIT;
  "%ORACLE_HOME%\bin\sqlplus.exe" -S -L "/ as sysdba" @"%TEMP%\jebris_unexpire.sql"
  del "%TEMP%\jebris_unexpire.sql" >nul 2>&1
  echo.
) else (
  echo  [0] sqlplus.exe 가 없어 건너뜁니다.
)

rem ---- Java 찾기 (JDK 17 이상) ----
set "JAVA="
if exist "%USERPROFILE%\.jdks\ms-17.0.16\bin\java.exe" set "JAVA=%USERPROFILE%\.jdks\ms-17.0.16\bin\java.exe"
if not defined JAVA if exist "%USERPROFILE%\.jdks\corretto-21.0.10\bin\java.exe" set "JAVA=%USERPROFILE%\.jdks\corretto-21.0.10\bin\java.exe"
if not defined JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JAVA for /f "delims=" %%J in ('where java 2^>nul') do if not defined JAVA set "JAVA=%%J"
if not defined JAVA (
  echo  !! Java를 찾지 못했어요. JDK 17 이상을 설치하거나 JAVA_HOME을 지정하세요.
  pause
  exit /b 1
)
echo  사용하는 Java: %JAVA%
echo.

"%JAVA%" -Dfile.encoding=UTF-8 -cp "src\main\webapp\WEB-INF\lib\ojdbc6.jar" tools\DbSetup.java %SYS_PW% %NEW_PW% > setup_db_log.txt 2>&1
type setup_db_log.txt
echo.
pause
