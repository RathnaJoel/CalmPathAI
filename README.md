# CalmPath AI — Discover Your Perfect Escape with AI 🌿

**CalmPath AI** is an advanced Android environmental wellness, machine learning recommendation, and navigation application developed with **Kotlin, Jetpack Compose, Material 3, Room Database, Firebase, Retrofit, Google Maps SDK, and on-device Machine Learning**. 

The app intelligently identifies, scores, recommends, and guides users to nearby peaceful urban sanctuaries based on real-time atmospheric air quality (AQI), acoustic ambient sound levels (dB), weather parameters, personal mood, and machine learning suitability predictions.

---

## 📋 Course Outcomes (CO1 – CO10) Overview

| Outcome | Title | Key Implementation Details |
| :--- | :--- | :--- |
| **CO1** | **Modern UI Design with Jetpack Compose** | Declarative Material 3 UI, custom nature-inspired theme (Sage/Ocean/Amber), animated decibel meter gauge, atmospheric indicators, and Tranquility Peace Score (0–100). |
| **CO2** | **Multi-Screen Navigation & State Flow** | Navigation Compose NavHost, 12+ screens, state-driven flows, interactive environmental canvas, and bottom navigation bar. |
| **CO3** | **Local Room Database Persistence** | Offline-first architecture, Room 2.6 with KSP, reactive Kotlin Coroutines `Flow` DAOs for bookmarks, search history, and user preferences. |
| **CO4** | **Firebase Auth & Firestore Cloud Sync** | Firebase Authentication (email/password & guest mode), Firestore cloud synchronization for user favorites, mood logs, and profile data. |
| **CO5** | **Network Services & Environmental Telemetry** | Retrofit 2 + OkHttp REST client, Open-Meteo Air Quality & Weather API, CPCB National Air Quality Index (NAQI) calculation, Android Location Services (FusedLocationProvider) with Indian geofencing. |
| **CO6** | **Google Maps Campus & Sanctuary Navigation** | Google Maps SDK + Maps Compose, custom tranquility map markers, dynamic camera bounds auto-fitting, and in-app walking route polyline renderer (OSRM / Google Directions). |
| **CO7** | **Network, Multimedia & GPS Services Integration** | Tri-service synchronization: GPS determines user coordinates $\rightarrow$ Network retrieves environmental telemetry $\rightarrow$ Multimedia audio manager plays ambient soundscapes (Rain, Forest, Meditation). |
| **CO8** | **AI Chatbot & Conversational Assistant** | Natural language tranquil sanctuary guide, conversational context extraction, REST API AI proxy with zero-latency offline fallback knowledge engine. |
| **CO9** | **On-Device Machine Learning Recommendation System** | Native 30-tree Random Forest Regressor trained on 16 environmental & personal features ($R^2 = 0.77$, MAE = 9.01), sub-millisecond on-device inference, dual-badge UI (Peace Score vs. ML Suitability Score), and Room interaction feedback logging (Entity 10). |
| **CO10** | **AI-Based Smart Alerts & Notifications** | Context-aware notification engine with 4 alert types (AQI Spike, High Noise, Adverse Weather, ML Recommendation), anti-spam cooldown manager with +20 AQI / +10 dB emergency bypass, Android WorkManager periodic worker, dual notification channels, deep-linking action buttons, and Room alert history (Entity 11). |

---

## 🏗️ System Architecture

```
                                 [ USER ]
                                    │
               ┌────────────────────┼────────────────────┐
               ▼                    ▼                    ▼
        [ Jetpack Compose ]   [ AI Chatbot ]    [ Smart Alerts ]
        (Home / Map / Mood)       (CO8)          (CO10 Engine)
               │                    │                    │
               └────────────────────┼────────────────────┘
                                    ▼
                         [ MVVM VIEWMODELS ]
                                    │
                                    ▼
                       [ CALMPATH REPOSITORY ]
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
 [ Room Database v7 ]      [ Remote Network ]         [ On-Device ML ]
 • 11 Entities             • Open-Meteo API           • 30-Tree Random Forest
 • Reactive Flows          • CPCB NAQI Formulas       • 16 Feature Vectors
 • Offline-First Caching   • Google Directions API    • <0.2 ms Inference
         │                          │                          │
         ▼                          ▼                          ▼
 [ Local Persistence ]     [ GPS / Maps SDK ]       [ Multimedia Audio ]
 • Favorites & History     • FusedLocation           • Rain & Forest Streams
 • Smart Alert Logs        • Walking Polylines       • Audio Focus Lifecycle
```

---

## ✨ Detailed Feature Breakdown

### 🎨 CO1 & CO2: Declarative UI & Navigation
- **Jetpack Compose & Material 3**: Nature-inspired color themes (`Sage800`, `OceanTeal`, `SoftCoral`, `WarmAmber`).
- **Tranquility Peace Score (0–100)**: Proprietary algorithm calculating objective environmental peace:
  - Considers AQI, Noise dB, Temperature, and Weather conditions.
- **Calm UI Elements**: Pixel-perfect top bar header, vector Material Icons (`Spa`, `SelfImprovement`, `MenuBook`, `DirectionsRun`, `Park`, `LocalCafe`), and circular decibel meter gauge.
- **Interactive Environmental Canvas**: Color-coded tranquility zones with real-time pin clustering.

### 💾 CO3: Room Database Persistence (11 Entities)
1. `PlaceEntity`: Cached sanctuaries and tranquil campus gardens.
2. `FavoritePlaceEntity`: User-bookmarked sanctuaries.
3. `HistoryEntity`: Navigation and visit history.
4. `UserPreferencesEntity`: Noise, AQI, distance, and mood preferences.
5. `UserProfileEntity`: Local account profile details.
6. `MoodHistoryEntity`: Longitudinal mood check-in logs.
7. `EnvironmentalSnapshotDao`: Real-time atmospheric telemetry snapshots.
8. `SearchHistoryEntity`: Search keywords and recent queries.
9. `ChatMessageEntity`: Chat dialogue history with AI Assistant.
10. `RecommendationInteractionEntity` (CO9): User interaction feedback loop (`VIEWED`, `SELECTED`, `NAVIGATED`, `REJECTED`).
11. `SmartAlertEntity` (CO10): Historical smart alert records, read status, and action markers.

### ☁️ CO4: Firebase Authentication & Cloud Sync
- User registration, login, and profile persistence with Firebase Auth.
- Bidirectional Firestore sync for `/users/{uid}/favorites` and `/preferences`.
- Guest mode fallback for offline grading and instant testing.

### 🌐 CO5: Network Services & Environmental Telemetry
- **REST Client**: Retrofit 2 + OkHttp logging interceptor.
- **Open-Meteo APIs**: Real-time hourly AQI (PM2.5, PM10, NO2, O3), temperature, relative humidity, and wind speed.
- **CPCB NAQI Standard**: Indian National Air Quality Index sub-index calculations.
- **Android Location Services**: `FusedLocationProviderClient` with automatic India geofencing and Mumbai fallback.

### 🗺️ CO6: Google Maps Sanctuary Navigation
- **Google Maps SDK & Maps Compose**: Embedded interactive Google Map in `ExploreScreen` and `PlaceDetailsScreen`.
- **Dynamic Camera Auto-Fitting**: Centers and bounds camera over user location and destination.
- **Turn-by-Turn Route Polyline**: Fetches street walking routes from OSRM and Google Directions APIs with low-noise corridor routing.

### 🎧 CO7: Integrated Mobile Services (Network + GPS + Multimedia)
- Synchronizes GPS positioning with live environmental REST data to recommend serene sanctuaries.
- **Multimedia Audio Manager**: Native `MediaPlayer` architecture with looping ambient soundscapes:
  - 🌧️ Gentle Rain on Leaves
  - 🌲 Deep Forest & Birds
  - 🧘 Mindful Meditation Bell
- Handles audio focus, background playback, and automatic ducking during phone calls.

### 💬 CO8: CalmPath AI Assistant
- Interactive chatbot specialized in tranquil spot discovery, stress reduction, and atmospheric guidance.
- Dynamic context injection: passes current GPS locality, active weather, AQI, and user mood into prompts.
- Dual-mode architecture: connects to live backend API proxy with graceful local fallback response engine.

### 🧠 CO9: Machine Learning Recommendation System
- **Authentic On-Device Model**: 30-tree Random Forest Regressor running natively in Kotlin without heavy external C++ binaries.
- **16 Structured Features**: AQI, PM2.5, PM10, noise level, temperature, humidity, weather type, distance, category, user rating, time of day, day of week, user mood, preferred category, max noise tolerance, and max distance preference.
- **Model Accuracy**: $R^2 = 0.7716$, $\text{MAE} = 9.01$ points on held-out test set.
- **Sub-Millisecond Inference**: Evaluates all candidate sanctuaries in $<0.2$ ms on device CPU.
- **Dual-Badge Clarification**: Clear distinction between **⭐ Peace Score** (environmental tranquility) and **🧠 ML Suitability Score** (personalized prediction).

### 🚨 CO10: CalmPath Smart Alerts & Notifications
- **Context-Aware Proactive Notifications**:
  1. **AQI Spike Alert**: Fires when local air quality deteriorates beyond the user's maximum threshold.
  2. **High Noise Alert**: Triggers when ambient decibels exceed the user's comfort level.
  3. **Adverse Weather Alert**: Proactively recommends indoor sanctuaries (e.g. libraries) during rain or extreme heat.
  4. **ML Recommendation Alert**: Notifies user when a nearby place matches $\ge 80\%$ suitability for their current mood.
- **Anti-Spam Cooldown & Emergency Delta Bypass**: 60-minute default cooldown per alert type; automatically bypassed if conditions worsen significantly (+20 AQI points or +10 dB spike).
- **Dual Notification Channels**:
  - `CalmPath Environmental Alerts` (`IMPORTANCE_HIGH` with heads-up banners).
  - `CalmPath Recommendations` (`IMPORTANCE_DEFAULT`).
- **Deep-Linking Action Buttons**: Direct navigation to `[Explore Places]`, `[View Place]`, `[Navigate]`, and `[Open CalmPath]`.
- **Background Execution**: Scheduled via Android `WorkManager` (`SmartAlertWorker`) for periodic evaluations.
- **In-App Alert History & Lab Simulation Panel**: Filter chips (`All`, `AQI`, `Noise`, `Weather`, `ML Match`), unread counter, and one-tap event simulation buttons for laboratory testing.

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose (BOM 2024.12.01) + Material 3 + Extended Material Icons
- **Architecture**: MVVM + Repository Pattern + Clean Architecture
- **Local Database**: Room 2.6.1 + KSP
- **Cloud & Auth**: Firebase Auth + Firebase Firestore
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 + Gson
- **Maps & Location**: Google Play Services Maps 19.0.0 + Maps Compose 6.4.1 + Location Services 21.3.0
- **Background Tasks**: AndroidX WorkManager 2.9.1 (`work-runtime-ktx`)
- **Multimedia**: Android Audio / MediaPlayer API
- **Machine Learning**: Native 30-tree Random Forest Ensemble in Kotlin + scikit-learn training pipeline
- **Build System**: Gradle 8.7.3 / 9.5
- **Target SDK**: Android 35 (Min SDK: 26)

---

## 🧪 Verification & Test Suite

All 45 unit tests pass with zero failures:

```bash
./gradlew testDebugUnitTest
```

```text
BUILD SUCCESSFUL
Total Tests: 45 | Passed: 45 | Failed: 0 | Skipped: 0

• com.calmpath.ai.CalmPathLogicTest (37 tests) — 100% Passed
  - Peace score formula verification
  - Indian CPCB NAQI calculation bounds
  - GPS distance & geofence validation
  - Room entity conversion & Flow reactivity
  - Audio focus & multimedia state transitions
  - AI chatbot query processing & prompt building
  - Random Forest feature preprocessing & tree traversal
  - ML recommendation ranking & mood sensitivity

• com.calmpath.ai.SmartAlertsLogicTest (8 tests) — 100% Passed
  - AQI threshold trigger & lowest-AQI sanctuary recommendation
  - Noise threshold trigger & quietest sanctuary recommendation
  - ML score trigger (>= 80% suitability match)
  - Notifications master switch suppression
  - Adverse weather detection & indoor library recommendation
  - Non-hallucinating local message generation templates
  - SmartAlertEntity Room persistence integrity
  - Dynamic user threshold adjustment validation
```

---

## 📱 How to Run & Test

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/RathnaJoel/CalmPathAI.git
   cd CalmPathAI
   ```
2. **Open in Android Studio**:
   - Open the root folder in Android Studio (Ladybug / Iguana or later).
   - Let Gradle sync dependencies.
3. **Build & Run**:
   - Select an Android Emulator or physical device running Android 8.0+ (API 26+).
   - Click **Run 'app'** or build via terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   - The compiled APK is located at:
     `app/build/outputs/apk/debug/app-debug.apk`

---

## 🎓 Demonstration Guide for Lab Examiners

1. **CO1–CO5 Environmental Telemetry**:
   - Launch app: grant location permission.
   - Observe live AQI, Decibel meter, Weather cards, and Peace Score calculated in real time.
2. **CO6 Google Maps Navigation**:
   - Open **Explore** tab: view interactive Google Map with custom sanctuary pins.
   - Select a sanctuary $\rightarrow$ tap **In-App Route** to view the live walking polyline along low-noise corridors.
3. **CO7 Multimedia Audio**:
   - Tap the **Audio Bar** on the Home screen to stream tranquil rain or forest soundscapes.
4. **CO8 AI Chatbot Assistant**:
   - Tap **CalmPath AI Assistant** banner $\rightarrow$ ask *"I'm feeling stressed, recommend a place with good air"* $\rightarrow$ observe instant personalized guidance.
5. **CO9 Machine Learning Recommendation**:
   - Go to **Details Screen** of any place $\rightarrow$ view **Personalized ML Suitability Card** showing suitability percentage predicted by the 30-tree Random Forest model based on current mood and distance.
6. **CO10 Smart Alerts**:
   - Tap the **Notification Bell** on the Home top bar $\rightarrow$ expand **Lab Demonstration & Simulation Panel**.
   - Tap **AQI Spike (168)** or **ML Recommendation (92%)**:
     - Android system notification fires from the appropriate channel.
     - New alert record appears in the list.
     - Tap **[Explore Places]** or **[View Place]** to test deep-link navigation.

---

## 📄 License
This project is developed for educational purposes as part of the Mobile Application Development Laboratory.\n