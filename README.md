# မြန်မာ-ရုရှား Language Learner (Android)

Myanmar-Russian Language Learning App - Android Version

## Features

- 🏠 **Home** - Dashboard with stats and feature overview
- 🃏 **Flashcards** - Flip cards for Myanmar/Russian vocabulary
- 🧠 **Quiz** - Multiple choice quiz to test knowledge
- 🎓 **Pro Tutor** - AI-powered chat tutor (Gemini backend)
- 📊 **Progress** - Track your learning progress

## Project Structure

```
myanmar-russian-android/
├── android/                    # Android native project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/myanmarrussian/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── AppState.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── HomeFragment.kt
│   │   │   │   │   ├── FlashcardsFragment.kt
│   │   │   │   │   ├── QuizFragment.kt
│   │   │   │   │   ├── ProTutorFragment.kt
│   │   │   │   │   └── ProgressFragment.kt
│   │   │   │   ├── models/
│   │   │   │   │   ├── Card.kt
│   │   │   │   │   ├── QuizQuestion.kt
│   │   │   │   │   └── ChatMessage.kt
│   │   │   │   └── api/
│   │   │   │       └── TutorApiService.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   └── values/
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── settings.gradle
└── backend/                    # Node.js backend (same as iOS)
    ├── server.js
    ├── package.json
    └── .env.example
```

## Tech Stack

- **Android**: Kotlin + ViewBinding + Material Design 3
- **Navigation**: Bottom Navigation + Fragments
- **Networking**: Retrofit2 + OkHttp
- **TTS**: Android TextToSpeech API
- **Backend**: Node.js + Express + Google Gemini AI

## Setup

### Backend Setup

```bash
cd backend
npm install
cp .env.example .env
# Add your GEMINI_API_KEY to .env
npm start
```

### Android Setup

1. Open `android/` folder in Android Studio
2. Update `BASE_URL` in `TutorApiService.kt` with your backend URL
3. Build and run on device/emulator

## Requirements

- Android Studio Hedgehog or newer
- Android SDK 24+ (Android 7.0)
- Target SDK 34
- Kotlin 1.9+
- Node.js 18+ (for backend)
