# 🔍 SkyLens Code Review Recommendations

**Review Date:** March 27, 2026
**Reviewer:** Claude Code
**Project:** SkyLens - AI-Powered Airplane Window Explorer
**Current Completion:** ~70%

---

## 🚨 CRITICAL ISSUES (Must Fix for Production)

### 1. **No Backend Infrastructure** ⭐⭐⭐ BLOCKING
**Priority:** CRITICAL
**Impact:** App cannot scale, no cloud backup, limited functionality

**Issues:**
- All data is local-only (no cloud sync)
- Trips lost if user uninstalls app
- Can't share trips across devices
- Limited to 138 hardcoded landmarks
- No way to update landmark data without app update

**Required Implementation:**
- [ ] Rust backend API with Axum framework
- [ ] PostgreSQL + PostGIS database on Fly.io or similar
- [ ] REST API endpoints:
  - `GET /api/landmarks?lat=X&lon=Y&radius=Z`
  - `POST /api/trips` (upload trip)
  - `GET /api/trips` (user's trip history)
  - `GET /api/offline-packs?route=LAX-NRT`
- [ ] Supabase Auth integration for backend
- [ ] Database migrations (airports, landmarks, trips)
- [ ] Populate 50,000+ landmarks from:
  - OpenStreetMap (mountains, peaks, volcanoes, lakes, rivers)
  - Wikidata (cities, historical sites, monuments)
  - Natural Earth Data (geographic features)

**Files to Create:**
```
backend/
├── src/
│   ├── main.rs
│   ├── routes/
│   │   ├── landmarks.rs
│   │   ├── trips.rs
│   │   └── offline_packs.rs
│   ├── models/
│   ├── db/
│   └── utils/
├── Cargo.toml
├── migrations/
└── Dockerfile
```

**Estimated Effort:** 2-3 weeks

---

### 2. **Offline Maps Not Implemented** ⭐⭐⭐ BLOCKING
**Priority:** CRITICAL
**Impact:** App useless during actual flights (no internet on planes)

**Issues:**
- Map currently uses online OSM tiles
- **Primary use case (in-flight viewing) completely broken**
- Requires WiFi/data connection that isn't available at 35,000 feet

**Required Implementation:**
- [ ] Map tile downloader (OpenStreetMap tiles)
- [ ] Tile storage in Room database (`MapTileEntity`)
- [ ] Configure MapLibre to use offline tile source
- [ ] Tile coverage calculator for route corridor
- [ ] Download tiles for zoom levels 4-9 (balance size vs detail)
- [ ] Tile expiration and cleanup (30-day TTL)
- [ ] Pre-flight download UI with:
  - Storage space check
  - Download progress indicator
  - Estimated size display
  - Pause/resume functionality

**Files to Create/Modify:**
```kotlin
// New files needed:
data/service/MapTileDownloader.kt
data/local/entities/MapTileEntity.kt
data/local/dao/MapTileDao.kt
data/repository/MapTileRepository.kt

// Modify:
presentation/ui/components/MapLibreMapView.kt  // Add offline tile source
```

**Technical Details:**
- Tile URL format: `https://tile.openstreetmap.org/{z}/{x}/{y}.png`
- Storage: ~50-200MB per route depending on length
- Tile naming: `{zoom}_{x}_{y}.png`

**Estimated Effort:** 1 week

---

### 3. **Offline Pack Download is Fake** ⭐⭐ HIGH
**Priority:** HIGH
**Impact:** Users can't prepare for flights, "Download" button does nothing real

**Current Implementation:**
```kotlin
// FlightPlanningViewModel.kt
fun downloadOfflinePack() {
    viewModelScope.launch {
        delay(3000)  // 🚨 FAKE DOWNLOAD - just waits 3 seconds
        _uiState.update {
            it.copy(isDownloadingPack = false, isPackDownloaded = true)
        }
    }
}
```

**Required Implementation:**
- [ ] Backend endpoint: `GET /api/offline-packs?departure=LAX&arrival=NRT`
- [ ] Pack generation on backend:
  - Calculate great circle route
  - Find landmarks within viewing distance (500km corridor)
  - Generate tile list for route
  - Bundle into ZIP file
- [ ] Android download implementation:
  - WorkManager for background download
  - Progress tracking (0-100%)
  - Network error handling and retry
  - ZIP extraction
  - Insert landmarks and tiles into Room database
- [ ] Storage management:
  - Check available space before download
  - Display pack size estimate
  - Delete old packs
  - Max storage limit (500MB recommended)

**Files to Create:**
```kotlin
workers/OfflinePackDownloadWorker.kt
data/service/PackDownloader.kt
data/service/PackExtractor.kt
domain/model/OfflinePack.kt
data/local/entities/OfflinePackEntity.kt
```

**Estimated Effort:** 1 week (Android) + 1 week (Backend)

---

### 4. **Limited Landmark Coverage** ⭐⭐ HIGH
**Priority:** HIGH
**Impact:** Most flights show empty map, poor user experience

**Current State:**
- Only **138 landmarks** in database
- Mostly famous landmarks (Mount Fuji, Grand Canyon, Eiffel Tower)
- Large geographic gaps (Africa, South America, Central Asia poorly covered)

**Required Implementation:**
- [ ] Data ingestion pipeline from multiple sources:
  - **OpenStreetMap Overpass API** - Natural features
    - `node[natural=peak][ele>2000]` (mountains over 2000m)
    - `node[natural=volcano]` (volcanoes)
    - `way[natural=lake][name]` (named lakes)
    - `way[waterway=river][name]` (major rivers)
  - **Wikidata SPARQL queries** - Cultural landmarks
    - UNESCO World Heritage Sites
    - Major cities (>500K population)
    - Historical monuments
  - **Natural Earth Data** - Geographic features
    - Country boundaries
    - Ocean names
    - Desert regions
- [ ] Importance scoring algorithm:
  - UNESCO sites: 100 points
  - Peaks >8000m: 95 points
  - Capitals: 90 points
  - Major cities: 70-85 points (by population)
  - Natural features: 60-80 points (by prominence)
- [ ] Deduplication logic (same landmark from multiple sources)
- [ ] Photo URL fetching from Wikimedia for each landmark
- [ ] Database indices for spatial queries (lat/lon with PostGIS)

**Target:** 50,000+ landmarks globally

**Files to Create:**
```
scripts/
├── ingest_osm_landmarks.py
├── ingest_wikidata_landmarks.py
├── ingest_natural_earth.py
├── calculate_importance_scores.py
└── deduplicate_landmarks.py
```

**Estimated Effort:** 2 weeks (data processing + quality validation)

---

## ⚠️ HIGH PRIORITY ISSUES

### 5. **No Trip Cloud Sync** ⭐⭐
**Priority:** HIGH
**Impact:** Users lose trips when switching devices, no backup

**Issues:**
- Trips stored only in local Room database
- No backup to cloud
- Can't access trips from multiple devices
- Data loss if app uninstalled

**Required Implementation:**
- [ ] Backend endpoints:
  - `POST /api/trips` - Upload trip with events
  - `GET /api/trips` - Fetch user's trip history
  - `GET /api/trips/:id` - Fetch specific trip
  - `DELETE /api/trips/:id` - Delete trip
- [ ] Android sync implementation:
  - Upload trip after landing (WorkManager)
  - Download trips on app launch
  - Conflict resolution (local vs cloud timestamps)
  - Sync status indicator
  - Retry failed uploads
- [ ] Trip sharing:
  - Generate shareable trip link
  - Public trip view (web page or deep link)

**Files to Modify:**
```kotlin
data/repository/TripRepository.kt  // Add sync methods
workers/TripSyncWorker.kt          // Create background sync
```

**Estimated Effort:** 1 week

---

### 6. **AI Stories Not Pre-Generated** ⭐⭐
**Priority:** HIGH
**Impact:** High API costs, slow response times, rate limit issues

**Current Implementation:**
- AI narration exists but generic
- Landmark detail screens have photos but minimal AI content
- Stories generated on-demand (slow + expensive)

**Required Implementation:**
- [ ] Pre-generate AI stories for top 1,000 landmarks
- [ ] Store in database with `ai_story` field
- [ ] Batch generation script:
  ```python
  # scripts/generate_landmark_stories.py
  for landmark in top_landmarks:
      story = claude_api.generate_story(landmark)
      db.update(landmark.id, ai_story=story)
      time.sleep(1)  # Rate limiting
  ```
- [ ] Fallback to on-demand generation for rare landmarks
- [ ] Story versioning (update stories periodically)
- [ ] Cache stories in Room database after first fetch

**Benefits:**
- Instant story display (no API latency)
- Reduced API costs (one-time generation vs per-user)
- Better user experience
- Works offline

**Estimated Effort:** 3-4 days (script + testing)

---

### 7. **No Error Handling for API Failures** ⭐
**Priority:** MEDIUM-HIGH
**Impact:** App breaks silently when APIs fail

**Current Issues:**
```kotlin
// ClaudeApiClient.kt - No proper error handling
suspend fun generateLandmarkStory(landmark: Landmark): String {
    return try {
        val response = httpClient.post("...") {
            // ...
        }
        response.body<ClaudeResponse>().content.first().text
    } catch (e: Exception) {
        "Error generating story"  // 🚨 Generic error, no retry, no logging
    }
}
```

**Required Implementation:**
- [ ] Exponential backoff retry logic
- [ ] Circuit breaker pattern (stop retrying after N failures)
- [ ] Proper error types:
  - `NetworkError` - No internet
  - `RateLimitError` - API quota exceeded
  - `AuthError` - Invalid API key
  - `ServerError` - 5xx responses
- [ ] User-friendly error messages in UI
- [ ] Fallback content when AI unavailable
- [ ] Error telemetry (track failure rates)

**Files to Create/Modify:**
```kotlin
domain/error/ApiError.kt           // Create error types
ai/RetryHandler.kt                 // Create retry logic
ai/ClaudeApiClient.kt             // Modify - add error handling
ai/GeminiApiClient.kt             // Modify - add error handling
presentation/ui/components/ErrorBanner.kt  // Create error UI
```

**Estimated Effort:** 3 days

---

### 8. **No Rate Limiting on AI Calls** ⭐
**Priority:** MEDIUM-HIGH
**Impact:** API quota exhaustion, unexpected bills

**Current Issues:**
- Flight narrator calls AI every 60 seconds
- No daily usage limits
- No cost tracking
- Users could rack up huge API bills

**Required Implementation:**
- [ ] Daily usage limits:
  - Free tier: 50 AI calls/day
  - Paid tier: Unlimited (user configures)
- [ ] Usage tracking in Room database:
  ```kotlin
  data class ApiUsageEntity(
      val date: LocalDate,
      val provider: String,  // "gemini" or "claude"
      val callCount: Int,
      val estimatedCost: Double
  )
  ```
- [ ] Show usage in Settings screen
- [ ] Disable AI features when quota exceeded
- [ ] Cache AI responses aggressively
- [ ] Background cleanup of old usage data

**Files to Create:**
```kotlin
data/local/entities/ApiUsageEntity.kt
data/local/dao/ApiUsageDao.kt
data/repository/ApiUsageRepository.kt
ai/UsageTracker.kt
```

**Estimated Effort:** 2 days

---

## 🛠 MEDIUM PRIORITY ISSUES

### 9. **No Unit Tests** ⚠️
**Priority:** MEDIUM
**Impact:** Bugs not caught early, hard to refactor safely

**Current State:**
- Test files exist but incomplete
- No actual test execution
- 0% code coverage

**Required Implementation:**
- [ ] Add test dependencies to `build.gradle.kts`:
  ```kotlin
  testImplementation("io.mockk:mockk:1.13.9")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
  testImplementation("app.cash.turbine:turbine:1.0.0")
  ```
- [ ] Write tests for critical paths:
  - ViewModels (all 6)
  - Repositories (data layer)
  - GeoCalculator (spatial math)
  - GpsFilter (Kalman filter)
  - AI API clients
- [ ] Set up CI/CD to run tests on PR
- [ ] Aim for 80%+ coverage on business logic

**Target Test Count:** 50+ unit tests

**Estimated Effort:** 1 week

---

### 10. **Airport Search Not Implemented** ⚠️
**Priority:** MEDIUM
**Impact:** User must type exact IATA codes, poor UX

**Current Implementation:**
```kotlin
// FlightPlanningScreen.kt
OutlinedTextField(
    value = departureAirport,
    onValueChange = { departureAirport = it },
    label = { Text("Departure (e.g., LAX)") }  // 🚨 User must know IATA code
)
```

**Required Implementation:**
- [ ] Autocomplete airport search:
  - Search by city name: "Los Angeles" → LAX
  - Search by airport name: "Heathrow" → LHR
  - Search by IATA code: "NRT" → Narita
- [ ] Display search results with:
  - Airport name
  - City
  - IATA code
  - Country
- [ ] Recent airports (last 5 used)
- [ ] Popular routes suggestions

**Files to Create:**
```kotlin
presentation/ui/components/AirportSearchField.kt
presentation/ui/components/AirportSearchResults.kt
```

**Estimated Effort:** 2-3 days

---

### 11. **No Photo Gallery Swiping** ⚠️
**Priority:** MEDIUM
**Impact:** Poor UX for landmarks with multiple photos

**Current Implementation:**
- Shows first photo only
- No way to view other photos for landmarks

**Required Implementation:**
- [ ] Horizontal pager for photo gallery
- [ ] Page indicators (dots)
- [ ] Photo count badge (e.g., "3/5")
- [ ] Swipe gestures
- [ ] Zoom capability for photos

**Files to Modify:**
```kotlin
presentation/ui/screens/landmark/LandmarkDetailScreen.kt
```

**Estimated Effort:** 2 days

---

### 12. **No Network State Handling** ⚠️
**Priority:** MEDIUM
**Impact:** Poor UX when network unavailable

**Current Issues:**
- App doesn't detect network state
- Tries API calls even when offline
- No "offline mode" indicator
- Confusing error messages

**Required Implementation:**
- [ ] Network state monitoring:
  ```kotlin
  class NetworkMonitor @Inject constructor(
      @ApplicationContext context: Context
  ) {
      val isOnline: StateFlow<Boolean>
  }
  ```
- [ ] Offline mode banner in UI
- [ ] Disable online-only features when offline:
  - AI narration
  - Photo downloads
  - Trip sync
  - Map tiles (until offline tiles implemented)
- [ ] Queue operations for when online
- [ ] Show cached content when offline

**Files to Create:**
```kotlin
data/network/NetworkMonitor.kt
presentation/ui/components/OfflineBanner.kt
```

**Estimated Effort:** 2 days

---

### 13. **Security: API Keys in Version Control Risk** ⚠️
**Priority:** MEDIUM
**Impact:** Potential key exposure

**Current Issues:**
```kotlin
// build.gradle.kts
val claudeApiKey = properties.getProperty("CLAUDE_API_KEY")
    ?: System.getenv("CLAUDE_API_KEY")
    ?: "sk-ant-placeholder"  // 🚨 Hardcoded placeholder
```

**Recommendations:**
- [ ] Never commit `local.properties`
- [ ] Add validation - fail build if keys are placeholders
- [ ] Use Android Keystore for production keys
- [ ] Implement key rotation mechanism
- [ ] Add ProGuard rules to obfuscate BuildConfig
- [ ] Consider backend proxy for API calls (hide keys server-side)

**Files to Modify:**
```kotlin
// Add to build.gradle.kts:
if (claudeApiKey == "sk-ant-placeholder") {
    throw GradleException("CLAUDE_API_KEY not configured in local.properties")
}
```

**Estimated Effort:** 1 day

---

### 14. **No Database Migrations Strategy** ⚠️
**Priority:** MEDIUM
**Impact:** Future schema changes will break existing installations

**Current Issues:**
- Room database has `version = 1`
- No migration paths defined
- Adding fields will crash app on update

**Required Implementation:**
- [ ] Define migration strategy:
  ```kotlin
  val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
          database.execSQL("ALTER TABLE landmarks ADD COLUMN ai_story TEXT")
      }
  }
  ```
- [ ] Test migrations with real user data
- [ ] Add `fallbackToDestructiveMigration()` only for debug builds
- [ ] Document migration process

**Files to Modify:**
```kotlin
data/local/SkyLensDatabase.kt  // Add migrations
```

**Estimated Effort:** 2 days

---

## 🔧 TECHNICAL DEBT

### 15. **Missing Input Validation** ⚠️
**Priority:** MEDIUM
**Impact:** Crashes, data corruption, security issues

**Issues Found:**

**a) No IATA code validation:**
```kotlin
// FlightPlanningViewModel.kt
fun setDepartureAirport(iata: String) {
    _departureAirport.value = iata  // 🚨 No validation - accepts "ABC123" or "!@#"
}
```

**Should be:**
```kotlin
fun setDepartureAirport(iata: String) {
    val cleaned = iata.trim().uppercase()
    if (cleaned.matches(Regex("^[A-Z]{3}$"))) {  // Only 3 letters
        _departureAirport.value = cleaned
    }
}
```

**b) No coordinate validation:**
```kotlin
// LandmarkEntity.kt
data class LandmarkEntity(
    val latitude: Double,  // 🚨 Could be 999.0 or -999.0
    val longitude: Double
)
```

**Should validate:**
- Latitude: -90 to +90
- Longitude: -180 to +180

**c) No API key format validation:**
- Claude API keys should start with `sk-ant-`
- Gemini API keys should start with `AIza`

**Required Implementation:**
- [ ] Input validators for all user inputs
- [ ] Database constraints in Room
- [ ] API key format checks at app startup
- [ ] Coordinate validation before storage

**Files to Create:**
```kotlin
domain/validation/Validators.kt
```

**Estimated Effort:** 2 days

---

### 16. **Memory Leaks Risk** ⚠️
**Priority:** MEDIUM
**Impact:** App slowdown, crashes on long flights

**Potential Issues:**

**a) Location updates not cleaned up:**
```kotlin
// FlightMapViewModel.kt
override fun onCleared() {
    super.onCleared()
    trackingJob?.cancel()
    narrationJob?.cancel()
    mockFlightJob?.cancel()
    // 🚨 But FlightTracker itself might not stop location updates
}
```

**b) Bitmap loading without size limits:**
- Wikimedia photos could be huge (5MB+ each)
- No downsampling before loading
- Could cause OutOfMemoryError

**Required Implementation:**
- [ ] Verify FlightTracker properly stops location updates
- [ ] Add image size limits:
  ```kotlin
  implementation("io.coil-kt:coil-compose:2.5.0")
  AsyncImage(
      model = ImageRequest.Builder(context)
          .data(photoUrl)
          .size(800, 800)  // Downsample
          .build()
  )
  ```
- [ ] Add LeakCanary for debug builds
- [ ] Profile memory usage during long simulation
- [ ] Clear old map tiles from memory

**Files to Modify:**
```kotlin
presentation/ui/screens/landmark/LandmarkDetailScreen.kt
location/FlightTracker.kt
```

**Estimated Effort:** 2 days

---

### 17. **No Proper Logging Strategy** ⚠️
**Priority:** MEDIUM
**Impact:** Hard to debug production issues

**Current Issues:**
- Mix of `Log.d()`, `Log.e()`, and `println()`
- No structured logging
- Logs removed in release builds (ProGuard)
- No crash reporting

**Required Implementation:**
- [ ] Implement Timber for structured logging
- [ ] Log levels by build type:
  - Debug: VERBOSE
  - Release: ERROR only
- [ ] Add Crashlytics/Sentry for crash reporting
- [ ] Log important events:
  - Flight started/stopped
  - Landmarks discovered
  - API calls (success/failure)
  - Database operations
- [ ] PII scrubbing (remove user emails, coordinates in production logs)

**Files to Create:**
```kotlin
util/Logger.kt
```

**Dependencies to Add:**
```kotlin
implementation("com.jakewharton.timber:timber:5.0.1")
// Or
implementation("io.sentry:sentry-android:7.0.0")
```

**Estimated Effort:** 1 day

---

### 18. **Inefficient Database Queries** ⚠️
**Priority:** MEDIUM
**Impact:** Slow performance with large landmark dataset

**Issues Found:**

**a) No database indices on spatial queries:**
```sql
-- LandmarkEntity currently has no indices on lat/lon
-- Spatial queries will be slow with 50K+ landmarks
```

**Should add:**
```kotlin
@Entity(
    tableName = "landmarks",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["type"]),
        Index(value = ["importance_score"])
    ]
)
```

**b) N+1 query problem in trip history:**
```kotlin
// TripHistoryViewModel might fetch trips, then fetch events for each trip
// Should use @Relation to fetch in one query
```

**c) No query pagination:**
- Landmark queries return all results
- Should implement paging for large result sets

**Required Implementation:**
- [ ] Add database indices
- [ ] Use Room `@Relation` for nested queries
- [ ] Implement Paging 3 for landmark list
- [ ] Add query LIMIT clauses
- [ ] Profile slow queries with Database Inspector

**Estimated Effort:** 2-3 days

---

## 📱 UI/UX IMPROVEMENTS

### 19. **No Onboarding Tutorial**
**Priority:** MEDIUM
**Impact:** New users don't understand app features

**Current State:**
- OnboardingScreen.kt exists but is empty
- No feature walkthrough
- No permission rationale before asking

**Required Implementation:**
- [ ] 3-4 screen onboarding flow:
  1. "What is SkyLens" - Intro with illustration
  2. "How it works" - GPS + AI explanation
  3. "Prepare for flight" - Download offline pack tutorial
  4. "Privacy" - What data is collected
- [ ] Interactive elements (swipe, tap demos)
- [ ] Skip button for returning users
- [ ] Show once on first launch (SharedPreferences flag)

**Estimated Effort:** 3 days

---

### 20. **Dark Mode Not Optimized**
**Priority:** LOW-MEDIUM
**Impact:** Poor nighttime visibility, battery drain on OLED

**Current Issues:**
- Theme supports dark mode but not optimized
- Map tiles are bright (battery drain)
- Some colors may have poor contrast

**Required Implementation:**
- [ ] True black background for OLED (#000000 vs #121212)
- [ ] Dark map style for MapLibre
- [ ] Adjust all color contrasts for WCAG AA compliance
- [ ] Test in actual dark environment
- [ ] Add pure black mode toggle in settings

**Estimated Effort:** 1-2 days

---

### 21. **No Empty States**
**Priority:** LOW
**Impact:** Confusing UI when no data

**Missing Empty States:**
- Trip history with no trips
- Map with no landmarks nearby
- Chat with no AI response yet
- Download list with no packs

**Required Implementation:**
- [ ] Illustration + text for each empty state
- [ ] CTA buttons ("Start your first flight!")
- [ ] Helpful tips

**Files to Create:**
```kotlin
presentation/ui/components/EmptyState.kt
```

**Estimated Effort:** 1 day

---

## 🔒 SECURITY ISSUES

### 22. **No Location Permission Rationale** ⚠️
**Priority:** MEDIUM
**Impact:** Play Store rejection risk, user distrust

**Current Issues:**
- App asks for BACKGROUND_LOCATION without clear explanation
- Google requires explicit rationale for sensitive permissions

**Required Implementation:**
- [ ] Permission rationale screen before system prompt:
  ```
  "SkyLens needs your location to:
  • Track your position during flights
  • Identify nearby landmarks
  • Generate trip history

  We never share your location with third parties."
  ```
- [ ] Separate request for foreground vs background location
- [ ] Explain why background location is needed
- [ ] Handle "Don't ask again" scenario

**Files to Modify:**
```kotlin
presentation/ui/screens/permissions/PermissionsScreen.kt  // Add detailed rationale
```

**Estimated Effort:** 1 day

---

### 23. **No Data Privacy Controls** ⚠️
**Priority:** MEDIUM
**Impact:** GDPR compliance issues, Play Store rejection

**Missing:**
- [ ] Privacy policy webpage (required by Play Store)
- [ ] Data deletion button in settings
- [ ] Opt-out of AI features
- [ ] Clear cache option
- [ ] Export user data (GDPR right to data portability)
- [ ] Anonymous usage analytics opt-out

**Required Implementation:**
- [ ] Host privacy policy at public URL
- [ ] Add "Delete my data" button:
  - Clear local database
  - Delete cloud trips (when backend exists)
  - Sign out
- [ ] Settings toggles:
  - Share usage statistics: ON/OFF
  - AI features: ON/OFF
  - Location tracking: ON/OFF

**Files to Create:**
```kotlin
data/repository/PrivacyRepository.kt
presentation/ui/screens/settings/PrivacySettingsScreen.kt
```

**Estimated Effort:** 2 days

---

### 24. **SQL Injection Risk in Raw Queries** ⚠️
**Priority:** LOW-MEDIUM
**Impact:** Potential security vulnerability (low risk in current code)

**Found in:**
```kotlin
// If any @RawQuery usage exists without proper parameter binding
@RawQuery
fun customQuery(query: SupportSQLiteQuery): List<Landmark>
```

**Recommendation:**
- [ ] Audit all database queries
- [ ] Use Room's type-safe query builders
- [ ] Never concatenate user input into SQL strings
- [ ] Use parameterized queries

**Current Assessment:** Low risk (no user-controlled raw queries found)

**Estimated Effort:** 1 day (audit only)

---

## 🎨 CODE QUALITY ISSUES

### 25. **TODO Comments in Production Code** ⚠️
**Priority:** LOW-MEDIUM
**Impact:** Incomplete features, confusion

**Found:**
```bash
$ grep -r "TODO" android/app/src/main/java/ | wc -l
28
```

**Examples:**
```kotlin
// TODO: Implement real pack download
// TODO: Add photo gallery
// TODO: Handle errors properly
```

**Required Implementation:**
- [ ] Convert all TODOs to GitHub issues
- [ ] Link issues in code comments:
  ```kotlin
  // FIXME: Issue #123 - Implement real pack download
  ```
- [ ] Remove or implement all TODOs before v1.0 release
- [ ] Add linter rule to prevent new TODOs in main branch

**Estimated Effort:** 1 day (documentation)

---

### 26. **Magic Numbers Throughout Code** ⚠️
**Priority:** LOW
**Impact:** Hard to maintain, unclear intent

**Examples:**
```kotlin
delay(3000)  // What is this delay for?
if (distance < 500) {  // 500 what? km? miles?
val radius = 300.0  // Magic number
```

**Required Implementation:**
- [ ] Extract to named constants:
  ```kotlin
  object FlightConstants {
      const val NARRATION_INTERVAL_MS = 60_000L
      const val VISIBILITY_RADIUS_KM = 500.0
      const val PREDICTION_LOOKAHEAD_MINUTES = 10
      const val MOCK_FLIGHT_SPEED_KMH = 850.0
  }
  ```
- [ ] Document units in variable names
- [ ] Group related constants

**Files to Create:**
```kotlin
util/FlightConstants.kt
util/MapConstants.kt
util/AiConstants.kt
```

**Estimated Effort:** 1 day

---

### 27. **No Code Documentation** ⚠️
**Priority:** LOW
**Impact:** Hard for other developers to contribute

**Current State:**
- Most classes lack KDoc comments
- Complex algorithms not explained
- No architecture decision records

**Required Implementation:**
- [ ] Add KDoc to all public APIs:
  ```kotlin
  /**
   * Calculates the great circle distance between two points on Earth.
   *
   * Uses the Haversine formula for accuracy up to ~0.5% for distances
   * under 1000km. For longer distances, consider Vincenty formula.
   *
   * @param lat1 Latitude of first point in degrees
   * @param lon1 Longitude of first point in degrees
   * @param lat2 Latitude of second point in degrees
   * @param lon2 Longitude of second point in degrees
   * @return Distance in kilometers
   */
  fun haversineDistance(lat1: Double, lon1: Double,
                        lat2: Double, lon2: Double): Double
  ```
- [ ] Document complex algorithms (Kalman filter, great circle route)
- [ ] Add architecture decision records (ADR)
- [ ] Create CONTRIBUTING.md guide

**Estimated Effort:** 2-3 days

---

### 28. **Hardcoded Strings in UI** ⚠️
**Priority:** LOW
**Impact:** Can't internationalize, hard to maintain

**Examples:**
```kotlin
Text("Start Flight")  // Should be in strings.xml
Text("Download Offline Pack")  // Hardcoded
```

**Required Implementation:**
- [ ] Move all UI strings to `res/values/strings.xml`
- [ ] Use `stringResource(R.string.start_flight)`
- [ ] Prepare for i18n (internationalization)
- [ ] Support multiple languages in future:
  - Japanese (many Japan flights)
  - Spanish
  - French
  - German

**Estimated Effort:** 1 day

---

## 📊 PERFORMANCE ISSUES

### 29. **No Background Work Optimization** ⚠️
**Priority:** MEDIUM
**Impact:** Battery drain, background restrictions

**Issues:**
- Flight tracking service runs continuously
- No battery optimization
- May be killed by Android Doze mode

**Required Implementation:**
- [ ] Request battery optimization exemption:
  ```kotlin
  if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
      // Show dialog explaining why
      startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS))
  }
  ```
- [ ] Use `WorkManager` for non-urgent tasks:
  - Trip upload
  - Photo downloads
  - AI story generation
- [ ] Implement location update batching
- [ ] Reduce GPS update frequency when stationary
- [ ] Profile battery usage with Battery Historian

**Estimated Effort:** 3 days

---

### 30. **No Image Caching Strategy** ⚠️
**Priority:** MEDIUM
**Impact:** Slow photo loading, excessive network usage

**Current Issues:**
- Photos re-downloaded every time
- No disk cache limits
- Could fill device storage

**Required Implementation:**
- [ ] Configure Coil image cache:
  ```kotlin
  ImageLoader.Builder(context)
      .memoryCache {
          MemoryCache.Builder(context)
              .maxSizePercent(0.25)  // 25% of app memory
              .build()
      }
      .diskCache {
          DiskCache.Builder()
              .directory(context.cacheDir.resolve("image_cache"))
              .maxSizeBytes(50 * 1024 * 1024)  // 50MB max
              .build()
      }
      .build()
  ```
- [ ] Implement cache eviction (LRU)
- [ ] Show cache size in settings
- [ ] "Clear cache" button

**Estimated Effort:** 1 day

---

### 31. **Inefficient Landmark Queries** ⚠️
**Priority:** MEDIUM
**Impact:** Slow map updates, high CPU usage

**Current Issue:**
```kotlin
// Queries all landmarks, filters in memory
val allLandmarks = landmarkRepository.getAllLandmarks()
val nearby = allLandmarks.filter {
    geoCalculator.haversineDistance(...) < visibilityRadius
}
```

**Should be:**
```kotlin
// Query with spatial filter in database
@Query("""
    SELECT * FROM landmarks
    WHERE (latitude BETWEEN :minLat AND :maxLat)
      AND (longitude BETWEEN :minLon AND :maxLon)
""")
fun getLandmarksInBounds(minLat: Double, maxLat: Double,
                         minLon: Double, maxLon: Double): Flow<List<LandmarkEntity>>
```

**Required Implementation:**
- [ ] Add bounding box pre-filter in SQL
- [ ] Use spatial indices
- [ ] Implement R-tree index for PostGIS backend
- [ ] Paginate landmark results
- [ ] Only query visible map bounds

**Files to Modify:**
```kotlin
data/local/dao/LandmarkDao.kt
data/repository/LandmarkRepository.kt
```

**Estimated Effort:** 2 days

---

## 🧪 TESTING GAPS

### 32. **No Integration Tests** ⚠️
**Priority:** MEDIUM
**Impact:** Component interactions not validated

**Missing:**
- [ ] ViewModel + Repository integration tests
- [ ] Database query tests
- [ ] Navigation flow tests
- [ ] API client integration tests (with mock server)
- [ ] GPS tracking end-to-end tests

**Estimated Effort:** 1 week

---

### 33. **No UI Tests** ⚠️
**Priority:** MEDIUM
**Impact:** UI regressions not caught

**Missing:**
- [ ] Compose UI tests for all screens
- [ ] User journey tests:
  - Sign in → Plan flight → Start → View landmarks → Stop → History
- [ ] Screenshot tests for visual regression
- [ ] Accessibility tests (TalkBack, large text)

**Required Dependencies:**
```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.3")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
```

**Estimated Effort:** 1 week

---

### 34. **No Performance Benchmarks** ⚠️
**Priority:** LOW
**Impact:** Unknown performance characteristics

**Missing:**
- [ ] Benchmark critical operations:
  - Landmark search query time
  - Map tile rendering FPS
  - Memory usage during 12-hour flight
  - Battery drain per hour
  - AI API response times
- [ ] Use Jetpack Microbenchmark library
- [ ] Set performance budgets

**Estimated Effort:** 2 days

---

## 📦 BUILD & DEPLOYMENT ISSUES

### 35. **No CI/CD Pipeline** ⚠️
**Priority:** MEDIUM
**Impact:** Manual testing, slow release cycle

**Missing:**
- [ ] GitHub Actions workflow
- [ ] Automated builds on PR
- [ ] Run unit tests automatically
- [ ] Lint checks (ktlint)
- [ ] APK artifact upload
- [ ] Deploy to Play Store internal track

**Files to Create:**
```yaml
.github/workflows/android.yml
```

**Estimated Effort:** 2 days

---

### 36. **No Release Signing Configuration** ⚠️
**Priority:** MEDIUM
**Impact:** Can't publish to Play Store

**Current State:**
- Using debug keystore only
- No release signing key

**Required Implementation:**
- [ ] Generate release keystore
- [ ] Configure signing in `build.gradle.kts`:
  ```kotlin
  signingConfigs {
      release {
          storeFile = file("../release.keystore")
          storePassword = System.getenv("KEYSTORE_PASSWORD")
          keyAlias = "skylens"
          keyPassword = System.getenv("KEY_PASSWORD")
      }
  }
  ```
- [ ] Store keystore securely (not in Git)
- [ ] Document signing process

**Estimated Effort:** 1 day

---

### 37. **ProGuard Rules Incomplete** ⚠️
**Priority:** MEDIUM
**Impact:** Release builds may crash due to over-obfuscation

**Current Issues:**
- Basic ProGuard rules exist
- May need additional rules for:
  - Kotlin serialization
  - Room database
  - Retrofit/HTTP clients
  - MapLibre native code

**Required Implementation:**
- [ ] Test release build thoroughly
- [ ] Add keep rules for reflection-based libraries
- [ ] Optimize shrinking rules
- [ ] Test with R8 full mode

**Estimated Effort:** 1 day

---

## 🌐 PLAY STORE REQUIREMENTS

### 38. **Missing Play Store Assets**
**Priority:** MEDIUM (before launch)
**Impact:** Can't publish to Play Store

**Required Assets:**
- [ ] Privacy policy webpage (legally required)
  - Host at `skylens.app/privacy` or GitHub Pages
  - Include: data collected, third-party services, user rights
- [ ] App screenshots (minimum 4):
  1. Flight planning screen
  2. Map with landmarks
  3. Landmark detail with AI story
  4. Trip history
- [ ] Feature graphic (1024×500px)
- [ ] App icon (512×512px - high res version)
- [ ] Short description (80 chars max)
- [ ] Full description (4000 chars max)
- [ ] Promotional video (optional but recommended)

**Estimated Effort:** 2 days (design + copywriting)

---

### 39. **Data Safety Form Not Completed**
**Priority:** MEDIUM
**Impact:** Play Store submission blocked

**Required:**
- [ ] Document all data collection:
  - Location data (why, how long stored)
  - User email (from Google Sign-In)
  - Trip history (stored locally + cloud)
  - Photos viewed (Wikimedia URLs)
- [ ] Declare third-party services:
  - Google Sign-In
  - Claude API / Gemini API
  - Wikimedia Commons
  - MapLibre
- [ ] Encryption declaration (data in transit + at rest)

**Estimated Effort:** 1 day (documentation)

---

## 🔮 NICE-TO-HAVE FEATURES

### 40. **No Social Sharing Features**
**Priority:** LOW
**Impact:** Limited viral growth potential

**Could Add:**
- [ ] Share trip on social media (Twitter, Instagram)
- [ ] Generate trip card image (map + stats)
- [ ] Public trip gallery
- [ ] Follow other travelers
- [ ] Landmark check-ins

**Estimated Effort:** 1-2 weeks

---

### 41. **No AR Camera Overlay**
**Priority:** LOW (future enhancement)
**Impact:** Would be a standout feature

**Vision:**
- Point camera at window
- See AR labels on landmarks in real-time
- Requires ARCore, device orientation, bearing calculation

**Estimated Effort:** 3-4 weeks

---

### 42. **No Multi-Language Support**
**Priority:** LOW (for global audience)
**Impact:** Limited to English speakers

**Could Add:**
- [ ] Japanese (many Japan flights)
- [ ] Spanish (LATAM market)
- [ ] French (Europe)
- [ ] German (Europe)
- [ ] Mandarin (China market)

**Estimated Effort:** 1 week per language (translation + testing)

---

## 📋 SUMMARY & ACTION PLAN

### Immediate Priorities (Next 2 Weeks)

#### Must Do (Blocking Real Usage):
1. ✅ Build Rust backend API (2 weeks)
2. ✅ Implement offline map tiles (1 week)
3. ✅ Real offline pack download (1 week)
4. ✅ Populate 50K+ landmark database (2 weeks)

#### Should Do (Quality):
5. ⚠️ Add unit tests (1 week)
6. ⚠️ Implement error handling (3 days)
7. ⚠️ Add rate limiting (2 days)
8. ⚠️ Fix memory leaks (2 days)

#### Nice to Do (Polish):
9. ⏳ Onboarding tutorial (3 days)
10. ⏳ Dark mode optimization (2 days)
11. ⏳ Photo gallery swiping (2 days)

---

### Before Play Store Launch:

**Required:**
- [ ] Privacy policy webpage
- [ ] Play Store assets (screenshots, descriptions)
- [ ] Data safety form
- [ ] Release signing setup
- [ ] Test on 5+ different devices
- [ ] Internal testing with 20+ users

**Timeline:** 1 month after backend complete

---

## 🎯 Recommended Development Order

### Phase 1: Make It Work (Critical Path)
**Goal:** App usable for actual flights

1. Build backend API (2 weeks)
2. Implement offline maps (1 week)
3. Real offline pack download (1 week)
4. Pre-generate AI stories for top 1,000 landmarks (3 days)

**Result:** App works during real flights

---

### Phase 2: Make It Stable (Quality)
**Goal:** Production-ready quality

5. Add comprehensive unit tests (1 week)
6. Implement error handling (3 days)
7. Add rate limiting (2 days)
8. Fix performance issues (3 days)
9. Security audit (2 days)

**Result:** App is reliable and secure

---

### Phase 3: Make It Shine (Polish)
**Goal:** Great user experience

10. Onboarding tutorial (3 days)
11. Empty states (1 day)
12. Dark mode optimization (2 days)
13. Photo gallery improvements (2 days)
14. Settings enhancements (2 days)

**Result:** App is delightful to use

---

### Phase 4: Make It Public (Launch)
**Goal:** Play Store ready

15. Privacy policy webpage (1 day)
16. Play Store assets (2 days)
17. Release build setup (1 day)
18. Beta testing with 20+ users (1 week)
19. Fix bugs from beta (3 days)
20. Submit to Play Store (1 day)

**Result:** App available to public

---

## 📈 Effort Estimate Summary

| Category | Tasks | Estimated Effort |
|----------|-------|------------------|
| **Critical (Backend + Offline)** | 4 | 6 weeks |
| **High Priority (Quality)** | 10 | 3 weeks |
| **Medium Priority (UX)** | 15 | 2 weeks |
| **Low Priority (Polish)** | 13 | 2 weeks |
| **Total** | 42 | **13 weeks** (~3 months) |

---

## 🎓 Key Takeaways

### Strengths:
- ✅ Solid architecture (Clean Architecture + MVVM)
- ✅ Modern tech stack (Compose, Room, Hilt)
- ✅ Core features working (GPS, AI, maps)
- ✅ Good UI/UX design
- ✅ 70% complete already

### Critical Gaps:
- ❌ No backend (app is local-only toy)
- ❌ No offline maps (doesn't work on planes)
- ❌ Limited landmarks (138 vs 50,000+ needed)
- ❌ Fake offline pack download

### Risk Areas:
- 🔴 **Technical debt:** 28 TODOs, no tests, magic numbers
- 🔴 **Security:** API keys, permissions, GDPR compliance
- 🔴 **Performance:** Memory leaks, inefficient queries, battery drain

### Recommendations:
1. **Focus on backend first** - Without it, app can't scale
2. **Implement offline maps second** - Critical for use case
3. **Don't add new features until technical debt is paid**
4. **Get to MVP fast** - Launch with 1,000 landmarks, expand later

---

## 📞 Questions for Product Owner

Before continuing development, clarify:

1. **Launch timeline** - When do you want to launch?
2. **Backend hosting budget** - Fly.io costs ~$10-30/month
3. **AI API budget** - Claude API could be $50-500/month depending on usage
4. **Target markets** - Which regions to prioritize for landmark coverage?
5. **Monetization strategy** - Free with ads? Freemium? Paid up-front?
6. **Team size** - Solo developer or team?
7. **Quality bar** - MVP or polished product?

---

**Review Status:** ✅ Complete
**Next Action:** Prioritize issues and create implementation roadmap
**Confidence Level:** 🟢 HIGH - Clear path to production-ready app
