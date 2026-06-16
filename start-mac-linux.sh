#!/bin/bash
echo "============================================"
echo "  CodeScan AI - Startup Script (Mac/Linux)"
echo "============================================"
echo ""

# Start backend in background
echo "[1/2] Starting Spring Boot backend..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo "Waiting for backend to start (15 seconds)..."
sleep 15

# Start frontend
echo "[2/2] Starting React frontend..."
cd frontend
npm start &
FRONTEND_PID=$!
cd ..

echo ""
echo "============================================"
echo "  App running at: http://localhost:3000"
echo "  API running at: http://localhost:8080"
echo "  Press Ctrl+C to stop both servers"
echo "============================================"

# Wait and cleanup on exit
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT TERM
wait
