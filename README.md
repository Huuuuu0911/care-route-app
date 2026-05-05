# CareRoute Final Report

## Project Overview

CareRoute is an Android mobile application designed to help users navigate basic health concerns more clearly and efficiently. The app allows users to select a body area, describe symptoms, answer AI-generated follow-up questions, and receive a structured care recommendation. Based on the result, the app can guide users toward emergency care, urgent care, primary care, pharmacy support, or home care.

The goal of the project is not to replace doctors or provide a medical diagnosis. Instead, CareRoute is designed as a supportive health navigation tool. It helps users organize symptoms, understand the urgency level of their situation, and decide what kind of care resource may be appropriate.

The app includes multiple major features:

- User authentication through Firebase
- Emergency guest mode for quick access
- 3D body-part selection
- AI-generated follow-up questions
- AI-generated care recommendation
- Google Maps / nearby care search
- History archive
- User profile and medical information
- Family Hub for shared family health access
- Remote database storage using Firebase Firestore

---

## Architecture

CareRoute follows a basic MVVM-style architecture. The project separates UI, state management, remote data operations, and API logic into different layers.

### UI Layer

The UI is built with Jetpack Compose and Material 3. Main screens include:

- `AuthScreen.kt`
- `HomeScreen.kt`
- `BodyPart3DScreen.kt`
- `MapScreen.kt`
- `HistoryScreen.kt`
- `SettingScreen.kt`
- `FamilyHubScreen.kt`

Compose is used to create reusable, mobile-friendly layouts. The app uses cards, buttons, text fields, sliders, chips, and scrollable layouts to organize information clearly. Most screens are designed with a mobile-first layout, using vertical flows and card-based sections.

### Navigation Layer

Navigation is handled through Navigation Compose in `AppNav.kt`. The main app flow connects:

```text
Login / Emergency Mode
→ Home
→ Body Selection
→ Symptom Detail
→ AI Follow-up
→ Result
→ Map / History / Settings / Family Hub
```

Arguments are passed between screens when needed, such as the selected body part, symptom description, and pain level. This creates a clear step-by-step user journey.

### ViewModel Layer

The main ViewModels are:

- `AuthViewModel`
- `CareRouteViewModel`

`AuthViewModel` manages Firebase login, registration, logout, password reset, and emergency guest mode.

`CareRouteViewModel` manages user profile data, selected family member context, history records, imported medical records, settings, daily health tips, and checkup suggestions. The newest version stores user-related data remotely in Firestore instead of relying on local Room storage, which prevents different users from sharing the same local profile.

### Repository Layer

The app uses repository-style classes for external data and remote operations:

- `GeminiRepository.kt`
- `RemoteFamilyRepository.kt`

`GeminiRepository` handles Gemini API calls and prompt formatting.

`RemoteFamilyRepository` handles Firestore operations for Family Hub features, including creating families, joining families, approving requests, inviting users by email, and switching the active family profile view.

This separation keeps API and database logic outside the UI layer, making the code easier to understand and maintain.

---

## Database Usage and Schema

The final app uses Firebase Firestore as the main remote database. Earlier versions used local Room storage, but the final direction moved toward Firestore because the app supports login accounts, family sharing, and cross-device persistence.

### Main Firestore Structure

The general Firestore structure is:

```text
users
 └── {uid}
      ├── uid
      ├── email
      ├── name
      ├── familyId
      ├── activeViewUid
      ├── createdAt
      ├── lastLoginAt
      └── updatedAt
```

Each authenticated user has one document under `users`.

### CareRoute User Data

Each user's health-related app data is stored under:

```text
users
 └── {uid}
      └── careRouteData
           └── main
                ├── profile
                │    └── self
                ├── settings
                │    └── app
                ├── historyRecords
                ├── importedMedicalRecords
                ├── dailyHealthTips
                └── checkupSuggestions
```

This schema separates each user's profile, settings, health history, imported records, daily tips, and checkup suggestions. Since the data is stored under the authenticated user's UID, one user's information does not overwrite another user's information.

### Family Hub Schema

Family Hub uses a separate `families` collection:

```text
families
 └── {familyId}
      ├── id
      ├── familyName
      ├── ownerUid
      ├── memberUids
      ├── createdAt
      └── updatedAt
```

Each family also contains subcollections:

```text
families
 └── {familyId}
      ├── profiles
      │    └── {uid}
      ├── joinRequests
      │    └── {requesterUid}
      └── invitations
           └── {email}
```

The Family Hub supports the following rules:

- A user can only belong to one family at a time.
- A user can create a family only if they are not already in one.
- A user can request to join an existing family.
- Current family members can approve or reject join requests.
- A family member can invite another user by email.
- Family members can view another member's profile through `activeViewUid`.

This does not change the Firebase login session. Instead, it changes the selected profile context inside the app, which is safer than actually logging in as another user.

---

## APIs, Sensors, and Usage

### Firebase Authentication

Firebase Authentication is used for account creation, login, logout, and password reset. The app supports email/password authentication. It also includes an Emergency Mode that allows users to enter the app quickly without creating an account.

For security and data separation, Emergency Mode does not use the previous Firebase session. The app signs out the current Firebase user before entering Emergency Mode, preventing emergency access from accidentally showing a previously logged-in user's data.

### Firebase Firestore

Firestore is used as the main cloud database. It stores user profile data, history records, medical records, app settings, family information, join requests, and invitations. Firestore listeners allow the app to update the UI when remote data changes.

### Gemini API

Gemini API is used for AI-assisted symptom support. The app uses Gemini for several tasks:

- Generating follow-up questions based on the selected body part, symptom description, pain level, and user profile
- Generating structured care recommendations
- Producing urgency levels and suggested care types
- Generating daily health tips
- Generating checkup suggestions
- Structuring manually entered medical records

The Gemini response is requested in a structured format so that the app can parse important fields such as urgency, care level, summary, warning signs, and map query.

### Google Maps

Google Maps is used to support nearby care search. Based on the AI recommendation, the app can guide users toward relevant care resources such as:

- Emergency room
- Urgent care
- Primary care clinic
- Pharmacy

The current version supports map-based search and care navigation. A future improvement would be to use the Places API to automatically fetch, rank, and display the closest three facilities directly inside the app.

### Sensor / Interaction Usage

The app includes mobile-specific interaction design through the 3D body selection screen. Users can interact with a visual body model and select symptom areas. This improves usability compared with only typing symptoms into a form.

Voice input and additional sensor-based features were considered as future improvements. The current implementation focuses on stable touch-based interaction, location/map support, and AI-guided symptom flow.

---

## Team Responsibilities and Contributions

Our team divided the work across UI, architecture, API integration, database work, and testing.

### Jiahao Hu

Jiahao focused on the main app structure, Compose UI, navigation flow, AI integration, and database redesign. His contributions included:

- Building the main Compose screens
- Connecting the core navigation flow
- Implementing the AI symptom-check process
- Integrating Gemini API logic
- Refactoring user data from local storage to Firestore
- Debugging Firebase Authentication and session issues
- Improving Emergency Mode behavior
- Designing and revising the Family Hub structure

### Zhi Gao
Zhi focused on the main app framework, core navigation, 3D body interaction, symptom-check flow, data persistence, UI integration, and project documentation. His contributions included:

- Building the basic app framework and organizing the main Android project structure
- Creating and improving the core navigation flow between Home, History, Map, Settings, 3D Body Viewer, Symptom Check, Result, and Family Hub screens
- Building and improving major Compose screens and reusable UI structure
- Implementing and improving the 3D body viewer
- Adding and updating male and female body models
- Adjusting model size and aligning body hotspot mappings
- Improving body-part selection, detailed symptom area selection, and multi-selection support
- Implementing parts of the symptom check flow, including pain level input, symptom detail input, and follow-up intake
- Helping integrate the Gemini-based assessment flow and structured result UI
- Adding Room and DataStore local persistence and connecting profile, history, medical record, and family-related data layers
- Updating ViewModel and navigation logic to support stored user data and app state
- Supporting Family Hub features, including family data flow, leave family, and disband family actions
- Fixing UI, navigation, model alignment, and compilation issues during integration

---

## AI Reflection

AI was used throughout the semester as a development support tool. It helped with brainstorming, architecture planning, debugging, UI wording, and code explanation. However, AI was not used blindly. Suggestions were reviewed, tested, and often modified before being accepted.

### How AI Was Used Across the Semester

AI was used to help:

- Brainstorm the app idea and user flow
- Break the app into screens and features
- Plan the MVVM architecture
- Debug Compose and Firebase errors
- Improve prompt structure for Gemini API
- Organize the final README/report

AI was especially helpful when the project involved unfamiliar or complicated integrations, such as Firestore schema design, Firebase session handling, and structured Gemini API responses.

### Where AI Influenced Architecture, Code, Testing, and UX

AI influenced the architecture by helping identify the need for clearer separation between UI, ViewModel, repository, and database layers. For example, the project originally mixed some local state and database behavior in ways that caused users to share the same local profile. AI helped identify that the better long-term structure was to store user-specific data remotely under each Firebase UID.

For UX, AI helped simplify the presentation of health information. Instead of showing a long paragraph from Gemini, the app uses structured fields such as summary, urgency, care level, warning signs, and next steps. This makes the result easier for users to understand on a mobile screen.

For testing, AI helped interpret error messages and suggest fixes. For example, issues around Firebase session persistence, password reset, and Firestore schema conflicts were diagnosed and fixed through iterative debugging.

### What AI Helped Accelerate

AI helped accelerate:

- Debugging Kotlin and Compose errors
- Writing repetitive model and repository code
- Creating structured prompts for Gemini
- Improving presentation and documentation language
- Comparing different architecture options

Without AI support, many integration problems would have taken much longer to research manually.

### Why Certain AI Suggestions Were Rejected

Not every AI suggestion was accepted. Some were rejected because they were too complex for the project timeline or not stable enough for the milestone.

For example:

- Some early suggestions used too many local database layers, which made user data separation harder.
- Some suggested Firestore structures were too complicated and created many errors during implementation.
- Some health-related AI outputs were too diagnostic, so the app was adjusted to frame Gemini as a support tool, not a doctor.

---

## Current Limitations and Future Improvements

CareRoute has a strong foundation, but there are still areas for future improvement.

Current limitations include:

- The app does not provide real medical diagnosis.
- Emergency Mode does not persist data after leaving the session.
- Family Hub supports viewing family profiles, but deeper shared health record permissions could be improved.
- More testing is needed for edge cases such as network failure, invalid API keys, and incomplete profiles.

Future improvements could include:

- Automatic nearby facility ranking by distance
- Better family permission controls
- Push notifications for family join requests
- Offline mode for basic symptom entry
- More accessibility testing

---

## Conclusion

CareRoute demonstrates a strong foundation for a mobile health navigation app. It includes multiple Compose screens, ViewModel-based state management, Firebase Authentication, Firestore database usage, Gemini API integration, Google Maps support, and a Family Hub system.

The project shows clear progress toward a complete app while still keeping the scope realistic. The final version focuses on stable architecture, meaningful integrations, and a usable mobile experience. While future improvements are still possible, the current implementation provides a clear direction and a strong base for continued development.
