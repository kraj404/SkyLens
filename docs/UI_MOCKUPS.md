# 🎨 SkyLens UI Mockups & Design

## Overview

SkyLens features a clean, modern Material 3 design optimized for in-flight use with large touch targets and clear typography.

---

## 🎨 Design System

### Color Palette
```
Primary:   #1976D2 (Sky Blue)
Secondary: #FF6F00 (Sunset Orange)
Background: #FFFFFF (Light) / #121212 (Dark)
Surface:    #F5F5F5 (Light) / #1E1E1E (Dark)
```

### Typography
- **Headline:** Large, bold for screen titles
- **Body:** Medium weight for content
- **Caption:** Small for metadata

### Components
- Material 3 Scaffold with TopAppBar
- Floating Action Buttons for primary actions
- Bottom sheets for landmark details
- Cards for trip summaries

---

## 📱 Screen Flow

```
┌─────────────────────┐
│   Splash Screen     │ (2 seconds)
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│   Auth Screen       │ ← Optional Google Sign-In
│  "Continue with     │   or Skip
│   Google" button    │
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│ Flight Planning     │ ← Select airports
│  [Departure]        │   Search & select
│  [Arrival]          │   Download pack
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│   Flight Map        │ ← Main screen
│  (Real-time)        │   GPS tracking
│   + Landmarks       │   AI features
└─────────────────────┘
           ↑
           │
┌──────────┴──────────┐
│   Trip History      │ ← Past trips
│  (Review/Replay)    │   AI summaries
└─────────────────────┘
```

---

## 1️⃣ Splash Screen

```
┌─────────────────────────────────┐
│                                 │
│                                 │
│          🛫 SkyLens            │
│                                 │
│     Identify Landmarks from     │
│        Your Airplane            │
│                                 │
│                                 │
│         [Loading...]            │
│                                 │
│                                 │
└─────────────────────────────────┘
```

**Implementation Status:** ✅ Complete
- Displays for 2 seconds
- Auto-navigates to Auth screen
- Simple branding

---

## 2️⃣ Auth Screen

```
┌─────────────────────────────────┐
│         < Back                  │
├─────────────────────────────────┤
│                                 │
│          🛫 SkyLens            │
│                                 │
│      Welcome to SkyLens!        │
│                                 │
│   Discover landmarks visible    │
│     from airplane windows       │
│                                 │
│  ┌───────────────────────────┐ │
│  │  🔵 Continue with Google  │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │      Skip for Now         │ │
│  └───────────────────────────┘ │
│                                 │
│    Works 100% offline after     │
│    downloading flight pack      │
│                                 │
└─────────────────────────────────┘
```

**Implementation Status:** ✅ Complete
- Google Sign-In via Credential Manager
- Skip option available
- Loading state shows CircularProgressIndicator
- Error state displays error message

**Features:**
- Optional authentication
- Supabase integration
- Credential Manager API
- Offline-first approach

---

## 3️⃣ Flight Planning Screen

```
┌─────────────────────────────────┐
│  Plan Your Flight               │
├─────────────────────────────────┤
│                                 │
│  Departure Airport:             │
│  ┌───────────────────────────┐ │
│  │ 🔍 Search... (e.g., LAX)  │ │
│  └───────────────────────────┘ │
│                                 │
│  Selected: Los Angeles (LAX)    │
│                                 │
│  ──────────────────────────────│
│                                 │
│  Arrival Airport:               │
│  ┌───────────────────────────┐ │
│  │ 🔍 Search... (e.g., NRT)  │ │
│  └───────────────────────────┘ │
│                                 │
│  Selected: Tokyo Narita (NRT)   │
│                                 │
│  ──────────────────────────────│
│                                 │
│  Route Preview:                 │
│  ├─ Distance: 8,815 km         │
│  ├─ Flight Time: ~11 hours     │
│  └─ Landmarks: 2,134 nearby    │
│                                 │
│  ┌───────────────────────────┐ │
│  │  Download Flight Pack     │ │
│  │      (487 MB)             │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

**Implementation Status:** ⚠️ Partial (Placeholder UI)
- Basic structure exists
- Airport search: TODO
- Pack download: TODO
- Route preview: TODO

**Planned Features:**
- Airport autocomplete search
- Route corridor visualization
- Pack size estimation
- Download progress bar
- Offline pack management

---

## 4️⃣ Flight Map Screen (Main View)

```
┌─────────────────────────────────┐
│  ← SkyLens         💬  📍  ⋮   │ ← Top bar
├─────────────────────────────────┤
│                                 │
│      [MAP VIEW AREA]            │ ← MapLibre
│                                 │
│      🏔️ Mount Fuji             │
│      89.4 km                    │ ← Landmark markers
│                                 │
│      🏙️ Tokyo                   │
│      45.2 km                    │
│                                 │
│      ✈️ Your Position           │ ← Aircraft icon
│                                 │
│                                 │
├─────────────────────────────────┤
│ 🔔 Mount Fuji visible in 4 min │ ← Prediction banner
├─────────────────────────────────┤
│ Alt: 35,000 ft  Speed: 850 km/h│ ← Status bar
│ LAX → NRT (3h 45m remaining)    │
├─────────────────────────────────┤
│  🗺  Planning  🏔  Map  📚  Log │ ← Bottom nav
└─────────────────────────────────┘

[Tap landmark → Bottom sheet opens]

┌─────────────────────────────────┐
│  Mount Fuji                   ✕ │
│  Mountain • 89.4 km away        │
├─────────────────────────────────┤
│  [Photo Gallery]                │
│  📷 📷 📷                         │
├─────────────────────────────────┤
│                                 │
│  🏔️ Elevation: 3,776 m         │
│  📍 Shizuoka, Japan             │
│                                 │
│  ──────────────────────────────│
│                                 │
│  Mount Fuji is Japan's tallest  │
│  peak and an iconic symbol...   │
│  [AI-generated story]           │
│                                 │
│  ┌───────────────────────────┐ │
│  │   🤖 Ask AI About This    │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │   🔗 Read on Wikipedia    │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

**Implementation Status:** ⚠️ Partial
- Basic map container exists
- MapLibre integration: TODO
- Landmark markers: TODO
- Bottom sheet: ✅ Complete
- Predictions: TODO
- Status bar: TODO

**Features Implemented:**
- LandmarkDetailSheet component
- Photo gallery support
- AI story display
- Wikipedia links
- Distance calculation

**Planned Features:**
- Real-time GPS tracking (5 sec intervals)
- Landmark visibility calculation
- AI Q&A chat interface
- Prediction notifications
- Flight narration (30 sec intervals)

---

## 5️⃣ Trip History Screen

```
┌─────────────────────────────────┐
│  Trip History                   │
├─────────────────────────────────┤
│                                 │
│  Recent Trips:                  │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 📅 Mar 20, 2026           │ │
│  │ LAX → NRT                 │ │
│  │ 11h 23m • 47 landmarks    │ │
│  │                           │ │
│  │ Highlights: Mount Fuji,   │ │
│  │ Tokyo Bay, Mount Rainier  │ │
│  │                           │ │
│  │ [View Details] [Replay]   │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │ 📅 Feb 15, 2026           │ │
│  │ JFK → LHR                 │ │
│  │ 6h 48m • 23 landmarks     │ │
│  │                           │ │
│  │ Highlights: Statue of     │ │
│  │ Liberty, Atlantic Ocean   │ │
│  │                           │ │
│  │ [View Details] [Replay]   │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │   🤖 Generate AI Summary  │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

**Implementation Status:** ⚠️ Partial (Placeholder UI)
- Basic structure exists
- Trip list: TODO
- Replay mode: TODO
- AI summary: TODO

**Planned Features:**
- Trip card with summary
- Replay animation
- AI-generated trip story
- Export as GeoJSON
- Share functionality

---

## 6️⃣ AI Chat Interface (Overlay)

```
┌─────────────────────────────────┐
│  ← Ask AI                     ✕ │
├─────────────────────────────────┤
│                                 │
│  🤖 What would you like to     │
│      know?                      │
│                                 │
│  ──────────────────────────────│
│                                 │
│  💬 You:                        │
│  "What's that mountain I can    │
│   see?"                         │
│                                 │
│  🤖 AI:                         │
│  "Based on your current         │
│   position, you're likely       │
│   seeing Mount Fuji! It's 89.4  │
│   km to your right. Would you   │
│   like to learn more?"          │
│                                 │
│  ──────────────────────────────│
│                                 │
│  Suggested Questions:           │
│  • What else is nearby?         │
│  • Tell me about Mount Fuji     │
│  • What will I see next?        │
│                                 │
│  ┌───────────────────────────┐ │
│  │ Type your question...     │ │
│  │                        🎙️ │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

**Implementation Status:** ⏳ Not Started
- UI design: Ready
- Claude API integration: Partial
- Chat state management: TODO

**Planned Features:**
- Conversational AI (Claude Haiku 4.5)
- Context-aware responses
- Suggested questions
- Voice input support
- Chat history

---

## 🎨 Component Library

### MapLibreMapView
- Full-screen map container
- Offline tile support
- Custom marker rendering
- Touch gestures (pan, zoom)

### LandmarkCard
- Compact landmark info
- Distance indicator
- Type icon
- Tap to expand

### LandmarkDetailSheet (✅ Complete)
- Modal bottom sheet
- Photo gallery
- AI-generated story
- Wikipedia link
- Q&A button

### FlightStatusBar
- Altitude display
- Speed indicator
- Route progress
- ETA calculation

### PredictionBanner
- Dismissible notification
- Countdown timer
- Landmark preview
- Distance update

### AirportSearchBar
- Autocomplete search
- Recent selections
- IATA code display

---

## 🌈 Dark Mode Support

All screens support automatic dark mode based on system settings:

```
Light Mode              Dark Mode
─────────────────────────────────────
#FFFFFF (Background)    #121212
#F5F5F5 (Surface)       #1E1E1E
#1976D2 (Primary)       #90CAF9
#000000 (Text)          #FFFFFF
```

---

## 📐 Responsive Design

### Layout Breakpoints
- **Phone (Portrait):** 360-428 dp width
- **Phone (Landscape):** 640-926 dp width
- **Tablet:** 960+ dp width

### Touch Targets
- **Minimum:** 48 dp × 48 dp (Material Design standard)
- **Buttons:** 56 dp height
- **FAB:** 56 dp diameter

---

## ♿ Accessibility

### Features
- **Content descriptions:** All interactive elements
- **Minimum contrast:** WCAG AA compliant
- **Font scaling:** Supports system text size
- **Screen reader:** TalkBack compatible
- **Focus indicators:** Clear visual feedback

### Text Sizes
- Large: 24sp (Headlines)
- Medium: 16sp (Body)
- Small: 12sp (Captions)

---

## 🎬 Animations

### Screen Transitions
- **Duration:** 300ms
- **Easing:** FastOutSlowIn

### Map Interactions
- **Marker pop:** Spring animation
- **Camera movement:** Ease-in-out
- **Zoom:** Linear interpolation

### Loading States
- **Circular progress:** Indeterminate
- **Skeleton screens:** Shimmer effect
- **Pull-to-refresh:** Material animation

---

## 📊 Current UI Implementation Status

| Screen | Structure | Styling | Data | Status |
|--------|-----------|---------|------|--------|
| Splash | ✅ | ✅ | ✅ | ✅ Complete |
| Auth | ✅ | ✅ | ⚠️ Partial | 90% |
| Planning | ✅ | ⚠️ | ❌ | 40% |
| Flight Map | ✅ | ⚠️ | ❌ | 30% |
| History | ✅ | ⚠️ | ❌ | 30% |
| Landmark Detail | ✅ | ✅ | ✅ | ✅ Complete |

### Legend
- ✅ Complete
- ⚠️ Partial (basic structure)
- ❌ Not Started

---

## 🚀 Next UI Development Tasks

### Priority 1 (Core Functionality)
1. Implement MapLibre map in FlightMapScreen
2. Add GPS position marker on map
3. Create landmark markers with clustering
4. Add real-time position updates
5. Implement airport search with autocomplete

### Priority 2 (Enhanced Features)
1. Add prediction banner system
2. Create flight status bar
3. Implement trip history cards
4. Add AI chat overlay
5. Create pack download progress UI

### Priority 3 (Polish)
1. Add loading animations
2. Implement dark mode refinements
3. Add haptic feedback
4. Create empty states
5. Add error recovery UIs

---

## 🎨 Design Resources

### Fonts
- **System Default:** Roboto (Android)
- **Fallback:** Sans-serif

### Icons
- **Material Icons:** Built-in
- **Custom:** SVG assets in `res/drawable/`

### Assets Needed
- App icon (512×512 px)
- Feature graphic (1024×500 px)
- Screenshots (4+ required for Play Store)

---

## 📱 Installation & Testing

To see the current UI:

```bash
cd android
./gradlew installDebug
adb shell am start -n com.skylens.app/.MainActivity
```

The app will show:
1. Splash screen (2 seconds)
2. Auth screen (Google Sign-In or Skip)
3. Bottom navigation (Planning, Map, History)
4. Placeholder content for non-auth screens

---

## 📝 Notes

- All screens use **Jetpack Compose** (no XML layouts)
- **Material 3** design system throughout
- **Offline-first** architecture (UI works without network)
- **Accessibility** is a priority (content descriptions, contrast)
- **Dark mode** auto-detects system preference

For implementation details, see the source code in:
```
android/app/src/main/java/com/skylens/presentation/ui/
```
