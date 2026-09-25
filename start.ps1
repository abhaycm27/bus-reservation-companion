Write-Host "Compiling TransitFlow Web Server..." -ForegroundColor Cyan
javac -d bin -sourcepath src src/com/busreservation/server/WebServer.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "Java Compilation Failed!" -ForegroundColor Red
    Exit
}
Write-Host "Starting Web Server on http://localhost:8080/..." -ForegroundColor Green
Start-Process "http://localhost:8080/"
java -cp bin com.busreservation.server.WebServer
