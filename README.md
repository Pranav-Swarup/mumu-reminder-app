# 🌸 MuMu — Minimalist Reminder & Task App

A calm, pastel-themed Android productivity app with zero clutter and multiple reminder intensities.

## Features

- **🔴 Urgent Tasks** — Must-do-today with persistent heads-up notifications
- **🔔 Recurring Alarms** — Daily/weekly/monthly habits with exact alarms
- **💭 Gentle Reminders** — Silent or on-unlock-only soft nudges
- **📋 Passive Todos** — Simple brain-dump checklist
- **📝 Notes** — Grid/list view with color coding and lock support
- **⚡ 2-tap add flow** — FAB → type → input → done

## Build Instructions

### Option A: Android Studio (Recommended)

1. **Install Android Studio** from https://developer.android.com/studio
2. **Unzip** the project folder
3. **Open** Android Studio → File → Open → select the `MuMu` folder
4. **Wait** for Gradle sync to complete (first time takes a few minutes)
5. **Connect** your Android phone via USB (enable Developer Options + USB Debugging)
   - Or create an emulator: Tools → Device Manager → Create Virtual Device
6. **Run**: Click the green ▶️ play button or press `Shift+F10`
7. **Build APK**: Build → Build Bundle(s)/APK(s) → Build APK(s)
   - APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Option B: Command Line

```bash
# Prerequisites: Java 17+, Android SDK with platform 34
export ANDROID_HOME=/path/to/your/android/sdk

# Build debug APK
./gradlew assembleDebug

# APK output:
# app/build/outputs/apk/debug/app-debug.apk
```

### Option C: Online Build Service

1. Go to https://appetize.io or similar Android build service
2. Upload the project zip
3. Download the built APK

### Installing the APK on your phone

1. Transfer the `app-debug.apk` to your phone
2. Open it on your phone
3. If prompted, allow "Install from unknown sources"
4. Install and open!

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Repository
- **Database:** Room
- **Notifications:** AlarmManager (exact) + Notification Channels
- **Min SDK:** 26 (Android 8.0)

## Project Structure

```
app/src/main/java/com/mumu/app/
├── MuMuApp.kt              # Application class
├── MainActivity.kt          # Main activity + navigation
├── data/
│   ├── model/Models.kt      # Task, Note, Media entities
│   ├── db/
│   │   ├── MuMuDatabase.kt  # Room database
│   │   └── Daos.kt          # Data access objects
│   └── repository/           # Repository layer
├── notification/
│   ├── NotificationHelper.kt # Channel management + sending
│   ├── AlarmScheduler.kt     # AlarmManager scheduling
│   └── Receivers.kt          # Boot, alarm, unlock, action receivers
└── ui/
    ├── MainViewModel.kt      # Central ViewModel
    ├── theme/Theme.kt         # Colors, typography, shapes
    ├── components/            # Shared UI components
    │   ├── Components.kt      # Cards, sections, text fields
    │   └── AddBottomSheet.kt  # Fast-add flow
    └── screens/
        ├── today/             # Today view
        ├── todos/             # Checklist view
        ├── reminders/         # Recurring + scheduled
        └── notes/             # Notes grid/list
```

## Design

Dark theme with pastel accents:
- Background: `#121212`
- Lavender `#CDB4DB` · Peach `#FFC8A2` · Mint `#BDE0C4` · Pink `#FFAFCC`
- Rounded corners, soft shadows, generous spacing
- Zero gradients, zero neon, zero corporate feel

## Permissions

- `POST_NOTIFICATIONS` — Show reminders
- `SCHEDULE_EXACT_ALARM` — Precise alarm timing
- `RECEIVE_BOOT_COMPLETED` — Restore alarms after reboot
- `VIBRATE` — Haptic feedback
- `USE_BIOMETRIC` — Note locking (future)
