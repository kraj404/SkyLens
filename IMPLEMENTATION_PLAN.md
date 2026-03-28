# SkyLens Implementation Plan
**Created:** March 27, 2026
**Based On:** Code Review Recommendations + WAVES_STATUS.md
**Current Completion:** 70%
**Target:** Production-Ready App

---

## 🎯 Strategic Overview

### Critical Insight
The app is **70% complete** but has **two blocking issues** that prevent real-world usage:
1. **No backend infrastructure** - app is local-only
2. **No offline map tiles** - map doesn't work on planes (primary use case)

Without these, the app is essentially a sophisticated demo that can't fulfill its core purpose.

---

## 📊 Issue Prioritization Matrix

### BLOCKING (Must Fix for Real Usage)
| Issue | WAVES Status | Code Review # | Impact | Effort |
|-------|--------------|---------------|--------|--------|
| Backend Infrastructure | Wave 8 - Not Started | #1 | App can't scale | 2-3 weeks |
| Offline Map Tiles | Wave 7 - Online Only | #2 | Doesn't work on flights | 1 week |
| Offline Pack Download | Wave 3 - Fake | #3 | Download does nothing real | 1 week |
| Limited Landmarks (138) | Wave 3 Note | #4 | Poor coverage | 2 weeks |

### HIGH PRIORITY (Quality & Reliability)
| Issue | WAVES Status | Code Review # | Impact | Effort |
|-------|--------------|---------------|--------|--------|
| Trip Cloud Sync | Wave 9 - Local Only | #5 | Data loss risk | 1 week |
| AI Stories Pre-Generation | N/A | #6 | Slow + expensive | 3-4 days |
| Error Handling | N/A | #7 | Silent failures | 3 days |
| Rate Limiting | N/A | #8 | API cost explosion | 2 days |
| Unit Tests | N/A | #9 | Hard to refactor | 1 week |

### MEDIUM PRIORITY (UX & Security)
| Issue | WAVES Status | Code Review # | Impact | Effort |
|-------|--------------|---------------|--------|--------|
| Airport Search | N/A | #10 | Poor UX | 2-3 days |
| Photo Gallery Swiping | N/A | #11 | Limited photos | 2 days |
| Network State Handling | N/A | #12 | Confusing offline | 2 days |
| API Key Security | N/A | #13 | Key exposure risk | 1 day |
| Database Migrations | N/A | #14 | Update crashes | 2 days |
| Input Validation | N/A | #15 | Data corruption | 2 days |
| Memory Leaks | N/A | #16 | App slowdown | 2 days |
| Logging Strategy | N/A | #17 | Hard to debug | 1 day |
| Database Indices | N/A | #18 | Slow queries | 2-3 days |
| Onboarding | Wave 11 - Empty | #19 | User confusion | 3 days |
| Dark Mode | N/A | #20 | Battery drain | 1-2 days |
| Empty States | N/A | #21 | Confusing UI | 1 day |
| Location Rationale | Wave 12 Implied | #22 | Play Store reject | 1 day |
| Privacy Controls | Wave 12 Implied | #23 | GDPR/Play Store | 2 days |

### LOW PRIORITY (Technical Debt & Polish)
| Issue | Code Review # | Impact | Effort |
|-------|---------------|--------|--------|
| SQL Injection Audit | #24 | Low risk | 1 day |
| TODO Comments | #25 | Clutter | 1 day |
| Magic Numbers | #26 | Maintenance | 1 day |
| Code Documentation | #27 | Collaboration | 2-3 days |
| Hardcoded Strings | #28 | i18n blocked | 1 day |
| Battery Optimization | #29 | Battery drain | 3 days |
| Image Caching | #30 | Network waste | 1 day |
| Query Optimization | #31 | CPU usage | 2 days |
| Integration Tests | #32 | Regression risk | 1 week |
| UI Tests | #33 | Visual regression | 1 week |
| Performance Benchmarks | #34 | Unknown perf | 2 days |
| CI/CD Pipeline | #35 | Manual testing | 2 days |
| Release Signing | #36 | Play Store block | 1 day |
| ProGuard Rules | #37 | Release crashes | 1 day |
| Play Store Assets | #38 | Can't publish | 2 days |
| Data Safety Form | #39 | Submission block | 1 day |

### FUTURE ENHANCEMENTS (Not Required for v1.0)
| Issue | Code Review # | Impact | Effort |
|-------|---------------|--------|--------|
| Social Sharing | #40 | Viral growth | 1-2 weeks |
| AR Camera Overlay | #41 | Standout feature | 3-4 weeks |
| Multi-Language | #42 | Global audience | 1 week/lang |

---

## 🚀 Implementation Roadmap

### Phase 1: BLOCKING ISSUES (Make It Work)
**Goal:** App usable during actual flights
**Timeline:** 6 weeks
**Status:** Critical path to MVP

#### Week 1-2: Backend Infrastructure (#1)
**Priority:** CRITICAL - Nothing else can proceed without this

**Tasks:**
- [ ] Set up Rust project with Axum framework
- [ ] Configure PostgreSQL + PostGIS on Fly.io or Railway
- [ ] Implement authentication (Supabase JWT validation)
- [ ] Create database schema:
  - `airports` table (7,000 entries)
  - `landmarks` table with PostGIS geography type
  - `trips` table with user_id foreign key
  - `trip_events` table
  - `offline_packs` table
  - `pack_jobs` table for async processing
- [ ] Implement API endpoints:
  - `GET /api/health` - Health check
  - `GET /api/landmarks?lat=X&lon=Y&radius=Z` - Spatial query
  - `POST /api/trips` - Upload trip
  - `GET /api/trips` - User's trip history
  - `POST /api/offline-packs` - Request pack generation
  - `GET /api/offline-packs/:id` - Check pack status
  - `GET /api/offline-packs/:id/download` - Download ZIP
- [ ] Implement great circle route calculation (Rust)
- [ ] Deploy to Fly.io with Docker
- [ ] Set up Cloudflare R2 for pack storage
- [ ] Add basic logging (tracing crate)
- [ ] Write integration tests for API endpoints

**Files to Create:**
```
backend/
├── src/
│   ├── main.rs                    # Axum server setup
│   ├── routes/
│   │   ├── mod.rs
│   │   ├── landmarks.rs           # Spatial queries
│   │   ├── trips.rs               # Trip CRUD
│   │   └── offline_packs.rs       # Pack generation
│   ├── models/
│   │   ├── mod.rs
│   │   ├── landmark.rs
│   │   ├── trip.rs
│   │   └── offline_pack.rs
│   ├── db/
│   │   ├── mod.rs
│   │   └── pool.rs                # PostgreSQL connection pool
│   ├── auth/
│   │   └── jwt.rs                 # Supabase JWT validation
│   ├── geo/
│   │   ├── great_circle.rs        # Route calculation
│   │   └── spatial_queries.rs     # PostGIS helpers
│   └── utils/
│       ├── error.rs               # Error types
│       └── logger.rs              # Tracing setup
├── Cargo.toml
├── migrations/
│   ├── 001_create_airports.sql
│   ├── 002_create_landmarks.sql
│   ├── 003_create_trips.sql
│   ├── 004_create_pack_jobs.sql
│   └── 005_add_indices.sql
├── Dockerfile
├── fly.toml
└── README.md
```

**Dependencies:**
```toml
[dependencies]
axum = "0.7"
tokio = { version = "1", features = ["full"] }
sqlx = { version = "0.7", features = ["postgres", "runtime-tokio-rustls", "macros"] }
tower = "0.4"
tower-http = { version = "0.5", features = ["cors"] }
serde = { version = "1", features = ["derive"] }
serde_json = "1"
tracing = "0.1"
tracing-subscriber = "0.3"
geojson = "0.24"
postgis = "0.9"
jsonwebtoken = "9"
uuid = { version = "1", features = ["v4", "serde"] }
```

**Success Criteria:**
- ✅ Backend deploys successfully to Fly.io
- ✅ API returns landmarks within 500km of test coordinate
- ✅ Can create and retrieve trips
- ✅ Health endpoint returns 200 OK
- ✅ Authentication works with Supabase tokens

---

#### Week 3: Offline Map Tiles (#2)
**Priority:** CRITICAL - Core use case doesn't work without this

**Tasks:**
- [ ] Create `MapTileEntity` Room entity:
  ```kotlin
  @Entity(tableName = "map_tiles")
  data class MapTileEntity(
      @PrimaryKey val id: String, // "{z}_{x}_{y}"
      val zoom: Int,
      val x: Int,
      val y: Int,
      val imageData: ByteArray,
      val downloadedAt: Long,
      val expiresAt: Long // 30 days TTL
  )
  ```
- [ ] Create `MapTileDao` with spatial queries
- [ ] Implement `MapTileDownloader` service:
  - Calculate tile coverage for route corridor
  - Download tiles from OSM (zoom 4-9)
  - Store in Room database
  - Progress tracking
  - Respect OSM usage policy (1 tile/sec max)
- [ ] Create `MapTileRepository` with:
  - `downloadTilesForRoute(departure, arrival)`
  - `getTile(z, x, y): ByteArray?`
  - `cleanupExpiredTiles()`
  - `getStorageUsedMB(): Float`
- [ ] Configure MapLibre to use offline tiles:
  ```kotlin
  map.setStyle(Style.Builder()
      .fromUri("asset://offline_style.json")
      .withSource(...)
  )
  ```
- [ ] Add tile download UI:
  - Estimated storage required
  - Download progress
  - Pause/resume
  - Delete tiles option in settings
- [ ] Implement tile cleanup worker (runs weekly)
- [ ] Test offline mode (airplane mode on device)

**Files to Create:**
```
android/app/src/main/java/com/skylens/
├── data/
│   ├── local/
│   │   ├── entities/MapTileEntity.kt
│   │   └── dao/MapTileDao.kt
│   ├── service/MapTileDownloader.kt
│   └── repository/MapTileRepository.kt
├── workers/MapTileCleanupWorker.kt
└── util/TileCalculator.kt
```

**Technical Details:**
- Tile URL: `https://tile.openstreetmap.org/{z}/{x}/{y}.png`
- Coverage: ±200km corridor from route
- Zoom levels: 4 (country), 5, 6, 7, 8, 9 (regional)
- Storage: ~100-300MB per route
- OSM rate limit: 1 tile/second max
- Tile expiration: 30 days (comply with OSM policy)

**Success Criteria:**
- ✅ Map displays fully in airplane mode
- ✅ Tiles download without errors
- ✅ Storage usage shown accurately
- ✅ Old tiles cleaned up automatically
- ✅ Respects OSM usage policy

---

#### Week 4: Real Offline Pack Download (#3)
**Priority:** CRITICAL - Users need pre-flight downloads

**Tasks:**
- [ ] Refactor `PackDownloadWorker` to use backend API:
  ```kotlin
  // Current: Fake download with local data
  // New: Real HTTP download from backend
  ```
- [ ] Call `POST /api/offline-packs`:
  ```json
  {
    "departure": "LAX",
    "arrival": "NRT",
    "includeTiles": true,
    "includePhotos": true,
    "tileZoomLevels": [4, 5, 6, 7, 8, 9]
  }
  ```
- [ ] Backend generates pack asynchronously:
  - Calculate route and landmarks
  - Generate tile list
  - Fetch photos for each landmark
  - Generate AI stories (batch)
  - Create ZIP file
  - Upload to R2 storage
  - Mark job as complete
- [ ] Android polls for completion:
  ```kotlin
  while (status != "completed") {
      delay(5000)
      status = checkPackStatus(jobId)
      updateProgress(status.progress)
  }
  ```
- [ ] Download ZIP from R2
- [ ] Extract and insert into Room:
  - Landmarks → `landmarks` table
  - Photos → cache directory
  - Tiles → `map_tiles` table
  - Metadata → `offline_packs` table
- [ ] Handle errors gracefully:
  - Network failures (retry with backoff)
  - Storage full (warn user)
  - Corrupt ZIP (re-download)
  - Backend timeout (show status)
- [ ] Update trip history integration (already done)

**Files to Modify:**
```
android/app/src/main/java/com/skylens/
├── workers/PackDownloadWorker.kt     # Replace fake with real
├── data/repository/OfflinePackRepository.kt
├── data/service/PackExtractor.kt     # Create - ZIP handling
└── data/remote/BackendApiClient.kt   # Create - API calls
```

**Success Criteria:**
- ✅ Downloads actual data from backend
- ✅ Progress shows real percentage (not fake)
- ✅ Works with network interruptions
- ✅ Creates trip history entry
- ✅ Map and photos work offline after download

---

#### Week 5-6: Landmark Database Expansion (#4)
**Priority:** CRITICAL - 138 landmarks is insufficient

**Current:** 138 manually seeded landmarks (comprehensive but limited)
**Target:** 50,000+ landmarks globally

**Data Sources:**

1. **OpenStreetMap Overpass API** (Natural features)
   - Query: `node[natural=peak][ele>2000]` → Mountains >2000m
   - Query: `node[natural=volcano]` → All volcanoes
   - Query: `way[natural=lake][name][area>1000000]` → Major lakes
   - Query: `way[waterway=river][name]` → Major rivers
   - Expected: ~15,000 features

2. **Wikidata SPARQL** (Cultural landmarks)
   - UNESCO World Heritage Sites (1,157)
   - Cities >100K population (~4,000)
   - Historical monuments with coordinates
   - Museums, stadiums, towers, bridges
   - Expected: ~20,000 features

3. **Natural Earth Data** (Geographic regions)
   - Deserts, forests, plateaus
   - Ocean features, straits, bays
   - Country capitals
   - Expected: ~5,000 features

4. **Wikipedia GeoData** (Notable locations)
   - Articles with coordinates
   - Filter by view count (importance proxy)
   - Expected: ~10,000 features

**Implementation:**

**Python Scripts:**
```python
# scripts/ingest_landmarks.py
import overpy  # Overpass API
import requests  # Wikidata SPARQL
import geopandas  # Natural Earth
import psycopg2  # PostgreSQL insert

def fetch_osm_peaks():
    """Fetch peaks >2000m from OSM"""
    query = """
    [out:json];
    node[natural=peak][ele>2000];
    out body;
    """
    api = overpy.Overpass()
    result = api.query(query)
    return [
        {
            'name': node.tags.get('name'),
            'latitude': float(node.lat),
            'longitude': float(node.lon),
            'elevation': int(node.tags.get('ele', 0)),
            'type': 'MOUNTAIN',
            'source': 'osm',
            'importance_score': calculate_importance(node)
        }
        for node in result.nodes
        if node.tags.get('name')
    ]

def calculate_importance(feature):
    """Score 0-100 based on prominence, popularity"""
    score = 50  # Base

    # Elevation bonus
    if feature.tags.get('ele'):
        ele = int(feature.tags['ele'])
        if ele > 8000: score += 50  # 8000m club
        elif ele > 6000: score += 30
        elif ele > 4000: score += 15

    # Wikipedia link bonus
    if feature.tags.get('wikipedia'): score += 20

    # UNESCO site
    if 'heritage' in feature.tags: score += 30

    return min(score, 100)

def deduplicate(landmarks):
    """Remove duplicate landmarks from different sources"""
    # Group by name + nearby coordinates
    # Keep highest importance score
    pass

def insert_to_postgres(landmarks, db_conn):
    """Bulk insert to backend database"""
    cursor = db_conn.cursor()
    for lm in landmarks:
        cursor.execute("""
            INSERT INTO landmarks
            (id, name, latitude, longitude, elevation_m, type,
             importance_score, source, created_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW())
            ON CONFLICT (name, latitude, longitude) DO NOTHING
        """, (
            str(uuid.uuid4()),
            lm['name'],
            lm['latitude'],
            lm['longitude'],
            lm['elevation'],
            lm['type'],
            lm['importance_score'],
            lm['source']
        ))
    db_conn.commit()
```

**Tasks:**
- [ ] Write `ingest_osm_landmarks.py` (mountains, volcanoes, lakes, rivers)
- [ ] Write `ingest_wikidata_landmarks.py` (UNESCO, cities, monuments)
- [ ] Write `ingest_natural_earth.py` (geographic features)
- [ ] Write `ingest_wikipedia_geodata.py` (notable articles)
- [ ] Implement importance scoring algorithm
- [ ] Implement deduplication logic (fuzzy name match + distance)
- [ ] Create `scripts/validate_landmarks.py`:
  - Check coordinate bounds (-90/90, -180/180)
  - Verify required fields
  - Remove test/junk data
  - Fix encoding issues
- [ ] Batch insert to PostgreSQL
- [ ] Create spatial indices:
  ```sql
  CREATE INDEX idx_landmarks_location
  ON landmarks USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326));

  CREATE INDEX idx_landmarks_importance ON landmarks (importance_score DESC);
  CREATE INDEX idx_landmarks_type ON landmarks (type);
  ```
- [ ] Verify route queries return appropriate landmarks
- [ ] Update Android seed data remover (remove local seed logic)
- [ ] Add landmark sync API call on app startup

**Files to Create:**
```
scripts/
├── ingest_osm_landmarks.py
├── ingest_wikidata_landmarks.py
├── ingest_natural_earth.py
├── ingest_wikipedia_geodata.py
├── calculate_importance_scores.py
├── deduplicate_landmarks.py
├── validate_landmarks.py
└── requirements.txt
```

**Backend Migration:**
```sql
-- migrations/007_populate_landmarks.sql
COPY landmarks(name, latitude, longitude, elevation_m, type, importance_score, source)
FROM '/data/landmarks.csv'
DELIMITER ','
CSV HEADER;
```

**Success Criteria:**
- ✅ 50,000+ landmarks in database
- ✅ Global coverage (all continents)
- ✅ Top 1,000 landmarks have photos
- ✅ Spatial queries return in <100ms
- ✅ Importance scores ranked correctly
- ✅ No duplicate landmarks

---

### Phase 2: QUALITY & RELIABILITY (Make It Stable)
**Goal:** Production-ready quality
**Timeline:** 3 weeks
**Status:** Required before launch

#### Week 7: Trip Cloud Sync (#5)
**Tasks:**
- [ ] Implement backend endpoints (already in Phase 1)
- [ ] Create `TripSyncWorker` (WorkManager):
  ```kotlin
  class TripSyncWorker : CoroutineWorker() {
      override suspend fun doWork(): Result {
          // 1. Get local trips not synced
          val unsyncedTrips = tripDao.getUnsyncedTrips()

          // 2. Upload each trip
          unsyncedTrips.forEach { trip ->
              try {
                  val response = backendApi.uploadTrip(trip)
                  tripDao.markSynced(trip.id, response.cloudId)
              } catch (e: Exception) {
                  // Retry on next sync
              }
          }

          // 3. Download cloud trips
          val cloudTrips = backendApi.getTrips()
          cloudTrips.forEach { tripDao.insertOrUpdate(it) }

          return Result.success()
      }
  }
  ```
- [ ] Add `isSynced` and `cloudId` columns to `TripEntity`
- [ ] Trigger sync after flight ends
- [ ] Trigger sync on app launch (if online)
- [ ] Show sync status in trip history
- [ ] Implement conflict resolution (latest timestamp wins)
- [ ] Add retry logic (exponential backoff)

**Files to Create:**
```
android/app/src/main/java/com/skylens/
├── workers/TripSyncWorker.kt
├── data/remote/BackendApiClient.kt (if not already created)
└── data/sync/ConflictResolver.kt
```

**Database Migration:**
```kotlin
// Migration 4 → 5
database.execSQL("""
    ALTER TABLE trips ADD COLUMN is_synced INTEGER DEFAULT 0;
    ALTER TABLE trips ADD COLUMN cloud_id TEXT;
""")
```

**Success Criteria:**
- ✅ Trips upload after flight
- ✅ Trips sync across devices
- ✅ Works offline (queues for later)
- ✅ No data loss during conflicts

---

#### Week 7: AI Story Pre-Generation (#6)
**Tasks:**
- [ ] Write batch generation script:
  ```python
  # scripts/generate_ai_stories.py
  import anthropic
  import psycopg2

  client = anthropic.Anthropic(api_key=os.getenv("CLAUDE_API_KEY"))
  conn = psycopg2.connect(DB_URL)

  # Get top 1,000 landmarks without stories
  cursor = conn.cursor()
  cursor.execute("""
      SELECT id, name, type, country, elevation_m
      FROM landmarks
      WHERE ai_story IS NULL
      ORDER BY importance_score DESC
      LIMIT 1000
  """)

  for row in cursor.fetchall():
      landmark_id, name, type, country, elevation = row

      # Generate story
      story = client.messages.create(
          model="claude-sonnet-4-6",
          max_tokens=300,
          messages=[{
              "role": "user",
              "content": f"Write a 2-sentence engaging story about {name}, a {type} in {country}."
          }]
      ).content[0].text

      # Update database
      conn.cursor().execute(
          "UPDATE landmarks SET ai_story = %s WHERE id = %s",
          (story, landmark_id)
      )
      conn.commit()

      # Rate limiting
      time.sleep(1)  # 60/min max

      print(f"Generated story for {name}")
  ```
- [ ] Add `ai_story`, `general_fact`, `historical_fact` columns to backend
- [ ] Run script to pre-generate all stories
- [ ] Add database index on `ai_story IS NULL`
- [ ] Implement fallback to on-demand generation for rare landmarks
- [ ] Add story versioning (regenerate periodically)
- [ ] Android: Fetch stories from API instead of generating locally

**Cost Estimate:**
- 1,000 landmarks × $0.003/story = **$3 one-time cost**
- Saves: $0.003 per view × 1000 users = $3,000 saved

**Success Criteria:**
- ✅ Top 1,000 landmarks have stories
- ✅ Stories load instantly (no API latency)
- ✅ Fallback works for new landmarks
- ✅ API costs reduced by 99%

---

#### Week 8: Error Handling & Rate Limiting (#7, #8)
**Tasks:**
- [ ] Create error type hierarchy:
  ```kotlin
  sealed class ApiError(message: String) : Exception(message) {
      class NetworkError : ApiError("No internet connection")
      class RateLimitError(val resetAt: Long) : ApiError("API quota exceeded")
      class AuthError : ApiError("Invalid API key")
      class ServerError(val code: Int) : ApiError("Server error: $code")
  }
  ```
- [ ] Implement retry handler:
  ```kotlin
  class RetryHandler {
      suspend fun <T> withRetry(
          maxRetries: Int = 3,
          initialDelay: Long = 1000,
          maxDelay: Long = 10000,
          factor: Double = 2.0,
          block: suspend () -> T
      ): Result<T>
  }
  ```
- [ ] Add circuit breaker pattern
- [ ] Implement rate limiting:
  ```kotlin
  @Entity(tableName = "api_usage")
  data class ApiUsageEntity(
      @PrimaryKey val date: String, // "2026-03-27"
      val provider: String, // "gemini" or "claude"
      val callCount: Int,
      val estimatedCostUsd: Double
  )
  ```
- [ ] Daily usage tracker:
  - Free tier: 50 AI calls/day
  - Show in settings
  - Disable AI when exceeded
  - Reset at midnight
- [ ] Add usage analytics screen
- [ ] User-friendly error messages in UI
- [ ] Fallback content when AI unavailable

**Files to Create:**
```
android/app/src/main/java/com/skylens/
├── domain/error/ApiError.kt
├── ai/RetryHandler.kt
├── ai/UsageTracker.kt
├── data/local/entities/ApiUsageEntity.kt
├── data/local/dao/ApiUsageDao.kt
└── presentation/ui/components/ErrorBanner.kt
```

**Success Criteria:**
- ✅ Network errors show helpful messages
- ✅ Rate limits prevent API overuse
- ✅ Retry works for transient failures
- ✅ Users see usage stats
- ✅ App doesn't crash on API failures

---

#### Week 9: Unit Tests (#9)
**Tasks:**
- [ ] Add test dependencies:
  ```kotlin
  testImplementation("io.mockk:mockk:1.13.9")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
  testImplementation("app.cash.turbine:turbine:1.0.0")
  testImplementation("androidx.arch.core:core-testing:2.2.0")
  ```
- [ ] Write ViewModel tests (all 6):
  - `FlightMapViewModelTest.kt` - GPS, predictions, narration
  - `TripHistoryViewModelTest.kt` - Trip CRUD operations
  - `LandmarkDetailViewModelTest.kt` - Landmark loading
  - `FlightPlanningViewModelTest.kt` - Route validation
  - `SettingsViewModelTest.kt` - Preferences
  - `AskAIViewModelTest.kt` - Chat functionality
- [ ] Write Repository tests:
  - `LandmarkRepositoryTest.kt` - Spatial queries
  - `TripRepositoryTest.kt` - Trip storage
  - `OfflinePackRepositoryTest.kt` - Pack management
- [ ] Write utility tests:
  - `GeoCalculatorTest.kt` - Distance, great circle, bearing
  - `GpsFilterTest.kt` - Kalman filter accuracy
  - `UnitFormatterTest.kt` - Unit conversions
- [ ] Write API client tests (with mock server):
  - `ClaudeApiClientTest.kt`
  - `GeminiApiClientTest.kt`
  - `BackendApiClientTest.kt`
- [ ] Aim for 80%+ code coverage on business logic

**Test Examples:**
```kotlin
@Test
fun `calculateDistance returns correct haversine result`() {
    val calculator = GeoCalculator()
    val distance = calculator.haversineDistance(
        lat1 = 33.9416, lon1 = -118.4085, // LAX
        lat2 = 35.7647, lon2 = 140.3864   // NRT
    )
    assertEquals(8815.0, distance, delta = 10.0) // ~8815 km
}

@Test
fun `predictFutureLandmarks returns sorted by ETA`() = runTest {
    val viewModel = FlightMapViewModel(mockRepository, mockTracker)
    val currentPosition = FlightPosition(35.0, 140.0, 10000, 850f, 90f, 10f, 0L)

    viewModel.updatePosition(currentPosition)
    val predictions = viewModel.uiState.value.predictedLandmarks

    assertTrue(predictions.isNotEmpty())
    assertTrue(predictions[0].visibleInMinutes < predictions[1].visibleInMinutes)
}
```

**Success Criteria:**
- ✅ 50+ unit tests pass
- ✅ 80%+ code coverage
- ✅ CI runs tests on every PR
- ✅ Critical paths fully tested

---

### Phase 3: UX & SECURITY (Make It Good)
**Goal:** Great user experience + security
**Timeline:** 2 weeks
**Status:** Required before public launch

#### Week 10: UX Improvements
**Tasks:**
- [ ] Airport autocomplete search (#10)
- [ ] Photo gallery pager with zoom (#11)
- [ ] Network state banner (#12)
- [ ] Onboarding tutorial (Wave 11) (#19)
- [ ] Dark mode optimization (#20)
- [ ] Empty states for all screens (#21)
- [ ] Location permission rationale (#22)

#### Week 11: Security & Data
**Tasks:**
- [ ] API key validation & ProGuard obfuscation (#13)
- [ ] Database migrations strategy (#14)
- [ ] Input validation for all inputs (#15)
- [ ] Memory leak audit (#16)
- [ ] Structured logging with Timber (#17)
- [ ] Database indices (#18)
- [ ] Privacy controls & data deletion (#23)

---

### Phase 4: POLISH & DEPLOYMENT (Make It Public)
**Goal:** Play Store ready
**Timeline:** 2 weeks
**Status:** Final checklist

#### Week 12: Technical Debt
**Tasks:**
- [ ] Convert TODO comments to GitHub issues (#25)
- [ ] Extract magic numbers to constants (#26)
- [ ] Add KDoc to public APIs (#27)
- [ ] Move strings to strings.xml (#28)
- [ ] Battery optimization (#29)
- [ ] Image cache configuration (#30)
- [ ] Query optimization (#31)
- [ ] SQL injection audit (#24)

#### Week 13: Testing & Release Prep
**Tasks:**
- [ ] Integration tests (#32)
- [ ] UI tests with Compose (#33)
- [ ] Performance benchmarks (#34)
- [ ] CI/CD pipeline (#35)
- [ ] Release signing (#36)
- [ ] ProGuard rules testing (#37)

#### Week 14: Play Store Launch (Wave 12)
**Tasks:**
- [ ] Privacy policy webpage (#38)
  - Host on GitHub Pages: `https://skylens.github.io/privacy`
  - Include: data collected, third-party services, user rights
  - Link in app settings
- [ ] App screenshots (minimum 4):
  1. Flight planning screen with route
  2. Live map with landmarks + AI narration
  3. Landmark detail with photos + facts
  4. Trip history with replay
- [ ] Feature graphic (1024×500px)
- [ ] High-res icon (512×512px)
- [ ] Store listing copy:
  - **Short description** (80 chars): "AI-powered airplane window companion - discover landmarks during flights"
  - **Full description** (4000 chars): Features, benefits, how it works
- [ ] Data safety form (#39):
  - Location: Collected, stored locally + cloud, not shared
  - Email: Collected for auth, not shared
  - Photos: URLs only, from Wikimedia (public)
  - AI: Sent to Google/Anthropic per provider
- [ ] Content rating questionnaire
- [ ] Internal testing track (20+ testers)
- [ ] Beta testing for 1-2 weeks
- [ ] Fix bugs from beta feedback
- [ ] Submit to Play Store for review
- [ ] Monitor crash reports (Crashlytics/Sentry)

**Play Store Requirements Checklist:**
- [ ] App bundle (AAB) generated
- [ ] Release signed with upload key
- [ ] Privacy policy URL provided
- [ ] Screenshots uploaded (4+ required)
- [ ] Feature graphic uploaded
- [ ] App description complete
- [ ] Data safety form complete
- [ ] Content rating complete
- [ ] Countries/regions selected
- [ ] Pricing set (free/paid)
- [ ] Internal testing passed
- [ ] Production release submitted

---

## 🎯 Recommended Development Order

### Option A: Fastest MVP (8 weeks)
**Goal:** Get to market quickly with core features

1. ✅ Backend API (2 weeks)
2. ✅ Offline maps (1 week)
3. ✅ Real pack download (1 week)
4. ✅ Landmark expansion (2 weeks)
5. ⚠️ Error handling + rate limiting (3 days)
6. ⚠️ Essential tests (critical paths only) (4 days)
7. ✅ Play Store prep (1 week)

**Trade-offs:**
- Skip comprehensive testing (risky)
- Skip some UX polish
- Launch with known technical debt
- Fix issues post-launch

**Result:** MVP in 8 weeks, iterate from user feedback

---

### Option B: Quality First (13 weeks)
**Goal:** Launch polished, production-ready app

1. ✅ Backend API (2 weeks)
2. ✅ Offline maps (1 week)
3. ✅ Real pack download (1 week)
4. ✅ Landmark expansion (2 weeks)
5. ✅ Trip sync (1 week)
6. ✅ AI pre-generation (3 days)
7. ✅ Error handling + rate limiting (3 days)
8. ✅ Unit tests (1 week)
9. ✅ UX improvements (1 week)
10. ✅ Security audit (1 week)
11. ✅ Technical debt cleanup (1 week)
12. ✅ Testing + release prep (1 week)

**Trade-offs:**
- Longer development time
- Higher quality at launch
- Fewer post-launch bugs
- Better user experience

**Result:** Polished app in 13 weeks, strong foundation

---

### Option C: Hybrid Approach (10 weeks) - RECOMMENDED
**Goal:** Balance speed with quality

**Phase 1 - MVP (6 weeks):**
1. Backend + Offline Maps + Pack Download + Landmarks (6 weeks)

**Phase 2 - Quality (2 weeks):**
2. Trip sync + AI pre-gen (1 week)
3. Error handling + rate limiting + critical tests (1 week)

**Phase 3 - Launch (2 weeks):**
4. Essential UX (airport search, empty states) (1 week)
5. Play Store prep + beta testing (1 week)

**Trade-offs:**
- Core features solid
- Basic quality checks
- Some technical debt remains
- Iterate after launch

**Result:** Production-ready in 10 weeks, iterate from there

---

## 📈 Effort Summary by Category

| Priority | Issues | Total Effort | Risk if Skipped |
|----------|--------|--------------|-----------------|
| **BLOCKING** | 4 | 6 weeks | App unusable for flights |
| **HIGH** | 5 | 3 weeks | Poor quality, high costs |
| **MEDIUM** | 15 | 4 weeks | Security/UX issues |
| **LOW** | 18 | 3 weeks | Technical debt |
| **TOTAL** | 42 | **16 weeks** | - |

---

## 🚦 Risk Assessment

### Critical Risks
- 🔴 **Backend infrastructure** - Single point of failure, complex deployment
- 🔴 **Offline maps** - Large download size, storage management complex
- 🔴 **API costs** - Could spiral out of control without rate limiting

### Medium Risks
- 🟡 **Play Store approval** - Privacy policy, permissions rationale required
- 🟡 **Performance** - Memory leaks on long flights, battery drain
- 🟡 **Data loss** - No cloud sync means data lost on uninstall

### Low Risks
- 🟢 **Technical debt** - Won't block launch but will slow future development
- 🟢 **Testing gaps** - Manual testing can catch critical issues

---

## 🛠 Infrastructure Requirements

### Backend Hosting
**Recommended:** Fly.io
**Specs:**
- 1 shared CPU instance ($5/month)
- 256MB RAM ($5/month)
- 1GB storage ($0.15/GB/month)
- PostgreSQL (1GB) ($7/month)
**Total:** ~$20-30/month

**Alternatives:**
- Railway.app - Similar pricing, easier setup
- Render.com - Free tier available
- Digital Ocean - $12/month droplet

### Storage (Photos + Packs)
**Recommended:** Cloudflare R2
**Pricing:** $0.015/GB storage + $0.36/million requests
**Expected:** 10GB = $0.15/month

### API Costs
**Claude API:**
- Pre-generation: $3 one-time (1,000 stories)
- On-demand: ~$5-50/month depending on users

**Gemini API:**
- Free tier: 1,500 requests/day
- Paid: $0.002/request

---

## 📋 Pre-Launch Checklist

### Functional Requirements
- [ ] App works offline (maps + landmarks)
- [ ] GPS tracking accurate
- [ ] AI narration engaging
- [ ] Photos display correctly
- [ ] Trips save and replay
- [ ] Notifications timely

### Performance Requirements
- [ ] App launch <3 seconds
- [ ] Landmark query <100ms
- [ ] Map renders at 60 FPS
- [ ] Battery usage <5%/hour during flight
- [ ] Storage <200MB per offline pack

### Security Requirements
- [ ] API keys not in version control
- [ ] ProGuard obfuscates sensitive code
- [ ] Location permissions justified
- [ ] Privacy policy published
- [ ] GDPR-compliant data deletion

### Quality Requirements
- [ ] Zero crashes in beta testing
- [ ] 80%+ test coverage on business logic
- [ ] No memory leaks in 12-hour test
- [ ] Passes Play Store review
- [ ] Works on Android 10-15

---

## 🔮 Post-Launch Roadmap (v2.0+)

### Future Enhancements (Not in MVP)
1. **AR Camera Overlay** - Point camera at window, see AR labels
2. **Social Features** - Share trips, follow travelers, leaderboards
3. **Multi-Language** - Japanese, Spanish, French, German, Mandarin
4. **Offline Maps Worldwide** - Pre-download entire regions
5. **Flight History Analytics** - Distance traveled, countries visited, stats
6. **Gamification** - Badges, achievements, landmark collections
7. **Apple Watch App** - Glanceable info on wrist
8. **Widget Support** - Home screen quick stats
9. **Tablet Optimization** - Split-screen, larger maps
10. **CarPlay/Android Auto** - Road trip mode

---

## 💡 Key Decisions Needed

### Before Starting Implementation:

1. **Backend Hosting Budget?**
   - $20-30/month for Fly.io OK?
   - Alternative: Free tier (Railway/Render) with limitations?

2. **Launch Timeline?**
   - Fast MVP (8 weeks)?
   - Quality first (13 weeks)?
   - Hybrid (10 weeks)?

3. **AI API Budget?**
   - $3 one-time for pre-generation?
   - $5-50/month for on-demand?
   - Or disable AI features?

4. **Landmark Coverage Priority?**
   - Global coverage (50K landmarks)?
   - Or focus on popular routes (5K)?

5. **Testing Strategy?**
   - Comprehensive tests (1 week)?
   - Or critical paths only (3 days)?

6. **Monetization Model?**
   - Free with ads?
   - Freemium (free + paid tiers)?
   - Paid upfront ($2.99)?
   - Free forever?

7. **Open Source?**
   - Keep private?
   - Or open source on GitHub?

---

## 📞 Next Steps

### Immediate Actions (Tonight - Done):
- ✅ Code review documented
- ✅ Implementation plan created
- ✅ All changes committed
- ✅ Pushed to GitHub

### Tomorrow's Review Topics:
1. Choose development approach (Option A/B/C)
2. Decide on backend hosting (Fly.io vs alternatives)
3. Set AI API budget and usage limits
4. Prioritize issues (which to skip for v1.0?)
5. Start Phase 1: Backend infrastructure

### Questions to Answer Tomorrow:
- Which issues are MVP-blockers vs nice-to-have?
- What's the launch deadline?
- What's the monthly infrastructure budget?
- Should we implement backend first or parallel with Android work?
- Do we need beta testers identified now?

---

**Plan Status:** 📝 DRAFT - Ready for review
**Next Action:** Review tomorrow and start Phase 1
**Estimated Time to Production:** 10-13 weeks
**Confidence Level:** 🟢 HIGH - Clear path forward
