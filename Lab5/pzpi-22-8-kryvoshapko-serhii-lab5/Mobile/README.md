# NutriTrack – Fitness and Nutrition Tracker 📱

**NutriTrack** is a comprehensive Android mobile application designed to help users monitor their
diet, track physical activity, maintain a detailed food diary, and connect with fitness consultants
for personalized guidance.

## ✨ Key Features

### For Users

- **User Authentication** - Secure registration and login with Firebase Auth
- **Personal Goal Setting** - Create customized fitness goals (weight loss, muscle gain,
  maintenance)
- **Comprehensive Logging** - Track meals, water intake, and physical activities
- **Smart Calculations** - Automatic daily calorie and macronutrient needs calculation
- **Progress Visualization** - Interactive charts and graphs to monitor your journey
- **Food Diary** - Detailed meal logging with nutritional information

### For Consultants

- **Consultant Panel** - Professional dashboard for managing clients
- **Client Overview** - Comprehensive view of client progress and data
- **Notes & Recommendations** - Add personalized advice and track client interactions

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Authentication:** Firebase Authentication
- **Cloud Storage:** Firebase Storage
- **Backend:** REST API built with ASP.NET Core
- **Database:** SQL Server (accessed via API)
- **Build System:** Gradle

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio** (Arctic Fox or newer)
- **JDK 8** or higher
- **Android SDK** (API level 21 or higher)
- **Git**

## 🚀 Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/nutritrack-android.git
cd nutritrack-android
```

### 2. Open in Android Studio

- Launch Android Studio
- Select "Open an existing Android Studio project"
- Navigate to the cloned project directory and select it

### 3. Install Dependencies

Android Studio will automatically download all necessary dependencies via Gradle. If not, you can
manually sync by:

- Go to **File → Sync Project with Gradle Files**

### 4. Configure Firebase

#### Set up Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add your Android app to the Firebase project
4. Register your app with your package name

#### Enable Authentication

1. In Firebase Console, go to **Authentication → Sign-in method**
2. Enable **Email/Password** authentication
3. Enable **Google Sign-In** (optional)

#### Download Configuration File

1. Download the `google-services.json` file from Firebase Console
2. Place it in the `app/` directory of your Android project
3. Ensure the file is at the same level as your `app/build.gradle` file

#### Verify Firebase Configuration

Make sure your `build.gradle` files include the necessary Firebase dependencies:

**Project-level `build.gradle`:**

```gradle
classpath 'com.google.gms:google-services:4.3.15'
```

**App-level `build.gradle`:**

```gradle
apply plugin: 'com.google.gms.google-services'
```

### 5. Configure Backend API

1. Update the API base URL in your app configuration
2. Ensure your backend ASP.NET Core API is running
3. Update any necessary API endpoints in the app

### 6. Run the Application

#### Using Android Studio

1. Connect an Android device via USB with **USB debugging enabled**, or
2. Start an Android emulator from AVD Manager
3. Click the **Run** button (▶️) in Android Studio

#### Using Command Line

```bash
./gradlew installDebug
```

The app will build and launch on your selected device or emulator.
