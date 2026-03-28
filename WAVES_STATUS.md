Based on the original plan and current implementation, here are ALL MISSING FEATURES for next development wave:

🚨 MISSING FEATURES - Next Development Wave

## ✅ WAVE 1: Google Sign-In
**Status**: ✅ COMPLETE (March 27, 2026)

**What was done:**
- ✅ Google Cloud Console project created
- ✅ OAuth 2.0 Client ID configured (Android + Web)
- ✅ SHA-1 fingerprint added
- ✅ Supabase Auth integration working
- ✅ Web Client ID: 289309407597-rpd6k933a6rulukjahgoqf7eb0mmv9vv.apps.googleusercontent.com
- ✅ Nonce issue fixed (removed nonce generation)
- ✅ Test users added to OAuth consent screen
- ✅ "Skip for now" offline mode working

**Result**: Users can now sign in with Google or continue offline

---

## ✅ WAVE 2: Real GPS Tracking
**Status**: ✅ COMPLETE (March 27, 2026)

**What was done:**
- ✅ GPS mode toggle added (Real GPS / Mock Flight)
- ✅ `startFlightTracking()` dispatcher implemented
- ✅ `startRealGpsFlight()` method wired up
- ✅ FusedLocationProviderClient already integrated
- ✅ Kalman filter for GPS smoothing already implemented
- ✅ GPS accuracy indicator in status bar
- ✅ Toggle positioned at top right

**Result**: Users can now track real flights using device GPS or use mock simulation for testing

---

## WAVE 3: Offline Pack Download (Backend + Download)
**Status**: ⚠️ FAKE (UI exists but just sets a flag, no actual download)

Missing:

Backend (Rust):

- Axum web server
- PostgreSQL + PostGIS database
- **Backend API for landmark sync** (deferred from seed data approach)
- Landmark database population (164 base landmarks seeded in Android, need 50K+ from external sources)
- Airport database (7,000 entries)
- Great circle route calculation
- Pack generation (landmarks + map tiles → ZIP)
- Cloudflare R2 storage integration

Android:

- Real HTTP download with progress tracking
- ZIP extraction to Room database
- Map tile storage in local database
- Storage space checking

**Current limitation**: Fake download - just waits 3 seconds then marks as "done"

**Note**: Android app now has 138 comprehensive landmarks seeded locally (mountains, cities, historical sites, volcanoes, rivers, lakes, national parks, islands, waterfalls, deserts, glaciers, ocean features, canyons/caves, forests). Backend sync API for incremental updates deferred to future wave.

---

## WAVE 4: AI Integration (Claude API)
**Status**: ✅ COMPLETE (March 27, 2026)

**What was done:**
- ✅ Dual AI provider support (Gemini + Claude)
- ✅ AiStoryManager with provider selection
- ✅ AI flight narration with fallback messages
- ✅ Rate limiting and usage tracking
- ✅ Ask AI chat interface
- ✅ Unified AI provider for all features
- ✅ Graceful fallbacks when API unavailable

**Features working:**
- Flight narrator updates every 60s with nearby landmarks
- Ask AI chat for landmark questions
- Provider selection in settings (Gemini/Claude)
- Cost tracking and usage stats

**Limitations:**
- Gemini free tier has low quota (1500/day)
- No AI-generated landmark stories yet (need photos first)

---

## WAVE 5: Predictions & Notifications
**Status**: ✅ COMPLETE (March 27, 2026)

**What was done:**
- ✅ Velocity-based prediction algorithm
- ✅ Future position calculation (2, 5, 10 min ahead)
- ✅ Landmark visibility predictions
- ✅ Android notification system
- ✅ POST_NOTIFICATIONS permission
- ✅ Notification channels configured
- ✅ "Upcoming" card in flight UI
- ✅ AI preview for predicted landmarks

**Features working:**
- Predicts landmarks 2-10 minutes ahead
- Shows "Visible in X min" card
- Sends notification when landmark 5 min away
- Works with both mock and real GPS

**Result**: Users get heads-up alerts before landmarks appear

---

## WAVE 6: Landmark Photos
**Status**: ✅ COMPLETE (March 27, 2026)

**What was done:**
- ✅ Wikimedia Commons API integration
- ✅ Photo URL fetching from Wikipedia
- ✅ Photo caching in database
- ✅ Background photo fetch service
- ✅ AsyncImage with Coil library
- ✅ Photo gallery in detail screen
- ✅ Photo display in bottom sheet

**Features working:**
- Auto-fetches photos when landmark opened
- Background service fetches 50 photos (10/min rate limit)
- Photos cached permanently after first fetch
- Smooth image loading with crossfade

**Result**: Landmark photos now display from Wikimedia Commons

---

## WAVE 7: Map Tiles & Offline Maps
Status: ⚠️ USING ONLINE TILES (not offline)

Missing:

OpenStreetMap tile download
Local tile storage in Room database
MapLibre offline tile source configuration
Tile coverage calculator (zoom levels 4-9)
Tile expiration and cleanup
**Current limitation**: Map requires internet connection

---

## WAVE 8: Backend Deployment
Status: ❌ NO BACKEND EXISTS

Missing:

Rust backend implementation (0% done)
PostgreSQL database setup
PostGIS spatial queries
Supabase Auth integration
API endpoints implementation
Fly.io deployment
Database migrations
**Current limitation**: App is 100% local, no cloud sync

---

## WAVE 9: Trip Cloud Sync
Status: ❌ LOCAL ONLY

Missing:

Trip upload to backend
Trip event synchronization
Trip history from cloud
Conflict resolution (local vs cloud)
Offline-first sync strategy
**Current limitation**: Trips only stored locally

---

## WAVE 10: Settings & About
**Status**: ✅ COMPLETE (March 27, 2026)

**What was done:**
- ✅ Units preference (metric/imperial) with persistence
- ✅ Trip export as GeoJSON format
- ✅ Share button in trip history
- ✅ FileProvider for secure file sharing
- ✅ Unit conversion (altitude, speed, distance, elevation)
- ✅ Settings screen with preferences
- ✅ About screen with app info
- ✅ App version display

**Features working:**
- Toggle between metric/imperial units in settings
- Units persist across app restarts
- Export trips as GeoJSON files via share sheet
- Altitude shown in ft or m based on preference
- Speed shown in km/h or mph
- Settings organized by category

**Missing** (deferred):
- Account deletion
- Notification settings toggle
- Privacy policy webpage
- Licenses screen content

**Result**: Core settings functionality complete with units and export

---

## WAVE 11: Onboarding
Status: ⚠️ SCREEN EXISTS BUT EMPTY

Missing:

Feature walkthrough (3-4 screens)
Screenshots/illustrations
"Get Started" flow
- First-time user education

---

## WAVE 12: Play Store Readiness
Status: ❌ NOT READY

Missing:

Privacy policy webpage
App screenshots (4+ required)
Feature graphic (1024×500)
Store listing copy
Content rating questionnaire
Data safety form completion
Release build signing
ProGuard rules optimization
- App bundle (AAB) generation

---

## ## 📊 Implementation Status Summary

| Feature | Status | Completion |
|---------|--------|------------|
| Auth Screen | ✅ Done | 100% (OAuth configured) |
| Flight Planning | ✅ Done | 100% |
| Pack Download UI | ⚠️ Fake | 20% (UI only) |
| Mock Flight | ✅ Done | 100% |
| Real GPS | ✅ Done | 100% (toggle implemented) |
| Map Display | ✅ Done | 80% (online only) |
| Landmark Display | ✅ Done | 100% |
| Landmark Detail | ✅ Done | 100% (photos + AI) |
| Landmark Navigation | ✅ Done | 100% |
| Trip History | ✅ Done | 100% |
| Trip Replay | ✅ Done | 100% |
| AI Stories | ✅ Done | 100% (Gemini/Claude) |
| Predictions | ✅ Done | 100% (with notifications) |
| Notifications | ✅ Done | 100% |
| Backend | ❌ Not Done | 0% |
| Offline Maps | ❌ Not Done | 0% |

**Overall Progress**: ~70% complete (up from 45%)