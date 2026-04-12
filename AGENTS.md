# 🤖 Agent Guidelines — Cross-Platform Parity (Android ↔ iOS ↔ Web)

> **This file is for AI agents (Copilot, ChatGPT, Claude, etc.) working on this codebase.**
> Read this before making any changes.

## 🚨 Critical Rule: Platform Parity

**Every change made for Android MUST be adapted for iOS (and vice versa).**

This is a Kotlin Multiplatform (KMP) project. Code is shared via `composeApp/src/commonMain/`,
but each platform has its own entry point and platform-specific configuration:

| Platform | Entry Point | Config/Permissions |
|----------|-------------|-------------------|
| **Android** | `composeApp/src/androidMain/.../MainActivity.kt` | `AndroidManifest.xml` |
| **iOS** | `composeApp/src/iosMain/.../MainViewController.kt` | `iOS/Xcode/iosApp/Info.plist` + `ContentView.swift` |
| **Web** | `composeApp/src/wasmJsMain/.../main.kt` | `web/nginx.conf` |

## ✅ Checklist — After Any Platform Change

When you modify anything platform-specific, verify:

### Permissions
- [ ] **Android**: Added to `AndroidManifest.xml`?
- [ ] **iOS**: Added usage description string to `Info.plist`?
- [ ] Web doesn't need manifest permissions (uses browser APIs)

### Safe Area / Insets
- [ ] **Android**: `enableEdgeToEdge()` + `WindowInsets` padding in `App.kt`
- [ ] **iOS**: `.ignoresSafeArea()` in `ContentView.swift` + `WindowInsets` in `App.kt`
- [ ] Both platforms share `App.kt` insets logic — if you change it, both are affected

### Lifecycle / Navigation
- [ ] **Android**: `defaultComponentContext()` in `MainActivity`
- [ ] **iOS**: `LifecycleRegistry().apply { resume() }` in `MainViewController.kt` (must be module-level, not inside composable)
- [ ] Component references must survive recomposition on iOS (use module-level `val`)

### HTTP Client
- [ ] **Android**: Uses `ktor-client-okhttp` (in `androidMain.dependencies`)
- [ ] **iOS**: Uses `ktor-client-darwin` (in `iosMain.dependencies`)
- [ ] **Web**: Uses `ktor-client-js` (in `wasmJsMain.dependencies`)
- [ ] API base URL and auth logic are in `commonMain` (shared)

### Storage
- [ ] Uses `multiplatform-settings` — works automatically on all platforms
- [ ] Android: SharedPreferences, iOS: NSUserDefaults, Web: localStorage

## 📁 Key File Mapping

| Concern | Android | iOS | Shared |
|---------|---------|-----|--------|
| Entry point | `MainActivity.kt` | `MainViewController.kt` | `App.kt` |
| Permissions manifest | `AndroidManifest.xml` | `Info.plist` | — |
| Permission gate UI | `MainActivity.kt` (Compose) | Native iOS dialogs (automatic) | — |
| Safe area handling | `enableEdgeToEdge()` + `App.kt` | `.ignoresSafeArea()` + `App.kt` | `App.kt` WindowInsets |
| Swift wrapper | — | `ContentView.swift` + `iOSApp.swift` | — |
| HTTP engine | `ktor-client-okhttp` | `ktor-client-darwin` | `ktor-client-core` |
| Navigation | — | — | `RootComponent.kt` (Decompose) |
| All UI screens | — | — | `ui/screens/**` (commonMain) |
| Theme/components | — | — | `ui/theme/**`, `ui/components/**` |

## 🔧 Platform-Specific Patterns

### Adding a New Permission

**Android** (`composeApp/src/androidMain/AndroidManifest.xml`):
```xml
<uses-permission android:name="android.permission.NEW_PERMISSION"/>
```
Also add to the `requiredPermissions` list in `MainActivity.kt` for the permission gate.

**iOS** (`iOS/Xcode/iosApp/Info.plist`):
```xml
<key>NSNewPermissionUsageDescription</key>
<string>Descrição em português do motivo.</string>
```
iOS shows permission dialogs automatically when the app first uses the feature.

### Adding a New Screen

1. Add `Config` + `Child` to `RootComponent.kt` (commonMain)
2. Create screen composable in `ui/screens/` (commonMain)
3. Add route to `RootContent.kt` (commonMain)
4. **No platform-specific changes needed** — screens are 100% shared

### Changing Navigation Behavior

All navigation is in `commonMain` via Decompose. Changes apply to all platforms.
But if you change lifecycle handling, verify both:
- `MainActivity.kt` → `defaultComponentContext()`
- `MainViewController.kt` → `DefaultComponentContext(lifecycle = lifecycle)`

## ⚠️ Common Mistakes

1. **Adding Android permission but forgetting iOS Info.plist** → App crashes on iOS
2. **Creating LifecycleRegistry inside composable on iOS** → Recreated on every recomposition, navigation breaks
3. **Hardcoding `top = 48.dp` padding** → Use `App.kt` WindowInsets instead (shared)
4. **Using Android-only APIs in commonMain** → Compilation fails on iOS/Web
5. **Forgetting to test wasmJs** → WASM has different limitations (no file system, no threads)

## 🏗️ Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS (requires macOS + Xcode)
open iOS/Xcode/iosApp.xcodeproj
# Then: Product → Build (⌘B)

# Web (wasmJs)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# All checks
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosArm64
./gradlew :composeApp:compileKotlinWasmJs
```

## 📝 Summary

> **When in doubt: if you changed something for one platform, search for
> the equivalent on the other platforms and update it too.**
>
> The shared code in `commonMain` handles 95% of the app. Platform-specific
> code is minimal but critical — permissions, lifecycle, HTTP engine, safe areas.
