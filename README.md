# 🎬 Movie App

A modern Android application for browsing, searching, and managing favorite movies — built entirely with **Jetpack Compose** and following **Clean Architecture** principles.

---

## 📸 Overview

Movie App allows users to:
- Browse a paginated movie catalog
- Search movies in real time
- View detailed movie info with trailer playback (YouTube)
- Mark/unmark favorites
- Authenticate with JWT (login, registration, auto token refresh)
- Scan a QR Code to find a movie
- Manage their profile (with photo upload)
- Configure app settings
- Complete an onboarding flow (shown only once)

---

## 🏗️ Architecture

The project follows **Clean Architecture** layered into three well-defined concerns:

```
┌─────────────────────────────────────────────────┐
│               Presentation Layer                │
│  (Screens / ViewModels / Compose UI)            │
├─────────────────────────────────────────────────┤
│                 Domain Layer                    │
│         (Use Cases / Repository Interfaces)     │
├─────────────────────────────────────────────────┤
│                  Data Layer                     │
│   (Repository Impls / Remote API / Local DB)    │
└─────────────────────────────────────────────────┘
```

### Multi-Module Structure

```
movieapp/
├── app/                        # Main application module
│   └── src/main/
│       ├── di/                 # Hilt dependency injection modules
│       ├── domain/             # Use cases
│       ├── data/               # Repository implementations
│       │   ├── remote/         # Retrofit API services + interceptors
│       │   └── local/          # Room database + DAOs
│       ├── model/              # Domain models
│       ├── screens/            # UI screens + ViewModels
│       └── navigation/         # Navigation routes + graph
│
├── core/
│   └── ui/                     # Shared Design System module
│       ├── components/         # CustomButton, LinkButton, GenreChip, etc.
│       └── theme/              # Color, Typography, Theme
│
└── feature/
    └── onboarding/             # Self-contained onboarding feature module
        ├── OnboardingScreen.kt
        └── OnboardingCard.kt
```

---

## 🧩 Layers in Detail

### 🎨 Presentation Layer — `app/screens/`

Each screen follows the **MVI / Unidirectional Data Flow** pattern:
- A `ViewModel` exposes a single `UiState` as a `StateFlow`
- The Composable collects the state and renders accordingly

| Screen       | ViewModel                  | Highlights                                      |
|--------------|----------------------------|-------------------------------------------------|
| Splash       | `SplashViewModel`          | Checks saved token & onboarding status          |
| Onboarding   | `OnboardingViewModel`      | Feature module, shown only once via SharedPrefs |
| Login        | `LoginViewModel`           | JWT auth, saved credentials restore             |
| Sign Up      | `SignUpViewModel`           | Registration with inline error handling         |
| Home         | `HomeViewModel`            | Paginated list + real-time search (debounced)   |
| Details      | `DetailsViewModel`         | Movie details + YouTube trailer player          |
| Favorites    | `FavoritesViewModel`       | Paginated favorites from the API                |
| QR Code      | `QrCodeReaderViewModel`    | CameraX + ML Kit barcode scanning               |
| Profile      | `ProfileViewModel`         | User info + profile picture upload              |
| Settings     | `SettingsViewModel`        | App preferences + logout                        |

---

### 🧠 Domain Layer — `app/domain/`

Pure Kotlin use cases with no Android dependencies. Each use case has a single responsibility:

```
Authentication          Movies                  Onboarding
───────────────         ─────────────────────   ──────────────────────
LoginUseCase            GetMoviesUseCase         IsOnboardingCompletedUseCase
SignUpUseCase           GetMovieByIdUseCase      MarkOnboardingCompletedUseCase
LogoutUseCase           SearchMoviesUseCase
RestoreTokenUseCase     GetFavoriteMoviesUseCase
CheckSavedTokenUseCase  GetFavoriteMovieUseCase
GetSavedCredentialsUseCase  UnfavoriteMovieUseCase

User
─────────────────────
GetUserUseCase
UpdateUserUseCase
UploadProfilePictureUseCase
```

---

### 🗄️ Data Layer — `app/data/`

#### Remote — Retrofit + OkHttp

| Service             | Endpoints                                                                 |
|---------------------|---------------------------------------------------------------------------|
| `AuthApiService`    | `POST /auth/login`, `POST /auth/register`, `POST /auth/refresh`          |
| `MovieApiService`   | `GET /movies`, `GET /movies/search`, `GET /movies/{id}`, favorites CRUD  |
| `UserApiService`    | `GET /users/me`, `PUT /users/me`, `POST /users/me/avatar`                |

#### Auth & Token Management

```
Request ──► AuthInterceptor ──► Adds "Authorization: Bearer <token>" header
                │
                ▼ (401 received)
         TokenAuthenticator ──► Refreshes token via /auth/refresh (NoAuth client)
                │
                ├─ Success ──► Retries original request with new token
                └─ Failure ──► SessionManager.logout() ──► Navigates to Login
```

Two separate OkHttp clients are configured via Hilt qualifier `@NoAuth`:
- **`@NoAuth` client** — No auth header; used only for `/auth/refresh` to avoid circular token refresh loops
- **Default client** — Carries `AuthInterceptor` + `TokenAuthenticator` for all other calls

#### Local — Room Database

| Database              | Entities         | Purpose                        |
|-----------------------|------------------|--------------------------------|
| `AppDatabase`         | `TokenEntity`    | Persists access + refresh JWT  |
| `CredentialsDatabase` | `CredentialEntity` | Stores saved login credentials |

---

### 💉 Dependency Injection — Hilt

| Module              | Provides                                                                 |
|---------------------|--------------------------------------------------------------------------|
| `AppModule`         | OkHttp clients, Retrofit instances, API services, Coil ImageLoader, Use Cases |
| `DatabaseModule`    | Room databases, DAOs, LocalDataSources                                   |
| `RepositoryModule`  | Binds repository interfaces to their implementations                     |

---

## 🗺️ Navigation

Uses **Jetpack Navigation Compose** with **type-safe routes** via `@Serializable` objects:

```kotlin
// Route definitions (kotlinx.serialization)
SplashRoute, OnboardingRoute, LoginRoute, SignUpRoute,
HomeRoute, DetailsRoute(movieId), FavoritesRoute,
QrCodeRoute, ProfileRoute, SettingsRoute
```

The navigation graph handles:
- Initial route resolution (Splash → Onboarding **or** Home)
- Auth-gated navigation (unauthenticated → Login)
- Global logout event via `SessionManager.logoutEvent` (SharedFlow)

---

## 📷 QR Code Scanner

Built with **CameraX** + **ML Kit Barcode Scanning**:
- Camera preview rendered in a Composable via `AndroidView`
- Barcodes analyzed with `MlKitAnalyzer`
- Decoded QR content is matched against movie IDs via `GetMoviesUseCase`
- Displays success ✅ or error ❌ feedback inline

---

## 🎨 Design System — `core:ui`

A dedicated module that houses all shared UI components and theming:

```
core/ui/
├── theme/
│   ├── Color.kt         — App color palette (dark-first)
│   ├── Theme.kt         — MaterialTheme setup
│   └── Type.kt          — Typography scale
├── components/
│   ├── CustomButton.kt
│   ├── LinkButton.kt
│   ├── text/
│   │   ├── TitleText.kt
│   │   ├── BodyText.kt
│   │   └── MovieCardTitle.kt
│   └── badge/
│       ├── GenreChip.kt
│       └── YearBadge.kt
└── ModifierExtensions.kt
```

---

## 🛠️ Tech Stack

| Category            | Library / Tool                                           | Version       |
|---------------------|----------------------------------------------------------|---------------|
| Language            | Kotlin                                                   | 2.3.21        |
| UI                  | Jetpack Compose BOM                                      | 2026.05.01    |
| Navigation          | Navigation Compose                                       | 2.9.8         |
| DI                  | Hilt                                                     | 2.59.2        |
| Networking          | Retrofit + OkHttp                                        | 3.0.0 / 5.3.2 |
| Local Storage       | Room                                                     | 2.8.4         |
| Image Loading       | Coil                                                     | 2.7.0         |
| Camera              | CameraX                                                  | 1.6.1         |
| Barcode Scanning    | ML Kit Barcode Scanning                                  | 18.3.1        |
| Video Playback      | AndroidYouTubePlayer                                     | 13.0.0        |
| Serialization       | kotlinx-serialization                                    | 1.11.0        |
| Code Quality        | Ktlint + Detekt                                          | 14.2.0 / 1.23.8 |
| Testing             | MockK + Coroutines Test                                  | 1.14.9 / 1.11.0 |
| Build               | AGP + KSP                                                | 9.1.1 / 2.3.8 |

---

## 🔐 Security & Session

- JWT **access token** stored in-memory (`TokenStore`) for runtime use
- JWT **access + refresh tokens** persisted in Room (`TokenLocalDataSource`)
- Saved login **credentials** stored in a separate encrypted Room database (`CredentialsLocalDataSource`)
- On 401, `TokenAuthenticator` transparently refreshes the token (thread-safe with `synchronized`)
- If refresh fails → `SessionManager` emits a logout event → user is redirected to Login

---

## ✅ Code Quality

| Tool      | Config File       | Purpose                              |
|-----------|-------------------|--------------------------------------|
| `ktlint`  | `.editorconfig`   | Kotlin code style enforcement        |
| `detekt`  | `detekt.yml`      | Static analysis & complexity checks  |

Run checks:
```bash
./gradlew ktlintCheck
./gradlew detekt
```

---

## 🚀 Getting Started

1. **Clone** the repository
2. Add your `google-services.json` to `app/`
3. Set `BASE_URL` in `local.properties`:
   ```properties
   BASE_URL=https://your-api-url.com/
   ```
4. Open in **Android Studio Hedgehog** or later
5. Run on a physical device or emulator (API 26+)

---

## 📁 Project Structure Summary

```
app/
├── di/                         # AppModule, DatabaseModule, RepositoryModule
├── domain/                     # 17 Use Cases
├── data/
│   ├── remote/                 # AuthApiService, MovieApiService, UserApiService
│   │   ├── AuthInterceptor     # Adds Bearer token to requests
│   │   ├── TokenAuthenticator  # Auto-refreshes token on 401
│   │   └── dto/                # Request/Response DTOs
│   └── local/
│       ├── AppDatabase         # Room — tokens
│       ├── CredentialsDatabase # Room — saved credentials
│       └── dao/                # TokenDao, CredentialsDao
├── model/                      # Movie, AuthResult, PagedResponse, etc.
├── screens/
│   ├── splash/
│   ├── onboarding/
│   ├── login/
│   ├── signup/
│   ├── home/                   # Paginated + search
│   ├── details/                # YouTube player
│   ├── favorites/
│   ├── qrcode/                 # CameraX + ML Kit
│   ├── profile/
│   └── settings/
└── navigation/
    ├── MovieRoutes.kt          # @Serializable route objects
    ├── MovieScreens.kt         # Screen composable mappings
    └── MovieNavigation.kt      # NavHost + graph definition
```
