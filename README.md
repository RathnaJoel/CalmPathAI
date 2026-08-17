# CalmPath AI – Discover Your Perfect Escape with AI 🌿

**CalmPath AI** is a modern Android environmental wellness and navigation application built with **Kotlin, Jetpack Compose, Material 3, Room Database, and Firebase (Auth & Firestore)**. It recommends nearby tranquil sanctuaries based on mood, air quality index (AQI), acoustic noise levels, and personalized environmental tolerances.

---

## ✨ Features & Course Outcomes

### 🎨 CO1: Jetpack Compose + Material 3 UI
- **Nature-Inspired Wellness Aesthetic**: Custom color palettes with Sage, Ocean Mist, Amber, and Coral accents.
- **Dynamic Theme Engine**: Light, Dark, and System Default themes with real-time reactive switching.
- **Acoustic Decibel Meter**: Circular animated gauge calibrated with EPA noise level tiers (*Very Quiet, Quiet, Moderate, Noisy*).
- **Peace Score Engine**: 0–100 tranquility score calculated from AQI, noise decibels, and greenery density.
- **Air Quality (AQI) & Weather Indicators**: Color-coded badges and EPA atmospheric progress bars.

### 🧭 CO2: Multi-Screen Navigation & Heatmap
- **10 Screens**: Welcome, Mood Selection, Home Dashboard, Explore Heatmap, Place Details, Favorites, History, Profile, Auth (Login/Register), and Settings.
- **Interactive Environmental Heatmap**: 5-tier peacefulness color scale (🔴 Poor $\rightarrow$ 🟠 Moderate-Poor $\rightarrow$ 🟡 Moderate $\rightarrow$ 🟢 Good $\rightarrow$ 🔵 Pristine) with topographic contours and interactive pin markers.
- **Smart Peace Route Navigation**: Route planning prioritizing clean-air and low-noise corridors.

### 💾 CO3: Local Room Database Persistence
- **Room Entities**: `FavoritePlaceEntity`, `HistoryEntity`, `UserPreferencesEntity`.
- **Reactive DAOs**: Kotlin Coroutines `Flow` streams for real-time UI updates.
- **Offline First**: All user bookmarks, browsing history, and environmental preferences persist locally.

### ☁️ CO4: Firebase Authentication & Cloud Sync
- **Firebase Auth**: User registration, login, profile management, and session state.
- **Cloud Firestore**: Bidirectional cloud sync for `/users/{uid}/favorites`, `/preferences`, and `/history`.
- **Smart Guest/Demo Mode**: Offline fallback for instant demonstration and grading without requiring live cloud setup.

---

## 🛠️ Tech Stack

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository Pattern + Clean Architecture
- **Local Persistence**: Room Database (with KSP)
- **Cloud & Auth**: Firebase BOM (Auth & Firestore)
- **Image Loading**: Coil Compose
- **Build System**: Gradle 8.11 / 9.5 (KTS)
- **Target SDK**: Android 35 (Min SDK: 26)

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/CalmPathAI.git
   ```
2. Open the project in **Android Studio** (Ladybug / Iguana or later).
3. Let Gradle sync and run on any Android Emulator or physical device (API 26+).

---

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
