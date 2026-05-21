# WeatherSnap

An Android app that lets you search live weather for any city, capture a photo using a custom CameraX screen, compress the image, add field notes, and save the result as a local report.

---

## Setup

**Requirements**

- Android Studio Hedgehog or newer
- JDK 17
- Android device or emulator running API 26 or higher
- Internet connection for weather and geocoding APIs

**Steps**

1. Clone the repository
2. Open the project in Android Studio
3. Let Gradle sync finish
4. Run the app on a device or emulator (physical device recommended for camera)

No API keys are needed. The app uses Open-Meteo which is free and open.

---

## App Flow

1. Search for a city by typing in the search field (3+ characters to get suggestions)
2. Select a city from the dropdown — weather loads automatically
3. Tap Create Report to open the report screen
4. Tap Capture Photo to open the custom camera
5. Capture the photo — it gets compressed automatically
6. Add any field notes
7. Tap Save Report — the report is saved to local Room DB
8. View all saved reports from the Reports button on the main screen

---

## Project Structure

```
app/src/main/java/com/weathersnap/
├── data/
│   ├── local/
│   │   ├── dao/         ReportDao
│   │   ├── database/    AppDatabase (Room)
│   │   └── entity/      ReportEntity, ReportDraftEntity
│   ├── remote/
│   │   ├── api/         GeocodingApi, WeatherApi (Retrofit)
│   │   └── model/       API response models
│   └── repository/      WeatherRepository
├── di/                  Hilt module (AppModule)
├── domain/model/        City, Weather, WeatherReport
├── ui/
│   ├── navigation/      NavHost and Screen routes
│   ├── screens/
│   │   ├── weather/     WeatherScreen + WeatherViewModel
│   │   ├── report/      CreateReportScreen + CreateReportViewModel
│   │   ├── camera/      CameraScreen (CameraX)
│   │   └── savedreports/ SavedReportsScreen + SavedReportsViewModel
│   └── theme/           Dark Material 3 theme
└── utils/               ImageUtils (compression), WeatherConditionMapper
```

---

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM with ViewModel and StateFlow
- Hilt for dependency injection
- Retrofit + Gson for API calls
- OkHttp logging interceptor (debug builds only)
- Room for local persistence
- CameraX for custom camera
- Coroutines for async work
- Coil for image loading
- Navigation Compose

---

## APIs Used

Both from Open-Meteo. No API key required.

- City suggestions: `https://geocoding-api.open-meteo.com/v1/search`
- Current weather: `https://api.open-meteo.com/v1/forecast`

City suggestions are cached in memory so repeated queries for the same text do not make additional network calls.

---

## Developer Judgment Challenge

The challenge was to make sure an in-progress report survives rotation and process death without creating duplicate saved reports.

**Approach**

A `report_draft` table in Room holds a single draft row (always `id = 1`). When the user opens the Create Report screen, the current weather snapshot is written to the draft immediately. Any changes — photo captured, notes typed — update the draft row. On rotation or process death, the ViewModel restores from this draft on re-init.

The draft is deleted in the same transaction as the final report save, so it can never result in a duplicate. If the user navigates back without saving, the draft persists until they open a new city's report (different `cityName`), at which point the old draft is cleared.

**Image file cleanup**

The raw uncompressed file from CameraX is deleted immediately after compression. If the user discards the report by going back, the compressed file remains on disk until the next report session clears it when a new draft is started. This is a minor tradeoff — the file is small and isolated to internal storage — but a more thorough cleanup could hook into the ViewModel's `onCleared()` to delete any draft image that was never saved.

**Tradeoff**

Using Room for the draft instead of `SavedStateHandle` alone means it survives full process death (not just config changes). The downside is an extra DB write on every state change, but since these writes go through Room on the IO dispatcher they do not block the UI.

---

## Unit Tests

Basic ViewModel tests are in `app/src/test/java/com/weathersnap/WeatherViewModelTest.kt`.

Run them with:
```
./gradlew test
```
