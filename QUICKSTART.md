# SkyLens - Quick Start Guide

## Step 1: Install Java Development Kit (JDK)

### Option A: Using Homebrew (Recommended)
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 17 (required for Android development)
brew install openjdk@17

# Link Java
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Verify installation
java -version
# Should show: openjdk version "17.x.x"
```

### Option B: Manual Download
1. Download Java 17 from: https://adoptium.net/temurin/releases/
2. Choose: macOS, aarch64 (Apple Silicon), JDK 17
3. Install the .pkg file
4. Verify: `java -version`

## Step 2: Configure Android Build

```bash
cd /Users/I808883/app/claude/android

# Create local.properties if it doesn't exist
cat > local.properties << 'EOF'
# Android SDK location (update if different)
sdk.dir=/Users/I808883/Library/Android/sdk

# API Keys (use test values for now)
SUPABASE_URL=https://test.supabase.co
SUPABASE_ANON_KEY=test-key
GOOGLE_WEB_CLIENT_ID=test-client-id.apps.googleusercontent.com
EOF

# Make gradlew executable
chmod +x gradlew
```

## Step 3: Build Debug APK

```bash
cd /Users/I808883/app/claude/android

# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

## Step 4: Install on Phone

### Option A: Via USB (ADB)
```bash
# Enable USB debugging on your phone:
# Settings → About Phone → Tap "Build Number" 7 times
# Settings → Developer Options → Enable "USB Debugging"

# Connect phone via USB cable

# Verify device is connected
adb devices
# Should show your device

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.skylens.app/.MainActivity
```

### Option B: Via File Transfer
```bash
# Copy APK to Downloads
cp app/build/outputs/apk/debug/app-debug.apk ~/Downloads/skylens.apk

# Transfer to phone via:
# - AirDrop
# - Email attachment
# - Cloud storage (Google Drive, iCloud)

# On phone: Open the APK file and install
# (You may need to allow "Install from unknown sources")
```

### Option C: Via Android Studio
1. Open Android Studio
2. File → Open → Select `/Users/I808883/app/claude/android`
3. Wait for Gradle sync
4. Connect phone via USB
5. Click green "Run" button (▶️)
6. Select your device

## Step 5: Grant Permissions

When app launches:
1. Complete onboarding tutorial (swipe through 6 pages)
2. Grant location permission when prompted
3. Choose "Skip for now" on sign-in screen
4. Enter test flight route (e.g., LAX → NRT)

## Step 6: Test Features

### Basic Flow Test:
```
1. ✅ App launches (splash screen)
2. ✅ Onboarding shows (6 pages)
3. ✅ Permissions requested
4. ✅ Flight planning screen appears
5. ✅ Enter "LAX" in departure → Select airport
6. ✅ Enter "NRT" in arrival → Select airport
7. ✅ Tap "Start Flight"
8. ✅ Download screen shows (or skip)
9. ✅ Flight map appears with your location
10. ✅ Landmarks visible on map
```

### Feature Testing Checklist:
- [ ] GPS location updates on map
- [ ] Tap landmark marker → Shows info popup
- [ ] Tap "Ask AI" → Chat interface works
- [ ] Send message → AI responds (mock or real)
- [ ] Tap POI badge → Shows landmark list
- [ ] Tap landmark in list → Details screen
- [ ] Back button → Returns to map
- [ ] Menu → Settings → Opens settings
- [ ] Menu → Trip History → Shows history
- [ ] Menu → About → Shows app info

## Troubleshooting

### Build Errors

**Error: "Unable to locate Java Runtime"**
```bash
# Check Java installation
java -version

# If not found, install Java 17 (see Step 1)
```

**Error: "SDK location not found"**
```bash
# Update local.properties with correct SDK path
echo "sdk.dir=/Users/I808883/Library/Android/sdk" > android/local.properties

# Or install Android SDK via Android Studio
```

**Error: "Gradle sync failed"**
```bash
# Delete cache and rebuild
cd android
rm -rf .gradle
./gradlew clean build
```

### Runtime Errors

**App crashes on launch:**
- Check logcat: `adb logcat | grep skylens`
- Look for stack traces

**Location not updating:**
- Verify location permissions granted
- Try outdoors for better GPS signal
- Check Settings → Mock GPS is enabled

**Landmarks not showing:**
- Database may be empty (no landmarks imported)
- Check logs: `adb logcat | grep Landmark`
- Expected: Mock landmarks should appear

### View Logs

```bash
# Real-time logs
adb logcat | grep -i skylens

# Filter by tag
adb logcat -s FlightMapViewModel

# Clear logs and start fresh
adb logcat -c
adb logcat | grep -i skylens
```

## Quick Test Without Building

If build fails and you want to see the flow:

1. Open Android Studio
2. File → Open → Select `android` folder
3. Let Gradle sync complete
4. Tools → Device Manager → Create virtual device
5. Run on emulator

## Next Steps After Testing

1. **Report Issues**: Note any crashes or bugs
2. **Check Features**: Which features work vs. don't work?
3. **Performance**: Does GPS update smoothly? Battery drain?
4. **UI/UX**: Any UI issues or confusing flows?

---

**Ready to start? Run the commands in order!** 🚀
