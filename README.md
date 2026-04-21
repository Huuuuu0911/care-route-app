# CareRoute

CareRoute is an Android health support application built with **Kotlin** and **Jetpack Compose**. The project was originally designed to help users describe symptoms and receive a clearer sense of what level of care they may need next. As the project evolved, it expanded beyond a simple symptom-check prototype into a more structured mobile application with **local persistence**, **profile management**, **family member support**, **history tracking**, **map integration**, and a growing foundation for **personalized health recommendations**.

The current version focuses on two major goals:

1. providing a cleaner and more practical symptom-to-care workflow for end users, and  
2. building a more realistic Android application architecture that can support persistent health-related data and future feature growth.

---

## Project Motivation

Many health-related apps either focus only on symptom input or only on static medical information. CareRoute was created to bridge that gap with a more user-centered workflow:

- help users describe symptoms more naturally,
- guide them toward an appropriate next care step,
- make the process more visual and less intimidating,
- allow users to keep relevant health information in one place, and
- support both personal and family-level health organization.

The project is especially aimed at making symptom reporting and care navigation more approachable in a mobile environment.

---

## Project Objectives

The main objectives of CareRoute are:

- to build an Android application with a clear and intuitive health support workflow,
- to support symptom entry through both text-based and body-part-based interaction,
- to provide a result screen that communicates care urgency more clearly,
- to help users locate nearby care options through map integration,
- to persist important information locally instead of relying only on temporary UI state,
- to support both the primary user and family members through profile-based data organization,
- to prepare the app for future personalized recommendations, document handling, and richer record management.

---

## Current Version Overview

The current version of CareRoute includes:

- a multi-screen Android app built with Jetpack Compose,
- symptom-check related UI flow,
- body-part and 3D body selection support,
- result and care guidance screens,
- map and location support for nearby care,
- local persistence with **Room** and **DataStore**,
- personal profile and family member data models,
- saved history record support,
- imported medical record data support,
- settings persistence support,
- infrastructure for daily health tips and personalized checkup suggestions,
- updated ViewModel, navigation, and settings integration.

This version moves the project from a UI-heavy prototype toward a more complete and maintainable Android application.

---

## Core User Flow

CareRoute is structured around a simple but extensible user flow:

**Home → Symptom Check / Body Selection → Result → Map / History / Settings**

The general idea is:

1. the user starts from the home screen,
2. enters symptoms or selects a body area,
3. receives a care-oriented result,
4. reviews or saves that result,
5. checks map-based care options if needed,
6. manages personal data, family data, history, and settings through the rest of the app.

---

## Main Features

### 1. Home Dashboard
The home screen acts as the entry point of the application. It is designed to provide a quick way to begin symptom checking and to grow into a dashboard for health-related summaries, such as tips, history previews, and quick actions.

### 2. Symptom Check Workflow
The project includes a symptom-check workflow that allows the user to move from symptom entry toward a care recommendation. This is the core functional idea behind the app.

### 3. Body Part Selection
CareRoute includes body-area selection screens to make symptom reporting more intuitive. Instead of relying only on free text, the user can indicate where discomfort or symptoms are occurring.

### 4. 3D Body Interaction
The project also includes a 3D body-view feature using SceneView. This is intended to make body-part selection more visual and interactive. It lays the groundwork for more advanced region-based symptom input and follow-up questioning.

### 5. Result and Care Guidance
After symptom-related input, the app presents a result view that communicates urgency and next-step guidance. The current design supports different levels of care guidance, such as:

- emergency-level care,
- urgent-care-level guidance,
- lower-urgency or safer at-home follow-up guidance.

### 6. Nearby Care / Map Support
The project integrates map functionality so that users can search for nearby care locations based on the result or urgency level. This helps connect the assessment flow with a practical next action.

### 7. History Support
The app now has local support for storing previous symptom-check results. This allows the project to move toward a usable history page rather than treating every interaction as temporary.

### 8. Personal Profile Management
The current version includes local storage support for the user’s health profile, including personal and health-related fields such as:

- name,
- birth date,
- age,
- gender,
- phone number,
- height,
- weight,
- address,
- allergies,
- medications,
- conditions,
- emergency contact.

### 9. Family Member Support
CareRoute also supports storing family member profiles, which expands the application beyond a single-user use case. This allows the app to function more like a small family health organizer.

### 10. Imported Medical Record Support
The project now includes local data structures for imported or manually entered medical records. This is an important architectural step because it prepares the app for future upload, parsing, summarization, or record review features.

### 11. Daily Health Tips
The application includes data support for daily health tip content. The data model and local persistence layer are in place so this feature can be stored and reused instead of treated as temporary UI text.

### 12. Personalized Checkup Suggestions
The project also includes support for personalized checkup suggestions. The persistence layer is implemented so the app can store recommendation-oriented content in a structured way.

### 13. Settings Persistence
The settings portion of the app now has real local persistence support. This includes settings-related data such as:

- notification preferences,
- dark mode preference,
- accent theme selection.

---

## Architecture Overview

CareRoute is structured with a layered Android architecture that separates UI, state management, and local data handling.

### UI Layer
The UI is built with **Jetpack Compose** and uses composable screens and reusable components. The goal of the UI layer is to remain focused on presentation and user interaction.

### Navigation Layer
Navigation is managed through a central navigation structure that coordinates the primary app routes and screen transitions.

### ViewModel Layer
The app uses a ViewModel-based state holder to coordinate screen state, business logic, and data flow between the UI and the persistence layer.

### Local Data Layer
The local data layer is implemented with **Room** and includes:

- entities,
- DAO interfaces,
- database configuration,
- type converters,
- entity-to-model mappers.

### Preferences Layer
For lightweight key-value state, the project uses **DataStore**, which is more appropriate than a relational database for simple app preference values.

### Service / Integration Layer
The project also includes integration-ready or service-related support for APIs such as map/location services and Gemini-related health guidance functionality.

---

## Local Persistence Design

One of the most important improvements in the current version is the addition of local persistence.

### Room Database
Room is used for structured local storage. The current database layer includes entities for:

- self profile,
- family members,
- saved history records,
- imported medical records,
- app settings,
- daily health tips,
- checkup suggestions.

### DAO Layer
The DAO layer is responsible for querying, inserting, updating, and deleting local data. This allows the rest of the application to interact with structured data through clearly defined functions rather than raw SQL scattered across the app.

### Type Converters
Because Room cannot directly store all Kotlin types, the project includes converters for values such as:

- enums,
- lists of strings.

### Entity Mappers
Entity mappers are used to convert between Room entities and app-facing data models. This keeps the database layer and UI layer better separated.

### DataStore
DataStore is used for lightweight preferences, such as:

- selected person state,
- legacy migration flags.

This complements Room by handling simpler preference-like values more efficiently.

---

## Data Model Scope

The current persistence layer supports the following categories of data:

### User and Family Data
- self profile information,
- family member information.

### Symptom and Assessment Data
- saved history records,
- symptom-related summaries,
- care-level result information.

### Medical Record Data
- imported or manually entered medical notes,
- findings,
- follow-up recommendations,
- raw text storage.

### Recommendation Data
- daily health tips,
- checkup suggestions.

### App Configuration Data
- settings,
- preferences,
- selected person state.

---

## Technology Stack

### Core Language and Platform
- **Kotlin**
- **Android**

### UI
- **Jetpack Compose**
- **Material 3**

### Local Persistence
- **Room**
- **DataStore Preferences**

### State Management
- **ViewModel**
- Compose state / state hoisting patterns

### Networking and API Support
- **Retrofit**
- **Gson Converter**
- **OkHttp Logging Interceptor**

### Maps and Location
- **Google Maps SDK**
- **Google Maps Compose**
- **Google Play Services Location**
- **Google Places**

### 3D Interaction
- **SceneView**
- Filament-related rendering support

---

## Project Structure

A simplified structure of the project is shown below:

```text
app/
└── src/main/java/com/example/cs501_final_project/
    ├── data/
    │   ├── local/
    │   │   ├── AppSettingsEntity.kt
    │   │   ├── CareRouteConverters.kt
    │   │   ├── CareRouteDao.kt
    │   │   ├── CareRouteDatabase.kt
    │   │   ├── CheckupSuggestionEntity.kt
    │   │   ├── DailyHealthTipEntity.kt
    │   │   ├── EntityMappers.kt
    │   │   ├── FamilyMemberEntity.kt
    │   │   ├── ImportedMedicalRecordEntity.kt
    │   │   ├── SavedCheckRecordEntity.kt
    │   │   └── SelfProfileEntity.kt
    │   ├── preferences/
    │   │   └── AppPreferencesRepository.kt
    │   └── CareRouteViewModel.kt
    ├── navigation/
    │   └── AppNav.kt
    ├── ui/
    │   ├── HomeScreen.kt
    │   ├── HistoryScreen.kt
    │   ├── MapScreen.kt
    │   ├── ResultScreen.kt
    │   ├── SettingScreen.kt
    │   ├── BodyPartScreen.kt
    │   ├── BodyPart3DScreen.kt
    │   └── components/
    └── MainActivity.kt
