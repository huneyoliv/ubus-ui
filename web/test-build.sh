#!/bin/bash
# Script para testar build wasmJs localmente

set -e

echo "🔨 Building KMP wasmJs..."
./gradlew :composeApp:wasmJsReleaseDistribution --no-daemon

echo "📦 Creating Docker image..."
docker build -t ubus-web:test -f web/Dockerfile .

echo "🚀 Running container..."
docker run -d -p 8080:80 --name ubus-web-test ubus-web:test

echo "⏳ Waiting for container to start..."
sleep 3

echo "✅ Container running! Testing..."
if curl -s http://localhost:8080 > /dev/null; then
    echo "✅ Web server responding!"
else
    echo "❌ Web server not responding"
    docker logs ubus-web-test
    exit 1
fi

echo ""
echo "✨ Web app available at http://localhost:8080"
echo "📋 View logs: docker logs ubus-web-test -f"
echo "🛑 Stop: docker rm -f ubus-web-test"
