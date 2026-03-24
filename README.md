# 🛫 SkyLens — AI-Powered Airplane Window Explorer

**Discover what you're seeing from your airplane window in real-time, powered by Claude AI.**

---

## 🎯 Project Overview

SkyLens is an Android app that uses GPS, geospatial calculations, and Claude AI to identify and narrate landmarks visible from airplane windows during flights.

### Key Features
- 🌍 Real-time GPS tracking during flights
- 🗺 Live map with your aircraft position
- 🏔 Automatic landmark identification
- 🤖 AI-generated stories and commentary (Claude API)
- 💬 Conversational AI ("What's that mountain?")
- 📸 AI photo analysis and captions
- 📊 AI trip summaries after landing
- 🔮 Predictive alerts ("Mount Fuji in 4 minutes")

---

## 📅 Development Milestones

### ✅ Milestone 1: Project Scaffold + Navigation (Days 1-3)
**Status:** 🚧 In Progress

**Deliverable:** App launches with working navigation

**What's Included:**
- ✅ Android project structure created
- ✅ Gradle build files configured
- ✅ Hilt dependency injection setup
- ⏳ Navigation graph (Splash → Auth → Home → Map → History)
- ⏳ Empty screen placeholders
- ⏳ Material 3 theme

**Demo:**
- Install APK on device
- App opens showing splash screen
- Navigate between empty screens via bottom nav

---

### Milestone 2: Authentication Screens + Google Sign-In (Days 4-6)
**Status:** ⏳ Not Started

**Deliverable:** Working Google authentication

**What's Included:**
- Google Sign-In button with Credential Manager API
- Supabase Auth integration
- Token storage with DataStore
- "Skip" option for anonymous usage
- Welcome screen showing user profile

**Demo:**
1. Open app → Auth screen
2. Tap "Continue with Google"
3. Select Google account
4. See personalized home screen: "Welcome, John!"
5. Close app, reopen → Still signed in

---

### Milestone 3: Core Data Models + API Integration (Days 7-11)
**Status:** ⏳ Not Started

**Deliverable:** Supabase connection + landmark data access

**What's Included:**
- Room database with landmark/airport entities
- Supabase client configuration
- Airport search functionality
- Landmark repository (stream from Supabase)
- Claude API client setup

**Demo:**
1. Search "Tokyo" → See NRT airport
2. Backend query returns landmarks
3. Claude API test: Generate story for Mount Fuji

---

### Milestone 4: GPS Tracking + Map Display (Days 12-16)
**Status:** ⏳ Not Started

**Deliverable:** Real-time position on interactive map

**What's Included:**
- Location permissions flow
- FusedLocationProvider GPS tracking
- MapLibre integration with Compose
- Live position marker on map
- Flight status bar (altitude, speed)

**Demo:**
1. Grant location permissions
2. See your position on map (blue dot)
3. Walk around → Map follows you
4. Tap "Simulate Flight" → Mock GPS playback

---

### Milestone 5: AI Landmark Narrator (Days 17-21) ⭐ **AI-FIRST**
**Status:** ⏳ Not Started

**Deliverable:** AI-powered landmark discovery and narration

**What's Included:**
- Landmark markers appear on map
- Tap landmark → AI-generated story (Claude)
- Real-time AI flight commentary
- AI photo captions
- "Upcoming landmark" predictions with AI context

**Demo:**
1. Simulate flight LAX → NRT
2. As you pass landmarks, Claude narrates:
   > "You're flying over the Mojave Desert, a vast arid region that played a key role in aviation testing..."
3. Tap Mount Fuji marker → Full AI story
4. Claude predicts: "Mount Fuji will be visible in 3 minutes on your right"

---

### Milestone 6: AI Q&A + Trip Summaries (Days 22-24)
**Status:** ⏳ Not Started

**Deliverable:** Conversational AI interface

**What's Included:**
- Chat interface in flight view
- Ask Claude anything about visible landmarks
- AI trip summary after landing
- Trip history with AI highlights

**Demo:**
1. During flight, tap chat icon
2. Ask: "What's the tallest mountain I can see?"
3. Claude responds with intelligent answer
4. After landing, see AI trip summary:
   > "Your 11-hour journey crossed 8 time zones, passing over 47 major landmarks including the Rocky Mountains, Great Plains, and Pacific Ocean..."

---

### Milestone 7: Polish + Play Store (Days 25-28)
**Status:** ⏳ Not Started

**Deliverable:** Play Store ready release

**What's Included:**
- App icon and branding
- Smooth animations
- Error handling and retry logic
- Privacy policy webpage
- Settings screen
- Play Store screenshots
- APK signed and ready

**Demo:**
- Polished UI with animations
- Graceful offline degradation
- Ready for internal testing track

---

## 🏗 Simplified Architecture (for 10 users)

```
┌─────────────────┐
│  Android App    │
│  (Kotlin)       │
│  - Compose UI   │
│  - Room DB      │
│  - GPS Tracker  │
└────────┬────────┘
         │ HTTPS
         ↓
┌─────────────────┐
│   Supabase      │
│  - PostgreSQL   │
│  - PostGIS      │
│  - Auth (Google)│
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Claude API     │
│  (Haiku 4.5)    │
│  - Stories      │
│  - Commentary   │
│  - Q&A          │
└─────────────────┘
```

**No custom backend needed!** Supabase handles database, auth, and API.

---

## 🚀 Quick Start

### Prerequisites
- **Java Development Kit 17+** ([Installation Guide](docs/JAVA_SETUP.md))
- Android Studio Hedgehog or newer
- Android SDK 34
- Physical Android device (GPS required for testing)

### Setup

1. **Verify Java installation:**
```bash
java -version
# Should show: openjdk version "17.0.x" or higher
```

2. **Clone repository:**
```bash
git clone https://github.com/yourusername/skylens.git
cd skylens/android
```

3. **Configure environment:**
Copy and edit `local.properties.example`:
```bash
cp local.properties.example local.properties
# Edit local.properties with your actual API keys
```

See [API Integration Guide](docs/API_INTEGRATION.md) for detailed setup instructions.

4. **Sync and build:**
```bash
./gradlew build
```

5. **Run on device:**
```bash
./gradlew installDebug
```

---

## 🤖 AI Features Breakdown

### 1. **AI Landmark Storyteller**
- Claude generates contextual stories for every landmark
- Cached responses for popular landmarks
- Real-time generation for rare sights

### 2. **AI Flight Narrator**
- Continuous AI commentary as you fly
- Contextual information about regions
- Historical and cultural insights

### 3. **AI Visual Analysis**
- Claude Vision analyzes landmark photos
- Generates descriptive captions
- Identifies key features in images

### 4. **Conversational AI Assistant**
- Ask questions during flight
- "What mountain range is that?"
- "Tell me about this region's history"

### 5. **AI Trip Summarizer**
- After landing, Claude generates flight summary
- Highlights most interesting landmarks
- Creates shareable trip story

### 6. **AI Recommendations**
- Based on your interests
- Suggests landmarks to watch for
- Personalizes experience over time

---

## 📱 Tech Stack

**Android:**
- Kotlin 1.9
- Jetpack Compose
- Material 3
- Hilt (DI)
- Room (offline storage)
- MapLibre (maps)
- Supabase Kotlin SDK
- Anthropic SDK (Claude API)

**Backend:**
- Supabase (PostgreSQL + PostGIS + Auth)
- Claude API (Haiku 4.5)

---

## 💰 Cost Estimate (10 users)

- **Supabase:** Free tier (sufficient for 10 users)
- **Claude API:** ~$2/month (generous caching, mostly Haiku)
- **Map Tiles:** Free (online OSM tiles)

**Total: ~$2/month**

---

## 🎯 Success Metrics

- ✅ User can track position during flight
- ✅ AI generates relevant landmark stories
- ✅ Predictions work ("Visible in X minutes")
- ✅ Battery usage <5% per hour
- ✅ App works with degraded GPS
- ✅ Play Store approved

---

## 📚 Documentation

- [Implementation Plan](.claude/plans/sprightly-orbiting-diffie.md)
- [API Integration Guide](docs/API_INTEGRATION.md) - Complete setup instructions
- [Build Guide](docs/BUILD_GUIDE.md) - Comprehensive build documentation
- [Java Setup](docs/JAVA_SETUP.md) - JDK installation guide
- [Privacy Policy](docs/PRIVACY_POLICY.md)
- [Database Migrations](docs/migrations/) - Supabase SQL scripts

---

## 🔮 Future Enhancements (Post-10 Users)

- Offline mode with downloadable packs
- AR camera overlay
- Social features (share trips)
- Premium AI features (GPT-4 level narration)
- Monetization (freemium model)

---

## 📞 Support

For questions or issues, open a GitHub issue.

---

**Built with ❤️ using Claude AI**
