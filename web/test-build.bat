@echo off
REM Script para testar build wasmJs localmente (Windows)

echo.🔨 Building KMP wasmJs...
call gradlew.bat :composeApp:wasmJsReleaseDistribution --no-daemon
if %ERRORLEVEL% neq 0 (
    echo.❌ Build failed
    exit /b 1
)

echo.📦 Creating Docker image...
docker build -t ubus-web:test -f web\Dockerfile .
if %ERRORLEVEL% neq 0 (
    echo.❌ Docker build failed
    exit /b 1
)

echo.🚀 Running container...
docker run -d -p 8080:80 --name ubus-web-test ubus-web:test

echo.⏳ Waiting for container to start...
timeout /t 3 /nobreak

echo.✅ Container running!
echo.
echo.✨ Web app available at http://localhost:8080
echo.📋 View logs: docker logs ubus-web-test -f
echo.🛑 Stop: docker rm -f ubus-web-test
