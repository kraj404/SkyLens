# 🎯 SkyLens — Milestone Tracking & Outputs

**Last Updated:** March 23, 2026
**Target Users:** 10 initial users
**Approach:** Simplified + AI-Enhanced

---

## 📊 Overall Progress

**Milestone 1:** 🚧 **40% Complete** (Day 1 of 3)
**Total Timeline:** 3-4 weeks

---

## Milestone 1: Project Scaffold + Navigation ✅ 40%

**Timeline:** Days 1-3
**Status:** 🚧 In Progress

### ✅ Completed (Day 1)
- [x] Project directory structure created
- [x] Android Gradle build files configured
- [x] Dependencies added (Compose, Hilt, Supabase, MapLibre, Claude SDK)
- [x] AndroidManifest.xml with permissions
- [x] Main application class (SkyLensApp.kt)
- [x] MainActivity with Compose setup
- [x] Material 3 theme (Color.kt, Theme.kt, Type.kt)
- [x] Navigation graph structure
- [x] Splash screen (with 2-second animation)
- [x] Auth screen placeholder
- [x] Planning screen placeholder
- [x] Map screen placeholder
- [x] History screen placeholder
- [x] String resources

### ⏳ Remaining (Days 2-3)
- [ ] Hilt DI modules (Database, Network, Location, Auth)
- [ ] Gradle wrapper and properties
- [ ] Build and verify app launches on device
- [ ] Test navigation flows
- [ ] Add bottom navigation bar
- [ ] Polish splash animation

### 🎬 Milestone 1 Demo Output

When complete, you'll be able to:

1. **Install APK on Android device**
   ```bash
   ./gradlew installDebug
   ```

2. **See working app navigation:**
   ```
   Launch app
     ↓
   Splash screen (2 seconds with airplane icon)
     ↓
   Auth screen (Google button + Skip button)
     ↓
   Planning screen (departure/arrival placeholders)
     ↓
   Navigate to Map screen (placeholder)
     ↓
   Navigate to History screen (placeholder)
   ```

3. **No crashes, smooth transitions**

4. **Screenshots:**
   - Splash screen with "SkyLens" branding
   - Auth screen with Google Sign-In button
   - Planning screen layout
   - Empty map placeholder
   - Empty history placeholder

---

## Milestone 2: Authentication Screens + Google Sign-In

**Timeline:** Days 4-6
**Status:** ⏳ Not Started

### Planned Features
- [ ] Google Credential Manager integration
- [ ] Supabase Auth client
- [ ] Token storage (DataStore)
- [ ] Auth ViewModel with state management
- [ ] OAuth flow handling
- [ ] Session persistence
- [ ] Sign-out functionality

### 🎬 Milestone 2 Demo Output

**Live Demo Flow:**
1. Open app → Auth screen appears
2. Tap "Continue with Google"
3. System Google account picker appears
4. Select account
5. App shows: "Welcome, [Your Name]!"
6. Close app and reopen → Still signed in (session restored)
7. Tap "Sign Out" → Returns to auth screen
8. Tap "Skip" → Use app anonymously

**Screenshots:**
- Google account selection dialog
- Welcome screen with user profile
- Signed-in state indicator

**Verification:**
- Token stored securely in DataStore
- Session persists after app restart
- Anonymous mode works (no backend calls fail)

---

## Milestone 3: Core Data Models + Supabase Integration

**Timeline:** Days 7-11
**Status:** ⏳ Not Started

### Planned Features
- [ ] Room database with all entities
- [ ] Database DAOs (Airport, Landmark, Trip)
- [ ] Supabase client configuration
- [ ] Repository layer (Airport, Landmark)
- [ ] Domain models (Airport, Landmark, Trip)
- [ ] Claude API client setup
- [ ] Test data seeding

### 🎬 Milestone 3 Demo Output

**Working Features:**
1. **Airport Search:**
   ```
   Type "Tokyo" → Shows:
   - NRT (Tokyo Narita)
   - HND (Tokyo Haneda)
   ```

2. **Landmark Query:**
   ```
   Backend returns 50 landmarks near Tokyo
   Stored in local Room database
   ```

3. **Claude AI Test:**
   ```
   Send: "Generate a story for Mount Fuji"
   Receive: [AI-generated 200-word story]
   ```

**Database State:**
- Room DB contains 7,000 airports
- Room DB contains 5,000 landmarks (seeded)
- Supabase connected and verified

**Screenshots:**
- Airport search working with autocomplete
- Landmark list displaying
- Claude API response in logs

---

## Milestone 4: GPS Tracking + Map Display

**Timeline:** Days 12-16
**Status:** ⏳ Not Started

### Planned Features
- [ ] Location permission flow with educational dialogs
- [ ] FusedLocationProvider integration
- [ ] GPS tracking service (5-second intervals)
- [ ] MapLibre map integration (AndroidView wrapper)
- [ ] Live position marker (blue dot)
- [ ] Map camera follows user
- [ ] Flight status bar (altitude, speed, heading)
- [ ] Mock GPS playback for testing

### 🎬 Milestone 4 Demo Output

**Live Demo:**
1. **Permission Flow:**
   ```
   App requests location permission
   → Shows educational dialog explaining why
   → User grants permission
   ```

2. **Map Display:**
   ```
   Map loads showing your current location
   Blue dot marker at your position
   Map follows as you walk around
   ```

3. **Flight Simulation:**
   ```
   Tap "Simulate Flight"
   → Mock GPS plays LAX → NRT route
   → Map animates aircraft position
   → Status bar shows: "Alt: 35,000 ft, Speed: 850 km/h"
   ```

**Screenshots:**
- Permission request dialog
- Map with live position
- Flight simulation in progress
- Status bar with flight metrics

**Performance:**
- Map renders at 60 FPS
- GPS updates smoothly every 5 seconds
- Battery usage logged (<5% per hour target)

---

## Milestone 5: AI Landmark Narrator ⭐ **AI-FIRST**

**Timeline:** Days 17-21
**Status:** ⏳ Not Started

### Planned AI Features
- [ ] Real-time landmark visibility calculations
- [ ] Landmark markers on map
- [ ] Claude AI story generation (on-demand)
- [ ] AI flight narrator (continuous commentary)
- [ ] AI photo captions (Claude Vision)
- [ ] Upcoming landmark predictions with AI context
- [ ] Notification system

### 🎬 Milestone 5 Demo Output

**AI in Action:**

1. **Real-Time AI Narration:**
   ```
   [You're over Nevada]
   Claude says: "You're currently flying over the Mojave Desert,
   a vast arid region covering 47,000 square miles. This area
   played a crucial role in aviation testing during the 1950s..."
   ```

2. **Landmark Discovery:**
   ```
   Red markers appear on map for visible landmarks
   Tap "Grand Canyon" marker
   → Claude generates story in 2 seconds:
   "The Grand Canyon, one of Earth's most spectacular geological
   features, stretches 277 miles long and up to 18 miles wide..."
   ```

3. **AI Predictions:**
   ```
   Notification appears:
   🔔 "Mount Fuji will be visible in 4 minutes on your right.
      At 3,776 meters, it's Japan's tallest peak and has been
      a sacred site for centuries."
   ```

4. **Photo Analysis:**
   ```
   Landmark photo appears
   Claude Vision caption: "Snow-capped volcanic peak rising
   above cloud layer, with distinctive symmetrical cone shape"
   ```

**Screenshots:**
- Map with multiple landmark markers
- Landmark detail sheet with AI story
- AI narrator panel showing real-time commentary
- Prediction notification
- Photo with AI caption

**AI Metrics:**
- Story generation: <3 seconds per landmark
- Narrator updates: Every 30 seconds
- Token usage: ~2000 tokens per flight hour
- Cost: ~$0.02 per hour of flight

---

## Milestone 6: AI Q&A + Trip Summaries

**Timeline:** Days 22-24
**Status:** ⏳ Not Started

### Planned AI Features
- [ ] In-flight AI chat interface
- [ ] Conversational Q&A about landmarks
- [ ] Context-aware responses
- [ ] Trip AI summarizer (post-flight)
- [ ] AI recommendations

### 🎬 Milestone 6 Demo Output

**Conversational AI:**

1. **During Flight Q&A:**
   ```
   User: "What's the tallest mountain I can see?"

   Claude: "Based on your current position over central Nevada,
   you can see Mount Whitney to your left, which at 14,505 feet
   (4,421m) is the tallest peak in the contiguous United States.
   It's approximately 180 km from your position."
   ```

2. **Follow-up Questions:**
   ```
   User: "Tell me more about Mount Whitney"

   Claude: "Mount Whitney is located in the Sierra Nevada range
   and was named after Josiah Whitney, California's State Geologist..."
   ```

3. **AI Trip Summary (After Landing):**
   ```
   Your 5-hour journey from Los Angeles to Tokyo was spectacular!

   🏔 Highlights:
   - Sierra Nevada Mountains (including Mount Whitney, 14,505 ft)
   - Mojave Desert expanse
   - Pacific Ocean crossing
   - Aleutian Islands arc
   - Mount Fuji approach into Tokyo

   📸 You flew over 3 UNESCO World Heritage Sites
   🌍 Crossed 8 time zones
   ✈️ Total distance: 8,815 km

   Your journey traced ancient migration routes and followed
   paths used by early aviators who pioneered transpacific flight...
   ```

**Screenshots:**
- Chat interface during flight
- AI Q&A conversation
- Trip summary card with AI highlights
- Share trip button

---

## Milestone 7: Polish + Play Store Prep

**Timeline:** Days 25-28
**Status:** ⏳ Not Started

### Planned Features
- [ ] App icon design
- [ ] Loading animations
- [ ] Error state UIs
- [ ] Retry logic
- [ ] Settings screen
- [ ] Privacy policy page
- [ ] About screen
- [ ] Play Store assets (screenshots, graphics)
- [ ] APK signing configuration

### 🎬 Milestone 7 Demo Output

**Play Store Submission Package:**

1. **Polished App:**
   - Custom app icon (airplane + AI theme)
   - Smooth animations between screens
   - Skeleton loading states
   - Error handling with friendly messages
   - Retry buttons for failed operations

2. **Play Store Assets:**
   - App icon (512×512)
   - Feature graphic (1024×500)
   - 4-6 screenshots showing key features
   - Short description: "AI-powered landmark discovery from airplane windows"
   - Full description (marketing copy)

3. **Compliance:**
   - Privacy policy live at skylens.app/privacy
   - Data safety form filled
   - Content rating: Everyone
   - APK signed with release keystore

4. **Internal Testing:**
   - APK uploaded to internal testing track
   - Tested on 3+ devices
   - No crashes in production build

**Final Deliverable:**
- `app-release.aab` ready for Play Store upload
- Privacy policy URL active
- All compliance requirements met

---

## 🎥 Video Demo Plan

**For Milestone 5 (Full Feature Demo):**

1. **Intro (0:00-0:15):**
   - Show SkyLens logo and tagline
   - "Discover landmarks with AI from your airplane window"

2. **Authentication (0:15-0:30):**
   - Open app
   - Tap "Continue with Google"
   - Sign-in completes

3. **Flight Planning (0:30-0:50):**
   - Select LAX (departure)
   - Select NRT (arrival)
   - Tap "Start Flight"

4. **Live Flight Demo (0:50-2:30):**
   - Map displays with position marker
   - Status bar: "Alt: 35,000 ft, Speed: 850 km/h"
   - AI Narrator box appears:
     > "You're currently crossing the Sierra Nevada mountain range..."
   - Landmark markers appear (Grand Canyon, Rocky Mountains)
   - Tap Mount Fuji marker
   - Bottom sheet opens with AI-generated story
   - Photos appear with AI captions
   - Prediction notification: "Tokyo Bay visible in 6 minutes"

5. **AI Chat Demo (2:30-3:00):**
   - Tap chat icon
   - Ask: "What's that snowy mountain?"
   - Claude responds instantly with detailed answer

6. **Trip Summary (3:00-3:30):**
   - Flight ends
   - AI generates trip summary
   - Show beautiful summary card with highlights

7. **History (3:30-3:45):**
   - Navigate to history screen
   - See saved trips
   - Tap trip → Replay mode

8. **Outro (3:45-4:00):**
   - "Download SkyLens on Google Play"
   - Call to action

---

## 🧪 Testing Checklist

### After Milestone 1
- [ ] App installs without errors
- [ ] Navigation works between all screens
- [ ] No runtime crashes
- [ ] Theme renders correctly

### After Milestone 2
- [ ] Google Sign-In completes successfully
- [ ] Session persists after app restart
- [ ] Skip button works (anonymous mode)
- [ ] Sign-out clears session

### After Milestone 3
- [ ] Airport search returns results
- [ ] Supabase connection established
- [ ] Room database created
- [ ] Claude API responds

### After Milestone 4
- [ ] Location permissions granted
- [ ] GPS tracking active
- [ ] Map displays current position
- [ ] Mock GPS simulation works

### After Milestone 5
- [ ] Landmarks appear on map
- [ ] AI stories generate correctly
- [ ] Predictions work
- [ ] Narrator updates in real-time

### After Milestone 6
- [ ] Chat interface functional
- [ ] AI Q&A works
- [ ] Trip summaries generate
- [ ] History saves locally

### After Milestone 7
- [ ] All animations smooth
- [ ] Error states handled
- [ ] Play Store assets ready
- [ ] Privacy policy published
- [ ] APK signed

---

## 📸 Screenshot Plan for Play Store

### Required Screenshots (4 minimum)

1. **Hero Shot:** Flight map with aircraft position + landmark markers
2. **AI Story:** Landmark detail with Claude-generated story
3. **Predictions:** Notification showing upcoming landmark
4. **Trip Summary:** AI-generated post-flight summary

### Optional Screenshots (5-6 total recommended)
5. **AI Chat:** Q&A interface conversation
6. **Planning:** Airport selection screen
7. **History:** Trip history list

---

## 🚀 Build Commands

### Development Build
```bash
cd android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Run on Emulator
```bash
./gradlew installDebug
adb shell am start -n com.skylens.app/.MainActivity
```

### Release Build (After Milestone 7)
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

### Check Build Status
```bash
./gradlew build --info
```

---

## 📦 Current File Structure

```
skylens/
├── android/
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/skylens/
│   │   │   │   ├── SkyLensApp.kt ✅
│   │   │   │   ├── MainActivity.kt ✅
│   │   │   │   ├── di/
│   │   │   │   │   └── AppModule.kt ✅
│   │   │   │   └── presentation/ui/
│   │   │   │       ├── navigation/
│   │   │   │       │   └── NavGraph.kt ✅
│   │   │   │       ├── screens/
│   │   │   │       │   ├── splash/
│   │   │   │       │   │   └── SplashScreen.kt ✅
│   │   │   │       │   ├── auth/
│   │   │   │       │   │   └── AuthScreen.kt ✅
│   │   │   │       │   ├── planning/
│   │   │   │       │   │   └── FlightPlanningScreen.kt ✅
│   │   │   │       │   ├── flight/
│   │   │   │       │   │   └── FlightMapScreen.kt ✅
│   │   │   │       │   └── history/
│   │   │   │       │       └── TripHistoryScreen.kt ✅
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt ✅
│   │   │   │           ├── Theme.kt ✅
│   │   │   │           └── Type.kt ✅
│   │   │   ├── res/values/
│   │   │   │   └── strings.xml ✅
│   │   │   └── AndroidManifest.xml ✅
│   │   ├── build.gradle.kts ✅
│   │   └── proguard-rules.pro ⏳
│   ├── build.gradle.kts ✅
│   └── settings.gradle.kts ✅
├── docs/
├── scripts/
├── README.md ✅
└── MILESTONE_STATUS.md ✅ (this file)
```

**Files Created:** 15
**Lines of Code:** ~800

---

## 🎯 Next Immediate Tasks (to Complete Milestone 1)

1. Create remaining Hilt modules:
   - DatabaseModule.kt
   - NetworkModule.kt
   - AuthModule.kt

2. Add Gradle wrapper files:
   - gradlew
   - gradlew.bat
   - gradle/wrapper/gradle-wrapper.properties

3. Create proguard-rules.pro

4. Build and test on device

5. Verify navigation flows

---

## 💡 Key Decisions Log

| Decision | Rationale |
|----------|-----------|
| Skip Rust backend | Use Supabase directly for 10 users; simpler, faster |
| Online-only mode | Defer offline packs; stream landmarks in real-time |
| AI-first approach | Maximize Claude API usage per user request |
| Free tier only | No monetization complexity for initial launch |
| Target 10 users | Validate concept before scaling infrastructure |

---

## 📞 Development Notes

**Current Status (End of Day 1):**
- ✅ Project scaffolding complete
- ✅ Navigation structure defined
- ✅ Dependencies configured
- ⏳ Ready to build and test

**Blockers:** None

**Next Session:** Complete Milestone 1 (add DI modules, build APK, verify on device)

---

**Milestone 1 ETA: 2 more days**
**Full MVP ETA: 3-4 weeks**
