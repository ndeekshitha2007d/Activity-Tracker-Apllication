# 🏃 Activity Tracker

A Java-based Android fitness application designed for real-time daily activity tracking and metrics aggregation. Built using pure **Java, Android ViewBinding, and Health Connect**, the system integrates seamlessly with Android 14's native health infrastructure to track daily steps, calculate distance and calorie burn, and persist local activity data.

---

## 🌟 Key Features

* **Health Connect Integration:** Interoperates with Google's Health Connect architecture to pull aggregated daily step count data securely without requiring background services.
* **Java–Kotlin Coroutine Bridge:** Custom execution pipeline utilizing `BuildersKt.runBlocking` inside background threads to bridge Java code with Kotlin's asynchronous `suspend` functions.
* **Fitness Metrics Computation:** Automatically converts step count into estimated distance covered ($0.762\text{ m/step}$) and active energy expenditure ($0.04\text{ kcal/step}$).
* **Offline Caching:** Utilizes `SharedPreferences` to cache local step state for instantaneous dashboard rendering upon launch.
* **Dynamic Progress Dashboard:** Responsive UI featuring a circular progress bar tracking towards a daily 10,000-step goal, updated instantly via dynamic state sync.

---

## 🛠️ Tech Stack

* **Language:** Java 17 (JDK 17)
* **Framework:** Android SDK (Min SDK: API 26 / Target SDK: API 34)
* **Health Engine:** `androidx.health.connect:connect-client:1.1.0-alpha07`
* **Concurrency:** Kotlin Coroutines (`kotlinx.coroutines.BuildersKt`), Java `ExecutorService`
* **UI Architecture:** Android ViewBinding, Material Components
* **Persistence:** Android `SharedPreferences`
* **IDE & Tooling:** Android Studio, Git

---

## 📁 Project Structure

```text
ActivityTracker/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/activitytracker/
│   │       │   ├── HealthConnectManager.java   # Health Connect API wrapper & coroutine bridge
│   │       │   ├── MainActivity.java           # Permission handling, UI binding & sync logic
│   │       │   └── StepData.java               # Model class for steps, distance & calories
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml       # Main dashboard layout & progress bar
│   │       │   └── values/
│   │       └── AndroidManifest.xml             # Permissions & Health Connect rationale filters
│   └── build.gradle                            # Dependencies & Android SDK config
├── .gitignore
└── README.md