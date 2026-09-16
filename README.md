# Appointment Booking App

A modern, Firebase-powered **appointment scheduling app** for Android, built with **Kotlin** and **Material Design**. Designed for clinics, salons, and consultants — it lets clients book services in a few taps while admins manage services and view all bookings in real time.

![Android](https://img.shields.io/badge/Platform-Android-3DDC84) ![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF) ![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28) ![License](https://img.shields.io/badge/License-MIT-blue)

---

## ✨ Features

- **User Authentication** — Secure sign-up / login with Firebase Auth (email + password)
- **Role-Based Access** — Separate experiences for **Admins** and **Users**
  - **User:** browse services, pick a date & time slot, book instantly, and view booking history
  - **Admin:** add services with price & duration, review all appointments
- **Real-time Booking** — Duplicate-slot detection prevents double-booking the same service/time
- **Appointment History** — See past & upcoming bookings with status
- **Polished Material UI** — Card-based lists, calendars, and Material Components theme

## 🛠 Tech Stack

| Layer      | Technology                                                     |
|------------|----------------------------------------------------------------|
| Language   | Kotlin                                                         |
| UI         | XML layouts + Material Components, ViewBinding-ready          |
| Architecture | MVVM-style (Repository pattern with Coroutines)              |
| Backend    | Firebase Auth + Cloud Firestore                                |
| Build      | Gradle (AGP 8.x), Kotlin 1.9                                   |

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Hedgehog or newer)
- A free **Firebase** project

### 1. Clone & open
```bash
git clone https://github.com/mhklogs/AppointmentBookingApp.git
```
Open the folder in Android Studio and let Gradle sync (`File → Sync Project with Gradle Files`).

### 2. Add Firebase config
This repo includes a **placeholder** `app/google-services.json` for build purposes. To run against your own backend:

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project (or reuse one).
2. Add an **Android app** with package name `com.example.appointmentapp`.
3. Download the generated `google-services.json` and replace `app/google-services.json`.
4. In **Firestore**, create two collections:
   - `services` — each doc: `id`, `name`, `price` (double), `durationMinutes` (int)
   - `appointments` — each doc: `id`, `userId`, `userEmail`, `serviceId`, `serviceName`, `date` (`yyyy-MM-dd`), `timeSlot` (`HH:mm`), `status`
5. In **Authentication → Sign-in method**, enable **Email/Password**.
6. Set Firestore security rules to allow authenticated reads/writes (dev mode), e.g.:
   ```js
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

### 3. Run
Select an emulator or device and press **Run ▶**. Register a new account to try the user flow.

## 📁 Project Structure

```
app/src/main/java/com/example/appointmentapp/
├── data/
│   ├── model/            # User, Service, Appointment data classes
│   └── repository/       # AuthRepository, FirestoreRepository
└── ui/
    ├── Adapters.kt       # RecyclerView adapters
    ├── admin/            # Admin dashboard
    ├── auth/             # Login / registration
    └── user/             # Service list, booking, history
```

## 🧭 Roadmap

- Push notifications & reminders to reduce no-shows
- Time-slot availability grid per service
- Booking cancellation / rescheduling
- Admin analytics dashboard

## 🤝 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

## 📄 License
Released under the [MIT License](LICENSE).