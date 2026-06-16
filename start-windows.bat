@echo off
echo ============================================
echo   CodeScan AI - Startup Script (Windows)
echo ============================================
echo.

echo [1/2] Starting Spring Boot backend...
start "Backend - Spring Boot" cmd /k "cd backend && mvn spring-boot:run"

echo Waiting 15 seconds for backend to start...
timeout /t 15 /nobreak > nul

echo [2/2] Starting React frontend...
start "Frontend - React" cmd /k "cd frontend && npm start"

echo.
echo ============================================
echo   App running at: http://localhost:3000
echo   API running at: http://localhost:8080
echo ============================================
echo.
pause
