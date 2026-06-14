@echo off
echo === Starting WireMock (Docker) ===
docker-compose up -d

echo.
echo === Running Tests ===
set JAVA_HOME=C:\Users\pc\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64
set PATH=%JAVA_HOME%\bin;%PATH%
call tools\apache-maven-3.9.6\bin\mvn.cmd test

echo.
echo === Opening Test Report ===
start target\surefire-reports\emailable-report.html

echo.
echo === Done ===
pause
