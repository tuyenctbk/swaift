# ZenFlow — Android Automation & Zero-Logic Routine Engine

ZenFlow is a production-ready, offline-first Android automation application built with **Kotlin** and **Jetpack Compose**. It empowers users to create, manage, and execute automated routines based on environmental conditions and triggers, with local persistence, background monitoring, and robust power management.

---

## 🌟 Core Architecture & Technical Stack

- **UI Framework**: Jetpack Compose adhering strictly to Material Design 3 (M3) principles, featuring dynamic color schemes, typography, interactive filter chips, and accessible touch targets.
- **Architecture**: MVVM (Model-View-ViewModel) pattern with reactive state management using Kotlin Coroutines and `StateFlow`.
- **Local Database**: **Room Database** (`ZenFlowDatabase`) handling persistent storage for user flows and execution history logs with reactive Flow queries.
- **Background Engine**: `AutomationForegroundService` running continuous background evaluation of active flows with adaptive polling intervals (optimized for Battery Saver mode).
- **Execution Engine**: `FlowExecutor` simulating and executing device state changes (Brightness, Volume, DND, Bluetooth, Auto-Rotate, TTS alerts, and Delayed Starts).
- **Backup & Restore**: JSON export and import capabilities for seamless database portability.

---

## 📱 Key Features & Screens

1. **Dashboard (`DashboardScreen.kt`)**:
   - Central hub displaying active flows, quick status metrics, environment overview, and quick-toggle shortcuts.
   - Interactive Environment Simulator drawer to test triggers in real-time.

2. **My Flows (`MyFlowsScreen.kt`)**:
   - Manage user-created automation routines. Enable/disable, edit, or delete flows with swipe-friendly card layouts.

3. **Zero-Logic Editor (`ZeroLogicEditorScreen.kt`)**:
   - Streamlined visual editor to configure triggers (Battery Level, Time, Wi-Fi state, Bluetooth state) and complex actions (Brightness, Volume, DND, TTS announcements, and Delayed Start intervals).

4. **Discover (`DiscoverScreen.kt`)**:
   - Curated marketplace of pre-built smart routine templates for instant onboarding and activation.

5. **Execution History (`HistoryScreen.kt`)**:
   - Detailed audit logs of all routine executions, success/failure indicators, and auto-pruning controls.

6. **Settings (`SettingsScreen.kt`)**:
   - Theme customization (System / Light / Dark), Power Management (Battery Saver Engine mode), log auto-pruning preferences, and JSON database backup/restore.

---

## 🚀 Getting Started & Build

ZenFlow builds cleanly using standard Gradle Kotlin DSL (`build.gradle.kts`):

```bash
# Compile and build debug APK
gradle :app:assembleDebug
```

## 🔒 Privacy & Offline-First Design
ZenFlow operates entirely offline with zero telemetry or third-party cloud data dependencies. All routine configurations and execution logs remain securely stored in the local Room database.
