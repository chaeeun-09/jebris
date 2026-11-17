#!/bin/bash
# =====================================================
#  J.E.B.R.I.S 학교 서버 재배포 (서버에서 root로 실행)
#  deploy_server.bat 이 ~/jebris-src 에 소스를 올린 뒤 이 스크립트를 실행한다.
#  처음 설치(Java, Docker Oracle, Tomcat, /etc/jebris/*.env)는 이미 되어 있어야 한다.
# =====================================================
set -euo pipefail
SRC=/home/ubuntu/jebris-src
T=/opt/tomcat
B=/tmp/jebris-build
OJDBC=$SRC/src/main/webapp/WEB-INF/lib/ojdbc6.jar

echo "[1/3] 컴파일"
[ -f "$OJDBC" ] || { echo "!! ojdbc6.jar 가 없어요: $OJDBC"; exit 1; }
rm -rf $B && mkdir -p $B/classes
find $SRC/src/main/java -name '*.java' > $B/sources.txt
javac -encoding UTF-8 --release 17 -nowarn \
  -cp "$T/lib/servlet-api.jar:$T/lib/jsp-api.jar:$OJDBC" -d $B/classes @$B/sources.txt
# 비밀 값은 /etc/jebris/*.env 환경변수로 주입 → 설정 파일에는 비워 둔다
cp $SRC/src/main/java/config.properties.example $B/classes/config.properties
sed -i 's/^jeb\.apiKey=.*/jeb.apiKey=/; s/^db\.password=.*/db.password=/' $B/classes/config.properties

echo "[2/3] Tomcat 배포"
systemctl stop tomcat
rm -rf $T/webapps/JEBRIS $T/work/Catalina/localhost/JEBRIS
mkdir -p $T/webapps/JEBRIS
cp -r $SRC/src/main/webapp/. $T/webapps/JEBRIS/
mkdir -p $T/webapps/JEBRIS/WEB-INF/classes
cp -r $B/classes/. $T/webapps/JEBRIS/WEB-INF/classes/
chown -R tomcat:tomcat $T/webapps/JEBRIS
systemctl start tomcat

echo "[3/3] 동작 확인"
CODE=000
for i in $(seq 1 30); do
  CODE=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/JEBRIS/login || true)
  [ "$CODE" = "200" ] && break
  sleep 2
done
echo " /JEBRIS/login 응답 코드: $CODE"
if [ "$CODE" != "200" ]; then
  echo "!! 서버가 응답하지 않아요. 최근 로그:"
  tail -20 $T/logs/catalina.out
  exit 1
fi
echo "배포 완료!"
