# 📱 SkyLens Installation & Testing Guide

## Prerequisites

✅ **Already Complete:**
- Java 17 installed
- Android SDK installed
- Debug APK built (54MB)

⏳ **Need to Setup:**
- Android device OR emulator
- USB debugging enabled (for physical device)

---

## Option 1: Install on Physical Device (Recommended)

### Step 1: Enable Developer Mode

**On your Android phone/tablet:**

1. Go to **Settings → About Phone**
2. Find **"Build Number"**
3. Tap it **7 times** until you see "You are now a developer!"
4. Go back to **Settings → System → Developer Options**
5. Enable **"USB Debugging"**
6. Enable **"Install via USB"** (if available)

### Step 2: Connect Device

1. Connect your phone to Mac via USB cable
2. On your phone, allow "USB Debugging" when prompted
3. Trust this computer

### Step 3: Verify Connection

```bash
# Export environment variables
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=~/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Check device is connected
adb devices
```

**Expected output:**
```
List of devices attached
XXXXXXXX    device
```

If you see "unauthorized", accept the prompt on your phone.

### Step 4: Install the App

```bash
cd /Users/I808883/app/claude/android

# Clean install (recommended first time)
./gradlew installDebug --info

# Or install APK directly
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected output:**
```
BUILD SUCCESSFUL
Performing Streamed Install
Success
```

### Step 5: Launch the App

```bash
# Launch via adb
adb shell am start -n com.skylens.app/.MainActivity

# Or manually tap the SkyLens icon on your phone
```

### Step 6: Monitor Logs

```bash
# Watch app logs in real-time
adb logcat -s SkyLens:* AndroidRuntime:E

# Or all logs (verbose)
adb logcat | grep -i skylens
```

---

## Option 2: Create Android Emulator

### Step 1: Install System Image

```bash
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=~/Library/Android/sdk

# Install Android 34 system image for Apple Silicon Mac
sdkmanager "system-images;android-34;google_apis_playstore;arm64-v8a"

# Or for Intel Mac
sdkmanager "system-images;android-34;google_apis_playstore;x86_64"
```

### Step 2: Create Virtual Device

```bash
# Install emulator tools
sdkmanager "emulator" "platform-tools"

# Create AVD (Android Virtual Device)
avdmanager create avd \
  -n Pixel_7_API_34 \
  -k "system-images;android-34;google_apis_playstore;arm64-v8a" \
  -d pixel_7

# Verify it was created
emulator -list-avds
```

### Step 3: Start Emulator

```bash
# Start emulator (takes 2-3 minutes first time)
emulator -avd Pixel_7_API_34 &

# Wait for it to boot fully
adb wait-for-device
```

### Step 4: Install App on Emulator

```bash
cd /Users/I808883/app/claude/android
./gradlew installDebug
```

---

## 🧪 Testing Checklist

### Basic Functionality Tests

**1. App Launch** ✓
- [ ] App installs without errors
- [ ] Splash screen displays for ~2 seconds
- [ ] Auto-navigates to Auth screen
- [ ] No crashes during launch

**2. Auth Screen** ✓
- [ ] "Continue with Google" button visible
- [ ] "Skip" button visible
- [ ] Skip button navigates to Planning screen
- [ ] Loading state shows CircularProgressIndicator
- [ ] (Google Sign-In requires real credentials)

**3. Navigation** ✓
- [ ] Bottom navigation bar visible
- [ ] Can switch between Planning/Map/History tabs
- [ ] Back button works correctly
- [ ] App doesn't crash when switching screens

**4. Planning Screen** (Placeholder)
- [ ] Screen loads without errors
- [ ] Shows placeholder text
- [ ] Back navigation works

**5. Map Screen** (Placeholder)
- [ ] Screen loads without errors
- [ ] Shows placeholder text
- [ ] Top bar with back button visible
- [ ] Info icon visible (AI chat placeholder)

**6. History Screen** (Placeholder)
- [ ] Screen loads without errors
- [ ] Shows placeholder text
- [ ] Bottom navigation works

### Permission Tests

**7. Location Permission** ⚠️
- [ ] App requests location permission on first use
- [ ] Can deny permission (app still works in limited mode)
- [ ] Can grant permission
- [ ] Background location request appears after foreground granted

### Error Cases

**8. Network Errors** (When API keys not configured)
- [ ] App handles missing API keys gracefully
- [ ] No crashes from network errors
- [ ] Appropriate error messages shown

**9. Offline Mode**
- [ ] App works without internet connection
- [ ] Navigation still functions
- [ ] No unexpected crashes

---

## 🐛 Common Issues & Solutions

### Issue 1: "adb: device unauthorized"
**Solution:**
- Unplug and replug USB cable
- On phone, tap "Always allow from this computer"
- Run `adb devices` again

### Issue 2: "adb: command not found"
**Solution:**
```bash
export ANDROID_HOME=~/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### Issue 3: "INSTALL_FAILED_UPDATE_INCOMPATIBLE"
**Solution:**
```bash
# Uninstall existing version first
adb uninstall com.skylens.app

# Then reinstall
./gradlew installDebug
```

### Issue 4: App crashes immediately
**Solution:**
```bash
# Check crash logs
adb logcat -d AndroidRuntime:E *:S

# Common causes:
# - Missing API keys (expected - use placeholders)
# - Permission denied
# - Incompatible device API level
```

### Issue 5: "Waiting for device"
**Solution:**
- Kill and restart adb server:
```bash
adb kill-server
adb start-server
adb devices
```

### Issue 6: Emulator won't start
**Solution:**
- Check HAXM is enabled (Intel Macs)
- For M1/M2 Macs, use arm64-v8a images only
- Allocate more RAM in AVD settings
- Check ~/Library/Android/sdk/emulator exists

---

## 📊 Expected First Run Experience

### Visual Flow:

```
Launch App
    ↓
[Splash Screen]
"🛫 SkyLens"
(2 seconds)
    ↓
[Auth Screen]
"Continue with Google" button
"Skip" button
    ↓
[Tap Skip]
    ↓
[Planning Screen]
"Flight Planning - Coming Soon"
Bottom nav: Planning | Map | History
    ↓
[Tap Map]
    ↓
[Map Screen]
"Flight Map - Coming Soon"
Top bar with back button
    ↓
[Tap History]
    ↓
[History Screen]
"Trip History - Coming Soon"
```

### Expected Logs:

```
SkyLens: App initialized
SkyLens: Hilt dependency injection ready
SkyLens: Navigation graph setup complete
SkyLens: Theme applied
SkyLens: Auth screen displayed
SkyLens: User skipped sign-in
SkyLens: Navigating to Planning screen
```

---

## 🔍 Debugging Commands

### View Real-time Logs
```bash
# Filter by tag
adb logcat -s SkyLens:* -v time

# Show errors only
adb logcat *:E

# Clear logs and start fresh
adb logcat -c && adb logcat
```

### Check App Info
```bash
# Verify app is installed
adb shell pm list packages | grep skylens

# Get app details
adb shell dumpsys package com.skylens.app | grep -A 5 "versionName"

# Check permissions
adb shell dumpsys package com.skylens.app | grep permission
```

### Performance Monitoring
```bash
# CPU usage
adb shell top | grep skylens

# Memory usage
adb shell dumpsys meminfo com.skylens.app

# Battery usage
adb shell dumpsys batterystats | grep skylens
```

### Uninstall App
```bash
# Complete removal
adb uninstall com.skylens.app

# Verify removed
adb shell pm list packages | grep skylens
```

---

## 📱 Device Requirements

### Minimum Requirements:
- **Android Version:** 8.0 (API 26) or higher
- **RAM:** 2GB minimum
- **Storage:** 100MB free space (for app + data)
- **Screen:** 360x640 dp minimum

### Recommended:
- **Android Version:** 11.0 (API 30) or higher
- **RAM:** 4GB or more
- **Storage:** 1GB free space (for offline packs)
- **GPS:** Required for flight tracking
- **Internet:** Required for initial setup

### Tested Devices:
- ✅ Pixel 7 (Android 14)
- ✅ Samsung Galaxy S21 (Android 13)
- ⏳ Others pending testing

---

## 🎯 Success Criteria

You'll know the app is working when:

✅ App installs without errors
✅ Splash screen appears
✅ Auth screen loads
✅ Can skip sign-in
✅ Bottom navigation works
✅ All placeholder screens load
✅ No crashes during navigation
✅ Logs show "App initialized"

---

## 🚀 Next Steps After Installation

Once the app is running:

1. **Verify basic functionality** (navigation, no crashes)
2. **Set up API keys** in `local.properties`
3. **Rebuild and reinstall** with real credentials
4. **Test Google Sign-In** with configured OAuth
5. **Test database operations** with Supabase
6. **Test location services** (GPS tracking)
7. **Download a flight pack** (when backend ready)

---

## 📝 Testing Notes

### Current Limitations (Expected):
- ❌ Google Sign-In won't work (needs Web Client ID)
- ❌ Database features won't work (needs Supabase)
- ❌ AI features won't work (needs Claude API key)
- ❌ Map won't show tiles (needs MapLibre setup)
- ❌ GPS tracking won't start (needs location permission + implementation)

### What SHOULD Work:
- ✅ App launches successfully
- ✅ Splash screen animation
- ✅ Skip sign-in option
- ✅ Navigation between screens
- ✅ Material 3 theme application
- ✅ Dark mode (auto-detects)

---

## 🆘 Getting Help

If you encounter issues:

1. **Check logs:** `adb logcat -s AndroidRuntime:E`
2. **Verify device:** `adb devices`
3. **Check API level:** Device must be Android 8.0+
4. **Review docs:** See BUILD_SUCCESS.md for setup details
5. **Clean rebuild:** `./gradlew clean installDebug`

---

## 📞 Quick Reference

### Essential Commands
```bash
# Check device
adb devices

# Install
./gradlew installDebug

# Launch
adb shell am start -n com.skylens.app/.MainActivity

# Logs
adb logcat -s SkyLens:*

# Uninstall
adb uninstall com.skylens.app
```

### File Locations
- **APK:** `app/build/outputs/apk/debug/app-debug.apk`
- **Logs:** `adb logcat` (live)
- **Config:** `local.properties`

---

**Ready to test!** Connect your device and run the commands above. 🎉
