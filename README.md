# Scheduli 📅 

A modern, premium Android productivity and scheduling suite featuring a **Scrum Board** with checklist sub-tasks, a **Calendar Schedule** viewer, and **Simulated Email Sync** integrations for Gmail and Outlook. 

Built entirely in **Kotlin** and **Jetpack Compose** using **Material 3** guidelines and local JSON-based persistence.

---

## 🛠️ Key Features

### 1. Responsive Scrum Board
*   **Adaptive Layout:** Automatically optimizes for screen sizes. In portrait mode, it shows a sleek **Top Tab Row** to toggle between lanes (*To Do*, *In Progress*, *In Review*, *Done*) to avoid cramped columns. In landscape or tablet layouts, it switches to a classic side-by-side scrolling columns layout.
*   **Checklist Sub-tasks:** Break down tasks into sub-tasks (e.g. *Importing gradle libraries* -> Done, *Designing database models* -> Pending). 
*   **Progress Indicators:** Shows completion progress fraction counts (e.g. `2/3`) and an animated progress bar directly on task cards.
*   **Interactive Details Dialog:** Add comments, append new sub-tasks, toggle checkboxes, and shift task statuses.
*   **Activity History Log:** A timeline at the bottom of each task tracking exactly what changed, when comments were added, or when sub-tasks were checked off.

### 2. Calendar Schedule
*   **Horizontal Day Selector:** Tap days of the week to show agenda events.
*   **Combined View:** Displays scheduled meetings side-by-side with Scrum tasks that are due on the selected day.
*   **Custom Local Events:** Easily schedule local meetings and select date, duration, description, and location.

### 3. Integrations Center (Gmail & Outlook Sync)
*   **Mock Connect Flow:** Connect mock Google/Gmail or Microsoft/Outlook accounts to simulate OAuth flows.
*   **Dynamic Data Sync:** Connecting a mock account automatically pulls calendar items (e.g. *Sprint Planning*, *Stakeholder Reviews*) and flagged-email tasks onto the Scrum Board and Calendar schedule.
*   **Activity Verification:** View sync activities logged in the Task detail history feed.

---

## 💻 Tech Stack

*   **UI Framework:** Jetpack Compose (Material 3 components, dynamic themes)
*   **Navigation:** Navigation 3 (Jetpack's next-gen Compose navigation)
*   **Language:** Kotlin (JVM 17)
*   **State Management:** StateFlow, ViewModel, and Coroutines
*   **Data Serialization:** kotlinx.serialization (JSON-based persistence)
*   **Storage:** File-based JSON caching (`context.filesDir/scheduli_data.json`)
*   **Icons:** Material Icons Extended

---

## 📂 Project Architecture

```text
app/src/main/java/com/example/scheduliapp/
│
├── MainActivity.kt       # Application Entry Point & Edge-to-Edge Setup
├── Navigation.kt         # Navigation 3 Host configuration
├── NavigationKeys.kt     # Route keys structure
│
├── data/
│   ├── Models.kt         # Serializable Task, SubTask, Meeting, & Account models
│   └── DataRepository.kt # LocalJsonDataRepository loading and saving data
│
├── theme/
│   ├── Color.kt          # Slate/Indigo/Teal premium dark color palette
│   ├── Theme.kt          # ScheduliAppTheme Configuration
│   └── Type.kt           # Custom Typography
│
└── ui/
    ├── main/
    │   ├── MainScreen.kt          # Bottom Tab Layout container & State router
    │   └── MainScreenViewModel.kt # State management & repository delegate
    │
    └── screens/
        ├── ScrumBoardScreen.kt    # Responsive Kanban board & details checklist
        ├── ScheduleScreen.kt      # Weekly calendar planner & event adder
        └── AccountsScreen.kt      # Integrations, simulation portal, & setup guides
```

---

## 🚀 How to Run the App

1.  Open **Android Studio**.
2.  Click **File -> Open...** and select this directory:
    `C:\Users\themo\.gemini\antigravity\scratch\ScheduliApp`
3.  Let Android Studio sync Gradle.
4.  Plug in a physical Android device or boot up a virtual device (emulator).
5.  Click the green **Run (Play icon)** in Android Studio.
6.  The database will initialize with default mock tasks (complete with pre-configured checklist items!) and meetings.

---

## 🔗 OAuth Production Setup Guide

To move this application from simulation mode to real Google Calendar / Microsoft Graph live APIs:

### 1. Google Calendar Integration
*   Register your package name and SHA-1 fingerprint in the [Google Cloud Console](https://console.cloud.google.com/).
*   Enable the **Google Calendar API**.
*   In your codebase, implement credentials fetching via **Google Credential Manager SDK**, request the `https://www.googleapis.com/auth/calendar` scope, and pass the token to the Google Calendar Builder.

### 2. Outlook / Microsoft Calendar Integration
*   Register your app in the [Microsoft Entra admin center](https://entra.microsoft.com/).
*   Configure the Android Redirect URI using your signature hash.
*   Setup configurations in `app/src/main/res/raw/auth_config.json`.
*   Initialize MSAL PublicClientApplication and call `acquireToken` silent/interactive routines to fetch access tokens for Microsoft Graph calendar endpoints (`https://graph.microsoft.com/v1.0/me/events`).
