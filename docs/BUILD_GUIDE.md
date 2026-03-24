# 🔨 SkyLens — Complete Build & Setup Guide

## 📋 Prerequisites

### Required Software
- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** or higher
- **Android SDK 34** (Android 14)
- **Physical Android device** (emulator works but GPS simulation limited)
- **Git** for version control

### Required Accounts
1. **Google Cloud Console** — for OAuth credentials
2. **Supabase** — for database and auth
3. **Anthropic** — for Claude API key

---

## 🚀 Step-by-Step Setup

### Step 1: Clone and Setup Project

```bash
cd /Users/I808883/app/claude
cd android
```

### Step 2: Configure Environment

Create `local.properties` file:
```properties
# Supabase Configuration (from https://supabase.com dashboard)
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGc...your-anon-key

# Google OAuth (from Google Cloud Console)
GOOGLE_WEB_CLIENT_ID=123456789-abc123.apps.googleusercontent.com

# Claude API (from https://console.anthropic.com)
CLAUDE_API_KEY=sk-ant-api03-your-key-here
```

### Step 3: Setup Supabase

1. **Create Supabase Project:**
   - Go to https://supabase.com
   - Click "New Project"
   - Name: "skylens"
   - Database Password: (save securely)

2. **Run Database Migrations:**

```sql
-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create tables
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE airports (
    id SERIAL PRIMARY KEY,
    iata_code VARCHAR(3) UNIQUE NOT NULL,
    icao_code VARCHAR(4),
    name TEXT NOT NULL,
    city TEXT,
    country TEXT,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    elevation_m INTEGER,
    timezone TEXT
);
CREATE INDEX idx_airports_location ON airports USING GIST(location);

CREATE TABLE landmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    elevation_m INTEGER,
    importance_score FLOAT NOT NULL DEFAULT 0,
    wiki_id TEXT,
    country TEXT,
    ai_story TEXT,
    photo_urls TEXT[],
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_landmarks_location ON landmarks USING GIST(location);
CREATE INDEX idx_landmarks_importance ON landmarks (importance_score DESC);

CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    departure_airport VARCHAR(3) NOT NULL,
    arrival_airport VARCHAR(3) NOT NULL,
    route_corridor GEOGRAPHY(LineString, 4326),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_trips_user ON trips (user_id);

CREATE TABLE trip_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id UUID REFERENCES trips(id) ON DELETE CASCADE,
    landmark_id UUID REFERENCES landmarks(id),
    event_time TIMESTAMPTZ NOT NULL,
    distance_km FLOAT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_trip_events_trip ON trip_events (trip_id);
```

3. **Insert Sample Data:**

```bash
cd scripts
python3 generate_sample_data.py > sample_data.sql
# Copy SQL output to Supabase SQL Editor and run
```

4. **Enable Google Auth in Supabase:**
   - Go to Authentication → Providers → Google
   - Enable Google provider
   - Add your Google OAuth Client ID
   - Save Supabase callback URL for next step

### Step 4: Setup Google Cloud Console

1. **Create Google Cloud Project:**
   - Go to https://console.cloud.google.com
   - Create new project: "skylens"

2. **Configure OAuth Consent Screen:**
   - APIs & Services → OAuth consent screen
   - Select "External"
   - App name: "SkyLens"
   - User support email: your-email@gmail.com
   - Add scopes: `openid`, `profile`, `email`
   - Save

3. **Create OAuth Credentials:**
   - APIs & Services → Credentials → Create Credentials → OAuth 2.0 Client ID
   - **Type:** Android
   - **Package name:** `com.skylens.app`
   - **SHA-1:** Get from your debug keystore:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey
     # Password: android
     ```
   - Copy the Client ID

4. **Create Web Client ID:**
   - Create another OAuth 2.0 Client ID
   - **Type:** Web application
   - **Authorized redirect URIs:** Add Supabase callback URL
   - Copy the Web Client ID (use this in local.properties)

### Step 5: Update Constants with Keys

Edit `Constants.kt` or configure BuildConfig to use environment variables:

```kotlin
// Option A: Direct (for testing only)
const val SUPABASE_URL = "https://abcdefgh.supabase.co"

// Option B: From local.properties (recommended)
buildConfigField("String", "SUPABASE_URL", "\"${project.properties["SUPABASE_URL"]}\"")
```

### Step 6: Sync and Build

```bash
# Open in Android Studio
open -a "Android Studio" /Users/I808883/app/claude/android

# Or command line:
./gradlew clean
./gradlew build
```

### Step 7: Run on Device

```bash
# Install debug APK
./gradlew installDebug

# Launch app
adb shell am start -n com.skylens.app/.MainActivity

# View logs
adb logcat | grep SkyLens
```

---

## 🐛 Troubleshooting

### Build Errors

**Error: "Unresolved reference: hiltViewModel"**
```bash
# Solution: Sync Gradle
./gradlew --refresh-dependencies
```

**Error: "Room schema export directory not set"**
```kotlin
// Add to build.gradle.kts:
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

**Error: "MapLibre not found"**
```kotlin
// Verify in build.gradle.kts:
implementation("org.maplibre.gl:android-sdk:11.0.0")
```

### Runtime Errors

**Error: "Location permission denied"**
- Check AndroidManifest.xml has location permissions
- Test on physical device (not emulator)
- Grant permissions in Settings → Apps → SkyLens

**Error: "Supabase connection failed"**
- Verify SUPABASE_URL and SUPABASE_ANON_KEY in local.properties
- Check internet connection
- Verify Supabase project is not paused

**Error: "Claude API 401 Unauthorized"**
- Verify CLAUDE_API_KEY is correct
- Check API key has not expired
- Verify account has available credits

---

## 📦 Building Release APK

### Step 1: Create Keystore

```bash
keytool -genkey -v -keystore skylens-release.keystore \
  -alias skylens -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Configure Signing

Add to `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../skylens-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "skylens"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

### Step 3: Build Release

```bash
export KEYSTORE_PASSWORD=your-password
export KEY_PASSWORD=your-key-password
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

---

## 🧪 Testing Strategies

### Unit Tests

```bash
./gradlew test
```

### GPS Simulation

Use Android Studio's location mock:
1. Run app on emulator
2. Extended Controls (⋯) → Location
3. Load GPX file or set coordinates manually

### AI Feature Testing

Test Claude API locally:
```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $CLAUDE_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "content-type: application/json" \
  -d '{"model": "claude-haiku-4.5-20251001", "max_tokens": 1024, "messages": [{"role": "user", "content": "Tell me about Mount Fuji"}]}'
```

---

## 📊 Performance Testing

### Battery Usage

```bash
# Start tracking
adb shell dumpsys batterystats --reset
adb shell dumpsys batterystats --enable full-wake-history

# Use app for 1 hour

# Check results
adb shell dumpsys batterystats | grep com.skylens.app
```

Target: <5% per hour

### Memory Profiling

1. Android Studio → Profiler → Memory
2. Start flight simulation
3. Monitor heap usage (target: <300MB)

### FPS Measurement

1. Android Studio → Profiler → CPU
2. Enable "Display frame rendering data"
3. Target: 60 FPS consistent

---

## 🚀 Deployment Checklist

### Pre-Launch
- [ ] Privacy policy published at public URL
- [ ] Google OAuth configured
- [ ] Supabase database populated with sample data
- [ ] Claude API key configured and tested
- [ ] App tested on 3+ devices
- [ ] Battery usage verified (<5% per hour)
- [ ] No crashes in 1-hour test session

### Play Store
- [ ] App signed with release keystore
- [ ] App bundle (.aab) generated
- [ ] Screenshots captured (4+ required)
- [ ] Feature graphic created (1024×500)
- [ ] Short description written (<80 chars)
- [ ] Full description written (<4000 chars)
- [ ] Data safety form completed
- [ ] Content rating completed
- [ ] Internal testing track created

### Post-Launch Monitoring
- [ ] Firebase Crashlytics enabled (optional)
- [ ] Monitor Claude API usage and costs
- [ ] Monitor Supabase database size
- [ ] Track user feedback
- [ ] Monitor battery usage reports

---

## 💡 Development Tips

### Hot Reload

Android Studio supports Compose hot reload:
- Make UI changes
- Press `Ctrl+Shift+F10` (Cmd+Shift+R on Mac)
- Changes appear instantly

### Debug Logging

```kotlin
import android.util.Log

Log.d("SkyLens", "Current position: $latitude, $longitude")
Log.i("SkyLens", "Landmarks found: ${landmarks.size}")
Log.e("SkyLens", "Error: ${e.message}")
```

### Database Inspection

```bash
# Pull database from device
adb pull /data/data/com.skylens.app/databases/skylens.db

# Open with DB Browser for SQLite
open skylens.db
```

---

## 📚 Additional Resources

- [Jetpack Compose Documentation](https://developer.android.com/compose)
- [MapLibre Android SDK](https://maplibre.org/maplibre-native/android/)
- [Supabase Kotlin Docs](https://supabase.com/docs/reference/kotlin/introduction)
- [Claude API Documentation](https://docs.anthropic.com/claude/reference)
- [Android Location Guide](https://developer.android.com/develop/sensors-and-location/location)

---

## 🎯 Quick Start (TL;DR)

```bash
# 1. Configure environment
cp local.properties.example local.properties
# Edit local.properties with your keys

# 2. Build
./gradlew build

# 3. Install
./gradlew installDebug

# 4. Run
adb shell am start -n com.skylens.app/.MainActivity
```

---

**Need help? Check MILESTONE_STATUS.md for current progress and expected outputs.**
