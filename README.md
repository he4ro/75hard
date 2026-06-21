# 75 Hard Tracker — Android App

## Build Instructions

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps
1. Open Android Studio → **Open** → select this folder (`75hard/`)
2. Wait for Gradle sync to complete (downloads dependencies, ~2 min first time)
3. Connect your Android device (USB debugging on) or start an emulator
4. Press **Run ▶** or `Shift+F10`

### Build a release APK
- Build → Generate Signed App Bundle / APK → APK
- Use a debug keystore for personal use

---

## App Features

- **Setup screen** — add your 5 rules (or use defaults), start the challenge
- **Home screen** — big day counter (1–75), tap rules to check off, 75-square grid horizon
- **Day logic** — configurable end-of-day cutoff (default 2 AM) so night owls aren't penalized
- **Background Warden** — WorkManager checks daily; if you missed tasks, resets to Day 1
- **Settings** — change cutoff hour, add/remove rules mid-challenge
- **Reset button** — manual reset with confirmation dialog

## Architecture
- **Room** — local SQLite for rules, daily logs, and challenge state
- **ViewModel + StateFlow** — reactive UI, single source of truth
- **Jetpack Compose** — full Compose UI, dark theme
- **WorkManager** — background daily enforcement
