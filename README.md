# Sante Price Index

A modern Android application built with Kotlin and Jetpack Compose that helps users track product prices, monitor trends, and analyze price changes over time using Firebase as the backend.

## Features

- Real-time product price tracking
- Historical price trend visualization
- Offline support with cached data
- Firebase Realtime Database integration
- Firebase Authentication support
- Modern Jetpack Compose UI
- Product watchlist / tracker
- Price trend graphs
- Responsive and clean interface
- Persistent local storage for offline access

## Tech Stack

- Kotlin
- Jetpack Compose
- Firebase Realtime Database
- Firebase Authentication
- Android Studio
- Material Design 3
- Coroutines
- ViewModel
- State Management

## Screenshots

_Add screenshots here_

## Project Structure

```text
Sante-Price-Index/
│
├── app/
│   ├── src/
│   ├── build.gradle
│
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Android Studio
- Android SDK
- Kotlin
- Firebase Project

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/jasonsamueldas/sante-price-index.git
```

### 2. Open in Android Studio

Open the project folder in Android Studio.

### 3. Connect Firebase

1. Go to Firebase Console
2. Create a new Firebase project
3. Add an Android app
4. Download the `google-services.json` file
5. Place it inside:

```text
app/google-services.json
```

### 4. Sync Gradle

Allow Android Studio to sync all dependencies.

### 5. Run the App

Connect an emulator or Android device and click:

```text
Run ▶
```

## Firebase Database Structure

Example structure:

```json
{
  "products": {
    "Milk": {
      "2026-05-01": 48,
      "2026-05-02": 50,
      "2026-05-03": 49
    },
    "Rice": {
      "2026-05-01": 65,
      "2026-05-02": 67
    }
  }
}
```

## Offline Support

The application supports offline usage by caching previously fetched product and trend data locally.

Features include:

- Cached product tracker
- Offline trend viewing
- Retained previous prices without internet
- Firebase offline persistence

## Build APK

To generate an APK:

```text
Build → Build APK(s)
```

Generated APK location:

```text
app/build/outputs/apk/
```

## Future Improvements

- Push notifications for price drops
- Advanced analytics dashboard
- Multi-user collaboration
- Product categories
- Dark mode improvements
- Export reports to PDF/CSV
- AI-based price prediction

## Contributing

Contributions are welcome.

1. Fork the repository
2. Create a new branch

```bash
git checkout -b feature-name
```

3. Commit changes

```bash
git commit -m "Added new feature"
```

4. Push to GitHub

```bash
git push origin feature-name
```

5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Author

Developed by Jason Samuel Das

GitHub: [jasonsamueldas](https://github.com/jasonsamueldas)

## Repository

[Sante Price Index Repository](https://github.com/jasonsamueldas/sante-price-index?utm_source=chatgpt.com)
