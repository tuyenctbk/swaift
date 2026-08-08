# AGENTS.md — ZenFlow Project Instructions & Guidelines

Welcome to the ZenFlow Android project codebase. Please adhere to the following project conventions and guidelines when maintaining or extending this application:

## 1. Project Standards
- **Language**: Kotlin exclusively.
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3). Never mix XML layouts.
- **Architecture**: MVVM with Repository pattern and Room database for local persistence.
- **Asynchronous Operations**: Kotlin Coroutines and StateFlow (`collectAsStateWithLifecycle`).

## 2. Key Codebase Modules
- **`com.example.data`**: Room entities (`FlowEntity`, `HistoryLogEntity`), DAOs, `ZenFlowDatabase`, `FlowRepository`, and JSON backup utilities.
- **`com.example.engine`**: `FlowExecutor` and `EnvironmentSimulator` for evaluating and executing automation routines.
- **`com.example.service`**: `AutomationForegroundService` for background loop monitoring and battery saver power management.
- **`com.example.viewmodel`**: `ZenFlowViewModel` and `AiRoutineViewModel` managing UI state and repository interactions.
- **`com.example.ui`**: Composables organized into `screens`, `components`, `navigation`, and `theme`.

## 3. Build & Verification
- Always run `compile_applet` after code modifications to verify successful Kotlin compilation and Gradle sync.
- Ensure all interactive Compose elements have unique `testTag` identifiers (`Modifier.testTag(...)`).
- Keep string resources in `res/values/strings.xml`.
