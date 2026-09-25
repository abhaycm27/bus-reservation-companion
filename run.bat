@echo off
echo Starting TransitFlow Web Server Compilation...
javac -d bin -sourcepath src src/com/busreservation/server/WebServer.java
if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b %errorlevel%
)
echo Compilation Successful. Launching Local Server http://localhost:8080/ ...
java -cp bin com.busreservation.server.WebServer
pause
