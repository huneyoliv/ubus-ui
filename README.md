# Ubus - University Bus Transportation System

Kotlin Multiplatform (KMP) project targeting **Android**, **iOS**, and **Web** using Compose Multiplatform.

## 📦 Project Structure

```
ubus/
├── composeApp/              # Shared KMP Compose code (all platforms)
│   └── src/
│       ├── commonMain/      # Shared code (UI, models, API)
│       ├── androidMain/     # Android-specific
│       └── iosMain/         # iOS-specific
│
├── Android/                 # Android build docs
├── iOS/Xcode/               # iOS Xcode project
├── web/                     # Web deployment (Docker, Nginx, CI/CD)
│   ├── Dockerfile           # Multi-stage: Gradle → Nginx
│   ├── nginx.conf           # SPA routing + WASM MIME types
│   ├── .github/workflows/   # GitHub Actions CI/CD
│   └── test-build.sh/bat    # Local test scripts
│
├── DEPLOY.md                # Deployment guide (secrets, troubleshooting)
└── settings.gradle.kts      # KMP build config
```

## 🚀 Quick Start

### Build Android
```bash
./gradlew :composeApp:assembleDebug
# Output: composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### Build iOS
```bash
open iOS/Xcode/iosApp.xcodeproj
# Build in Xcode
```

### Build Web (wasmJs)
```bash
./gradlew :composeApp:wasmJsReleaseDistribution
# Output: composeApp/build/dist/wasmJs/
```

### Deploy Web (Automated)
Push to `main` → GitHub Actions automatically:
1. Builds Docker image (multi-arch amd64 + arm64)
2. Pushes to ghcr.io
3. Deploys to VM via SSH

## 📚 Documentation

- **[DEPLOY.md](./DEPLOY.md)** — Complete deployment guide
  - SSH secrets setup
  - CI/CD pipeline details
  - Local testing
  - Troubleshooting
  
- **[Android/README.md](./Android/README.md)** — Android-specific build docs

- **[iOS/README.md](./iOS/README.md)** — iOS-specific build docs

- **[web/README.md](./web/README.md)** — Web deployment guide

## 🛠️ Tech Stack

- **Language**: Kotlin 2.3.20
- **UI Framework**: Compose Multiplatform 1.10.3
- **Navigation**: Decompose 3.3.0
- **HTTP Client**: Ktor 3.1.3
- **API**: https://api.ubus.me/v1
- **Build System**: Gradle 8.14.3

## 🔧 Requirements

- **JDK 21** (Temurin recommended, NOT JDK 25+)
- **Android SDK** API 34+ (for Android builds)
- **Xcode 14+** (for iOS builds, macOS only)
- **Docker** (for web deployment testing)

## 📝 Key Files

| File | Purpose |
|------|---------|
| `composeApp/build.gradle.kts` | KMP build config (targets: Android, iOS, wasmJs) |
| `composeApp/src/commonMain/` | Shared Kotlin code |
| `web/Dockerfile` | Multi-stage Docker build for wasmJs + Nginx |
| `web/nginx.conf` | Web server config (SPA routing, WASM MIME types) |
| `web/.github/workflows/deploy-web.yml` | GitHub Actions CI/CD |
| `DEPLOY.md` | Deployment secrets & troubleshooting |

## 🚢 Deployment Status

| Platform | Status | Details |
|----------|--------|---------|
| Android | ✅ Ready | APK builds, installs on devices |
| iOS | ✅ Ready | Xcode project ready, needs signing |
| Web | ✅ Auto | GitHub Actions → Docker → ghcr.io → VM |

## 🆘 Troubleshooting

### Build Issues
- **JDK version error**: Use JDK 21 (not 25+)
- **Gradle cache**: `rm -rf .gradle/configuration-cache`
- **Gradle timeout**: Try `./gradlew --no-daemon`

### Web Deployment
- **Docker build fails**: Check Gradle build output
- **WASM not loading**: Verify nginx.conf has `application/wasm` MIME type
- **SSH connection failed**: Verify secrets in GitHub > Settings

See **[DEPLOY.md](./DEPLOY.md)** for detailed troubleshooting.

## 📖 Learn More

- [Kotlin Multiplatform Docs](https://www.jetbrains.com/help/kotlin-multiplatform-dev/)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)
- [Kotlin/Wasm](https://kotl.in/wasm/)

## 📄 License

[Your License Here]
