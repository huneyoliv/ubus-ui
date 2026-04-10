# Android Build

Located in: Related to composeApp/ (at repo root)

## Build Requirements
- JDK 21 (Temurin recommended)
- Android SDK API 34+

## Build

From repo root:

\\\ash
./gradlew :composeApp:assembleDebug

# Or release
./gradlew :composeApp:assembleRelease
\\\

## Output
APK: \composeApp/build/outputs/apk/debug/composeApp-debug.apk\

## Deployment
- Firebase App Distribution
- Google Play Store
- Manual sideload via ADB

See root [DEPLOY.md](../DEPLOY.md) for more details.
