# NutriTrack Web Admin

**NutriTrack Web Admin** is a Flutter-based administrative dashboard for managing users, consultants, and system data within the NutriTrack health and nutrition ecosystem.

## 🚀 Features

- Firebase Authentication with Google Sign-In
- View and manage users and consultants
- Search by nickname
- Edit and delete accounts
- View system statistics
- Trigger database backups via Web API
- Responsive UI (works on desktop and mobile browsers)

---

## 🛠 Requirements

Make sure you have the following installed:

- [Flutter SDK](https://docs.flutter.dev/get-started/install) (version 3.7.0+ recommended)
- [Dart SDK](https://dart.dev/get-dart)
- Chrome or any modern browser
- Firebase project (for Authentication and Firestore)
- ASP.NET Core Web API backend running on localhost or remote server

---

## 🔧 Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/nutri_track_web_admin.git
   cd nutri_track_web_admin
   ```
2. **Install dependencies:**
   ```bash
   flutter pub get
   ```
3. **Run the project in browser:**
   ```bash
   flutter run -d chrome --web-hostname=localhost --web-port=59409
   ```
