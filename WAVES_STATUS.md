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

Axum web server
PostgreSQL + PostGIS database
Landmark database (50,000 entries)
Airport database (7,000 entries)
Great circle route calculation
Pack generation (landmarks + map tiles → ZIP)
Cloudflare R2 storage integration
Android:

Real HTTP download with progress tracking
ZIP extraction to Room database
Map tile storage in local database
Storage space checking
**Current limitation**: Fake download - just waits 3 seconds then marks as "done"

---

## WAVE 4: AI Integration (Claude API)
**Status**: ❌ NOT IMPLEMENTED (placeholder client exists)

**Missing:**

Claude API client implementation
AI story generation for landmarks
AI flight narration (real-time commentary)
Prediction AI context ("Mount Fuji visible in 4 minutes")
Rate limiting and cost monitoring
Cache-first strategy (pre-generated stories)
**Current limitation**: No AI stories show in landmark details

---

## WAVE 5: Predictions & Notifications
Status: ❌ NOT IMPLEMENTED

Missing:

Upcoming landmark prediction algorithm
Velocity vector calculations
Future position extrapolation
Android notifications (heads-up style)
"Visible in X minutes" alerts
Notification channels setup
**Current limitation**: No upcoming landmark alerts

---

## WAVE 6: Landmark Photos
Status: ❌ NO PHOTOS (database has empty photo URLs)

Missing:

Wikimedia Commons API integration
Photo URL fetching for each landmark
Photo download and caching
Image optimization (WebP conversion)
Photo gallery in detail screen
**Current limitation**: Landmark detail screen shows no photos

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
Status: ⚠️ BASIC SCREENS EXIST

Missing:

Account management (delete account)
Export trip history (GeoJSON)
Units preference (metric/imperial)
Notification settings
Privacy policy viewer
Licenses screen
- App version display

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
| Landmark Detail | ✅ Done | 90% (no photos/AI) |
| Landmark Navigation | ✅ Done | 100% |
| Trip History | ✅ Done | 100% |
| Trip Replay | ✅ Done | 100% |
| AI Stories | ❌ Not Done | 0% |
| Predictions | ❌ Not Done | 0% |
| Notifications | ❌ Not Done | 0% |
| Backend | ❌ Not Done | 0% |
| Offline Maps | ❌ Not Done | 0% |

**Overall Progress**: ~45% complete (up from 35%)