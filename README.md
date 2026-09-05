# Music Player Application

[![Android CI/CD](https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPOSITORY_NAME>/actions/workflows/android_ci.yml/badge.svg)](https://github.com/whyriez/music-player-test/actions/workflows/android_ci.yml)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)

A modern Android music streaming application built using **Kotlin**, **MVVM architecture**, and **AndroidX Media3 (ExoPlayer)**. The app fetches track previews from the public iTunes Search API and provides an audio playback experience with real-time queue management, state synchronization, and a custom Canvas-rendered spectrum visualizer.

---

## 🚀 Features

- **iTunes Search Integration**: Query songs, artists, and albums dynamically with instant response rendering.
- **Audio Playback Engine**:
  - Full playback controls: Play, Pause, Next, Previous, and Track Dismissal.
  - Interactive scrub/seek slider synchronized with track progress and duration.
  - Automatic queue progression (Auto-play next track when current audio finishes).
- **Custom Spectrum Visualizer**:
  - Custom Android View (`SpectrumView`) drawn via Android `Canvas` rendering animated frequency bars for the active track.
  - Lifecycle-aware animation management optimized for RecyclerView view-caching.
- **Robust State Management**: Comprehensive UI states covering `Idle`, `Loading`, `Success`, `Empty`, and `Error` with retry actions for network timeouts and offline scenarios.
- **XML Layouts & ViewBinding**: High-performance XML layouts with responsive bottom sheet controls avoiding UI overlap.

---

## 🏗️ Architecture & Design Patterns

The project strictly follows **Clean Architecture** and the **MVVM (Model-View-ViewModel)** design pattern to enforce separation of concerns and maintainable, testable code.

```text
com.whyriez.music/
├── data/
│   ├── model/               # DTOs & API Response mappers (toDomain)
│   ├── remote/              # Retrofit API Service & Network Client
│   └── repository/          # MusicRepository implementation (with error dispatching)
├── domain/
│   ├── model/               # Pure business domain entities (Song)
│   └── repository/          # Repository interfaces
├── player/
│   └── MusicPlayerManager.kt# Standalone audio engine wrapping AndroidX Media3
├── ui/
│   ├── adapter/             # ListAdapter with DiffUtil & ViewHolder lifecycle handling
│   ├── viewmodel/           # MusicViewModel managing UI states & coroutine flows
│   └── MainActivity.kt      # Activity binding, state collection, & user interaction
└── utils/
    ├── Resource.kt          # Generic network state wrapper
    └── SpectrumView.kt      # Custom Canvas-based visualizer
```

### Key Architectural Highlights
- **Decoupled Audio Manager**: `MusicPlayerManager` operates independently from the UI layer, exposing playback states via Kotlin `StateFlow`.
- **Unidirectional Data Flow (UDF)**: ViewModels emit immutable `MusicUiState` instances collected by the Activity using `repeatOnLifecycle`.
- **Defensive Error Handling**: Explicit network exception catching (`UnknownHostException`, `SocketTimeoutException`, `HttpException`) mapped to user-friendly feedback.

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin
- **UI Framework**: Native Android XML with ViewBinding
- **Audio Playback**: AndroidX Media3 (ExoPlayer)
- **Networking**: Retrofit 2 + OkHttp 3 + Gson
- **Asynchronous & Reactive**: Kotlin Coroutines & StateFlow
- **Image Loading**: Glide
- **Testing**:
  - **JUnit 4** - Unit test runner
  - **MockK** - Mocking library for Kotlin
  - **Turbine** - Flow testing assertion library
  - **Kotlinx Coroutines Test** - Virtualized dispatcher testing

---

## 🧪 Unit Testing

Unit tests cover the data and presentation layers, ensuring API error resilience and state flow integrity.

Run the unit test suite locally via Gradle:

```bash
./gradlew testDebugUnitTest
```

Test coverage includes:
- **`MusicRepositoryImplTest`**: Verifies DTO mapping, filtering of invalid track entries, and network exception translation (`UnknownHostException`, `SocketTimeoutException`, HTTP 500).
- **`MusicViewModelTest`**: Tests state emission sequences (`Idle` -> `Loading` -> `Success` / `Empty` / `Error`), blank query protections, retry triggers, and playback delegation.

---

## ⚙️ Building & Running Locally

### Prerequisites
- Android Studio Ladybug | 2024.2+ or newer
- JDK 17
- Android SDK (API Level 34)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPOSITORY_NAME>.git
   cd <YOUR_REPOSITORY_NAME>
   ```
2. Build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. Install directly to a connected Android device or emulator:
   ```bash
   ./gradlew installDebug
   ```

---

## 📦 APK Download (CI/CD Pipeline)

Every push to `main` automatically triggers our GitHub Actions pipeline to run all unit tests and compile a production-ready debug APK artifact.

**Direct Artifact Download**: Go to GitHub Actions Runs (Click the latest successful run and download the `app-debug` artifact at the bottom).
