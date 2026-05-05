# CareRoute

CareRoute is an Android mobile health-navigation app built for CS501. The app helps users organize symptoms, select the painful body area, answer follow-up questions, and receive a structured care-routing suggestion. It is designed to guide users toward the right type of care, such as emergency care, urgent care, primary care, pharmacy support, or home care.

CareRoute is not a diagnosis tool and does not replace professional medical advice. It is a support tool that helps users describe symptoms more clearly, understand potential urgency, and decide what type of care resource may be appropriate.

---

## Project Overview

CareRoute combines a mobile-first symptom check flow with AI support, local persistence, Firebase cloud services, and family health profile management.

The core user flow is:

```text
Sign in / Emergency Mode
→ Home
→ 3D Body Area Check
→ Symptom Detail
→ Pain Level
→ Gemini Follow-up Questions
→ Structured Care Recommendation
→ Map Search / History Archive
```

The app includes:

- Firebase email/password authentication
- Emergency guest mode for quick access
- 3D body area selection with male and female models
- Anatomically aligned clickable hotspots
- Symptom detail selection
- Pain level slider
- Gemini-generated follow-up questions
- Gemini-generated care recommendation
- Structured result cards with urgency, care level, warning signs, self-care, OTC options, and next steps
- Google Maps search for hospitals, pharmacies, urgent care, and primary care clinics
- Local history archive
- User profile and medical information
- Daily AI-generated health tips
- Personalized checkup suggestions
- Family Hub with family creation, join requests, email invitations, member switching, and shared family context
- Local Room database cache
- Firebase Firestore cloud sync for account, profile, history, family, and app data

---

## Main Features

### 1. Authentication and Emergency Mode

CareRoute supports Firebase email/password authentication. Users can:

- Create an account
- Sign in
- Log out
- Reset password
- Continue in Emergency Mode

Emergency Mode allows quick access without account creation. When Emergency Mode starts, the app signs out the current Firebase session and uses an emergency guest session. This prevents emergency access from accidentally showing a previously signed-in user's cloud identity.

---

### 2. Home Dashboard

The Home screen provides a central starting point for the app. It includes:

- Active patient context
- Start symptom check button
- Daily health tip
- Personalized checkup suggestions
- Recent archive summary
- Quick access to History, Map, and Settings
- Emergency actions, including calling emergency services and finding nearby emergency rooms

Daily health tips and checkup suggestions are generated with Gemini using the user's profile, recent symptom history, and imported medical notes when available.

---

### 3. 3D Body Area Check

The app includes an interactive 3D body viewer using SceneView.

Current 3D model files are stored in:

```text
app/src/main/assets/models/male_model.glb
app/src/main/assets/models/female_model.glb
```

Users can switch between:

- Male model
- Female model
- Front view
- Back view

Each model has clickable hotspot points. Tapping a hotspot opens the Symptom Check screen with the selected body area already passed into the next step.

The 3D screen currently supports:

- Model gender switching
- Front/back rotation
- Hotspot overlay
- Body-area routing into Symptom Check
- Separate tuning values for model scale, model vertical offset, and hotspot coordinates

The hotspot values are intentionally kept as editable constants and coordinate lists so model alignment can be adjusted quickly during UI tuning.

---

### 4. Symptom Check Flow

After selecting a body area, users continue through a symptom-intake process:

1. Confirm or refine the selected body area
2. Enter a symptom description
3. Select a pain level from 0 to 10
4. Answer Gemini-generated follow-up questions
5. Receive a structured care-routing recommendation

The selected body part is carried through the navigation route and used by the Gemini prompt.

---

### 5. Gemini AI Integration

CareRoute uses Gemini for AI-assisted symptom support. Gemini is used for:

- Generating follow-up questions
- Generating final care recommendations
- Producing urgency levels
- Suggesting care type
- Creating map search queries
- Generating daily health tips
- Generating personalized checkup suggestions
- Structuring imported medical record notes

The final assessment response is parsed into structured sections, including:

- Urgency
- Care level
- Place type
- Map query
- Recommendation score
- Summary
- Key points
- Self-care guidance
- OTC options
- Next steps
- Warning signs
- Detailed notes

CareRoute frames Gemini output as navigation support, not as a diagnosis.

---

### 6. Results and Care Recommendation

The result screen presents the Gemini output in mobile-friendly cards.

The result can include:

- Care urgency
- Suggested care level
- Summary
- Key points
- Self-care steps
- OTC options
- Next steps
- Warning signs
- Map search button

If the recommendation includes a map query, the user can open the Map screen with the suggested search term.

---

### 7. Map Search

CareRoute includes a map page using Google Maps Compose. It supports:

- Embedded map preview
- Search keyword input
- Quick buttons for common care categories
- External Google Maps nearby search

Supported quick-search categories include:

- Hospitals
- Pharmacy
- Urgent Care
- Checkup Center / Primary Care

The current app keeps an embedded map inside CareRoute, then opens Google Maps for live nearby search results. A future improvement would be to use the Google Places API to automatically rank nearby facilities by distance, rating, and availability directly inside the app.

---

### 8. History Archive

CareRoute stores completed symptom checks in a history archive. Each saved check includes:

- Record ID
- Patient/person ID
- Patient/person name
- Body part
- Symptom description
- Pain level
- Urgency
- Care level
- Summary
- Map query
- Created timestamp

Users can review past checks and reopen relevant map searches.

---

### 9. Profile, Settings, and Medical Information

The Settings screen stores user and app preferences, including:

- Name
- Birth date
- Age
- Gender
- Height
- Weight
- Address
- Allergies
- Medications
- Conditions
- Emergency contact
- Notification preference
- Dark mode preference
- Accent theme preference

This information is used to personalize Gemini prompts and improve the context of daily health tips, checkup suggestions, and symptom recommendations.

---

### 10. Imported Medical Records

CareRoute supports imported or manually entered medical record notes. These records can be summarized and used as additional context for AI-generated tips and recommendations.

Imported medical records can include:

- Source type
- Source label
- Title
- Summary
- Findings
- Recommended follow-up
- Raw text

---

### 11. Family Hub

Family Hub allows multiple users to share a family context.

Family Hub supports:

- Creating a family
- Generating a family code
- Joining a family with a code
- Sending join requests
- Approving or rejecting join requests
- Inviting members by email
- Viewing family members
- Switching the active patient context
- Leaving a family
- Disbanding a family as the owner

The app does not actually log in as another family member. Instead, it switches the selected patient context inside the app. This keeps the Firebase authentication session separate from the selected family profile.

---

## Tech Stack

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android ViewModel
- Kotlin Coroutines
- Room
- DataStore Preferences
- Retrofit
- Gson
- SceneView

### Firebase

- Firebase Authentication
- Firebase Firestore
- Firebase Analytics
- Google Services Gradle plugin

### Google / Maps

- Google Maps SDK
- Google Maps Compose
- Google Play Services Location
- Google Places dependency
- External Google Maps intent search

### AI

- Gemini API through Retrofit
- Structured prompt design
- Structured response parsing

---

## Project Structure

```text
app/src/main/java/com/example/cs501_final_project
├── MainActivity.kt
├── data
│   ├── AppModels.kt
│   ├── AuthModels.kt
│   ├── AuthViewModel.kt
│   ├── CareRouteViewModel.kt
│   ├── GeminiRepository.kt
│   ├── local
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
│   ├── preferences
│   │   └── AppPreferencesRepository.kt
│   └── remote
│       ├── CloudModels.kt
│       └── RemoteFamilyRepository.kt
├── navigation
│   └── AppNav.kt
├── network
│   ├── GeminiApiService.kt
│   └── GeminiModels.kt
└── ui
    ├── AuthScreen.kt
    ├── BodyPart3DScreen.kt
    ├── BodyPartScreen.kt
    ├── FamilyHubScreen.kt
    ├── FamilyHubViewModel.kt
    ├── HistoryScreen.kt
    ├── HomeScreen.kt
    ├── MapScreen.kt
    ├── ResultScreen.kt
    ├── SettingScreen.kt
    ├── TriageScreen.kt
    ├── components
    └── theme
```

3D model assets are stored in:

```text
app/src/main/assets/models
├── male_model.glb
└── female_model.glb
```

---

## Architecture

CareRoute follows an MVVM-style architecture.

```text
Compose UI
→ Navigation
→ ViewModel
→ Repository / DAO / Remote Repository
→ Room / DataStore / Firebase / Gemini / Maps
```

### UI Layer

The UI layer is built with Jetpack Compose and Material 3. It contains screens, reusable cards, buttons, chips, and form components.

Important UI files include:

- `AuthScreen.kt`
- `HomeScreen.kt`
- `BodyPart3DScreen.kt`
- `HistoryScreen.kt`
- `MapScreen.kt`
- `SettingScreen.kt`
- `FamilyHubScreen.kt`

### Navigation Layer

Navigation is handled in:

```text
navigation/AppNav.kt
```

Main routes include:

```text
home
family_hub
history
map
setting
body3d
detail/{part}
follow_up/{part}/{symptomText}/{painLevel}
```

### ViewModel Layer

Important ViewModels include:

- `AuthViewModel`
- `CareRouteViewModel`
- `FamilyHubViewModel`

`AuthViewModel` handles authentication, registration, password reset, logout, and emergency guest mode.

`CareRouteViewModel` manages local app state, profile data, family profiles, history records, imported records, settings, daily tips, checkup suggestions, and cloud sync.

`FamilyHubViewModel` manages family creation, joining, members, requests, invitations, and family-level actions.

### Repository / Data Layer

Important repository and data files include:

- `GeminiRepository.kt`
- `RemoteFamilyRepository.kt`
- `CareRouteDao.kt`
- `CareRouteDatabase.kt`
- `AppPreferencesRepository.kt`

`GeminiRepository` handles Gemini API calls and response parsing.

`RemoteFamilyRepository` handles Firestore family operations.

`CareRouteDao` and `CareRouteDatabase` handle local Room persistence.

`AppPreferencesRepository` handles DataStore preferences, including selected active person.

---

## Data Storage

CareRoute uses both local and remote storage.

### Local Room Database

The local database is:

```text
care_route_local.db
```

Room stores:

- Self profile
- App settings
- Family members
- History records
- Imported medical records
- Daily health tips
- Checkup suggestions

Room gives the app a local cache and allows the UI to observe stored data through flows.

### DataStore Preferences

DataStore stores lightweight preferences, including:

- Selected active person ID
- Legacy migration completion flag

### Firebase Firestore

Firestore is used for authenticated cloud storage and Family Hub data.

User documents are stored under:

```text
users/{uid}
```

A user document can include:

```text
uid
email
name
birthDate
age
gender
height
weight
address
allergies
medications
conditions
emergencyContact
selectedFamilyId
familyCode
createdAt
lastLoginAt
updatedAt
```

User subcollections include:

```text
users/{uid}/settings/default
users/{uid}/familyProfiles/{memberId}
users/{uid}/historyRecords/{recordId}
users/{uid}/importedMedicalRecords/{recordId}
users/{uid}/dailyHealthTips/{personId}
users/{uid}/checkupSuggestions/{suggestionId}
```

Family documents are stored under:

```text
families/{familyId}
```

A family document can include:

```text
id
familyName
familyCode
ownerUid
ownerEmail
memberUids
roles
createdAt
updatedAt
```

Family subcollections include:

```text
families/{familyId}/members/{memberId}
families/{familyId}/records/{recordId}
families/{familyId}/joinRequests/{requesterUid}
families/{familyId}/emailInvites/{inviteId}
```

Email invitations can also create documents in:

```text
mail
```

This is intended for integration with a Firebase email extension or similar mail-delivery setup.

---

## API Key Setup

The project reads API keys from `local.properties`.

Create or update `local.properties` in the project root:

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

Android Studio may also add:

```properties
sdk.dir=/path/to/your/android/sdk
```

The project uses:

```kotlin
buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
```

`MAPS_API_KEY` is used by Google Maps.

`GEMINI_API_KEY` is used by Gemini API calls.

For a production app, API calls should be routed through a backend or Cloud Function instead of exposing API keys in the Android client.

---

## Firebase Setup

To run the app with Firebase:

1. Create a Firebase project.
2. Add an Android app using package name:

```text
com.example.cs501_final_project
```

3. Download `google-services.json`.
4. Place it in:

```text
app/google-services.json
```

5. Enable Firebase Authentication.
6. Enable Email/Password sign-in.
7. Enable Firestore Database.
8. Add Firestore security rules appropriate for user and family data.

The repository currently includes a `google-services.json` for the course project setup. If running the project with a different Firebase project, replace it with your own file.

---

## How to Run

1. Clone or download the project.
2. Open it in Android Studio.
3. Make sure `local.properties` contains the required API keys.
4. Make sure `app/google-services.json` exists.
5. Sync Gradle.
6. Run the app on an emulator or Android device.

Recommended emulator:

```text
Medium Phone
API 36 or similar
```

Minimum SDK:

```text
24
```

Target SDK:

```text
36
```

---

## Main Demo Flow

A suggested demo flow:

1. Launch the app.
2. Create an account or sign in.
3. Open the Home screen.
4. Tap the main symptom-check button.
5. Select Male or Female model.
6. Tap a hotspot on the 3D body.
7. Confirm the selected body area.
8. Enter a symptom description.
9. Select pain level.
10. Answer Gemini follow-up questions.
11. View the care recommendation.
12. Open the suggested map search.
13. Return to History and confirm the saved record.
14. Open Family Hub and show family creation or member context switching.

---

## Medical Safety and Scope

CareRoute does not provide a medical diagnosis. It is a health-navigation support tool.

The app is designed to:

- Help users describe symptoms
- Help users organize health information
- Suggest possible care direction
- Encourage users to seek urgent help when warning signs are present
- Provide map search support for nearby care resources

The app is not designed to:

- Diagnose disease
- Replace a doctor
- Replace emergency services
- Guarantee medical accuracy
- Provide treatment instructions for serious conditions

For severe symptoms, users should contact emergency services or seek professional care immediately.

---

## Current Limitations

CareRoute is a course project prototype. Current limitations include:

- AI output should always be treated as support, not diagnosis.
- The embedded map currently previews a default map area and opens Google Maps for live nearby search.
- Nearby facility ranking is not fully implemented inside the app.
- Firestore security rules should be strengthened before any real deployment.
- API keys are loaded into the Android build for the project demo; production usage should move Gemini calls behind a backend.
- Local Room data is used as the offline cache, while Firestore is used for cloud sync and family data.
- More testing is needed for network failure, API failure, missing keys, and incomplete user profiles.
- Accessibility testing can be improved.
- Some older experimental screens remain in the codebase but are not part of the primary final navigation flow.

---

## Future Improvements

Future improvements could include:

- Real Places API ranking for nearby care facilities
- Distance, rating, and open-hours sorting for suggested care locations
- Stronger Firestore security rules
- Cloud Functions for Gemini API calls
- Push notifications for family join requests
- Better family permission controls
- More detailed health record sharing permissions
- Offline-first sync conflict handling
- More unit tests for Gemini response parsing
- UI tests for the complete symptom-check flow
- More accessibility improvements
- Additional body model calibration for more screen sizes

---

## Team Responsibilities and Contributions

### Jiahao Hu

Jiahao focused on app structure, Compose UI, navigation, AI integration, authentication, cloud data behavior, and database redesign.

Main contributions included:

- Building major Compose screens
- Connecting the core navigation flow
- Implementing the AI symptom-check process
- Integrating Gemini API logic
- Improving structured Gemini prompt and response handling
- Debugging Firebase Authentication
- Improving Firebase session behavior
- Supporting Firestore-based account data
- Improving Emergency Mode behavior
- Designing and revising Family Hub data flow

### Zhi Gao

Zhi focused on the main app framework, core navigation, 3D interaction, symptom-check flow, persistence, UI integration, and final documentation.

Main contributions included:

- Building the Android project structure
- Creating and improving the core navigation flow
- Building and improving major Compose screens
- Implementing the 3D body viewer
- Adding and updating male and female body models
- Adjusting model size and vertical positioning
- Calibrating body hotspot mappings
- Connecting hotspot selection to Symptom Check
- Improving body-part selection and symptom detail intake
- Adding Room and DataStore local persistence
- Connecting profile, history, medical record, and family-related data layers
- Supporting Family Hub actions such as leave family and disband family
- Fixing UI, navigation, model alignment, and compilation issues during integration
- Preparing and revising final project documentation

---

## AI Reflection

AI was used as a development assistant throughout the project. It supported brainstorming, architecture planning, UI wording, debugging, documentation, and code review. AI was not used blindly; suggestions were reviewed, tested, and modified before being accepted.

### How AI Was Used

AI helped with:

- Brainstorming the app idea
- Designing the user flow
- Planning MVVM-style architecture
- Debugging Kotlin and Compose errors
- Improving Firebase and Firestore structure
- Designing Gemini prompts
- Structuring Gemini response parsing
- Improving UI wording
- Writing and revising final documentation

### Where AI Influenced the Project

AI influenced the app architecture by helping separate UI, ViewModel, repository, local storage, remote storage, and API logic.

AI also influenced the UX. Instead of showing a long unstructured Gemini paragraph, the final result is separated into summary, urgency, care level, warning signs, self-care, OTC options, next steps, and notes. This makes the output easier to read on a phone.

AI also helped debug the 3D body viewer. Model scale, vertical offset, hotspot alignment, and click routing were adjusted through several iterations.

### What AI Helped Accelerate

AI accelerated:

- Kotlin debugging
- Compose UI iteration
- Gemini prompt design
- Firestore schema planning
- README/report writing
- Refactoring repeated code
- Explaining difficult errors

### AI Suggestions That Were Rejected

Some AI suggestions were not used because they were too complex for the timeline or not stable enough for the final demo.

Rejected or postponed ideas included:

- Full 3D mesh raycasting for hotspot selection
- Complete Places API ranking inside the app
- A production backend for all Gemini calls
- Complex multi-role family permissions
- Advanced sensor-based symptom intake

The final version focuses on a stable and understandable implementation suitable for a course project demo.

---

## Conclusion

CareRoute demonstrates a complete mobile health-navigation workflow. It combines Jetpack Compose UI, Firebase Authentication, Firestore, Room, DataStore, Gemini API, Google Maps, and SceneView-based 3D body selection.

The project shows how AI can support symptom organization and care routing while still keeping the app within a safe scope. CareRoute does not diagnose users, but it helps users organize symptoms, understand possible urgency, and find appropriate care resources.

The current implementation provides a strong foundation for future development, including better nearby facility ranking, stronger cloud security, more detailed family permissions, and deeper testing.
