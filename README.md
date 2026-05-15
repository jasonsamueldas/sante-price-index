# Sante Price Index

An Android application built with Kotlin and Jetpack Compose that tracks and visualizes product price trends using live data from the Indian Government Open Data Platform (`data.gov.in`).

The app allows users to monitor commodity prices, analyze historical trends, and access cached data even when offline.

## Features

- Live commodity price tracking
- Data fetched from `data.gov.in`
- Product trend visualization
- Historical price analysis
- Offline caching support
- Modern Jetpack Compose UI
- Product watchlist / tracker
- Responsive Material Design 3 interface
- Persistent local storage for offline usage

## Tech Stack

- Kotlin
- Jetpack Compose
- Material Design 3
- Coroutines
- ViewModel
- Retrofit / HTTP Networking
- JSON Parsing
- Local Storage Caching
- Android Studio

## Data Source

This project uses publicly available commodity price data from:

- https://data.gov.in

The application fetches and processes government-provided datasets to display pricing trends and market information.

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

Make sure you have:

- Android Studio
- Android SDK
- Internet connection for live data
- API key from `data.gov.in`

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/jasonsamueldas/sante-price-index.git
```

### 2. Open in Android Studio

Open the project folder in Android Studio.

### 3. Add API Key

Get an API key from:

```text
https://data.gov.in
```

Add your API key in the appropriate configuration file or constant inside the project.

Example:

```kotlin
const val API_KEY = "YOUR_API_KEY"
```

### 4. Sync Gradle

Allow Android Studio to download and sync all dependencies.

### 5. Run the Application

Connect an Android device or emulator and click:

```text
Run ▶
```

## Example API Response

Example commodity price data:

```json
{
  "records": [
    {
      "commodity": "Rice",
      "state": "Karnataka",
      "market": "Bangalore",
      "price": "65"
    },
    {
      "commodity": "Milk",
      "state": "Karnataka",
      "market": "Bangalore",
      "price": "48"
    }
  ]
}
```

## Offline Support

The app stores previously fetched data locally so users can continue viewing:

- Saved products
- Previous prices
- Trend graphs
- Cached market data

even without an internet connection.

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

- Smart price alerts
- Price prediction using ML
- Advanced filtering and sorting
- Multiple chart types
- Export data to CSV/PDF
- User accounts and cloud sync
- Regional analytics dashboard

## Contributing

Contributions are welcome.

### Steps

1. Fork the repository
2. Create a feature branch

```bash
git checkout -b feature-name
```

3. Commit your changes

```bash
git commit -m "Added new feature"
```

4. Push the branch

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

https://github.com/jasonsamueldas/sante-price-index
