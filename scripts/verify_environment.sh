#!/bin/bash
# SkyLens Environment Setup Verification Script
# Run this to check if your development environment is ready

set -e

echo "🛫 SkyLens Environment Check"
echo "=============================="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

# Check Java
echo "Checking Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    JAVA_MAJOR=$(echo $JAVA_VERSION | cut -d'.' -f1)

    if [ "$JAVA_MAJOR" -ge 17 ]; then
        echo -e "${GREEN}✓${NC} Java $JAVA_VERSION installed"
    else
        echo -e "${RED}✗${NC} Java $JAVA_VERSION found, but version 17+ required"
        echo "  Install: brew install openjdk@17"
        ERRORS=$((ERRORS+1))
    fi
else
    echo -e "${RED}✗${NC} Java not installed"
    echo "  Install: brew install openjdk@17"
    echo "  See: docs/JAVA_SETUP.md"
    ERRORS=$((ERRORS+1))
fi
echo ""

# Check JAVA_HOME
echo "Checking JAVA_HOME..."
if [ -n "$JAVA_HOME" ]; then
    echo -e "${GREEN}✓${NC} JAVA_HOME set to: $JAVA_HOME"
else
    echo -e "${YELLOW}⚠${NC} JAVA_HOME not set"
    echo "  Add to ~/.zshrc: export JAVA_HOME=\$(/usr/libexec/java_home -v 17)"
    WARNINGS=$((WARNINGS+1))
fi
echo ""

# Check Android SDK
echo "Checking Android SDK..."
if [ -n "$ANDROID_HOME" ]; then
    echo -e "${GREEN}✓${NC} ANDROID_HOME set to: $ANDROID_HOME"
elif [ -d "$HOME/Library/Android/sdk" ]; then
    echo -e "${YELLOW}⚠${NC} Android SDK found at ~/Library/Android/sdk but ANDROID_HOME not set"
    echo "  Add to ~/.zshrc: export ANDROID_HOME=\$HOME/Library/Android/sdk"
    WARNINGS=$((WARNINGS+1))
else
    echo -e "${RED}✗${NC} Android SDK not found"
    echo "  Install Android Studio: https://developer.android.com/studio"
    ERRORS=$((ERRORS+1))
fi
echo ""

# Check local.properties
echo "Checking configuration..."
if [ -f "android/local.properties" ]; then
    echo -e "${GREEN}✓${NC} local.properties exists"

    # Check for required keys
    if grep -q "SUPABASE_URL" android/local.properties; then
        echo -e "${GREEN}  ✓${NC} SUPABASE_URL configured"
    else
        echo -e "${RED}  ✗${NC} SUPABASE_URL missing"
        ERRORS=$((ERRORS+1))
    fi

    if grep -q "SUPABASE_ANON_KEY" android/local.properties; then
        echo -e "${GREEN}  ✓${NC} SUPABASE_ANON_KEY configured"
    else
        echo -e "${RED}  ✗${NC} SUPABASE_ANON_KEY missing"
        ERRORS=$((ERRORS+1))
    fi

    if grep -q "GOOGLE_WEB_CLIENT_ID" android/local.properties; then
        echo -e "${GREEN}  ✓${NC} GOOGLE_WEB_CLIENT_ID configured"
    else
        echo -e "${RED}  ✗${NC} GOOGLE_WEB_CLIENT_ID missing"
        ERRORS=$((ERRORS+1))
    fi

    if grep -q "CLAUDE_API_KEY" android/local.properties; then
        echo -e "${GREEN}  ✓${NC} CLAUDE_API_KEY configured"
    else
        echo -e "${RED}  ✗${NC} CLAUDE_API_KEY missing"
        ERRORS=$((ERRORS+1))
    fi
else
    echo -e "${RED}✗${NC} local.properties not found"
    echo "  Copy template: cp android/local.properties.example android/local.properties"
    echo "  Then edit with your API keys"
    echo "  See: docs/API_INTEGRATION.md"
    ERRORS=$((ERRORS+1))
fi
echo ""

# Check Gradle wrapper
echo "Checking Gradle wrapper..."
if [ -f "android/gradlew" ]; then
    if [ -x "android/gradlew" ]; then
        echo -e "${GREEN}✓${NC} gradlew executable"
    else
        echo -e "${YELLOW}⚠${NC} gradlew not executable"
        echo "  Fix: chmod +x android/gradlew"
        WARNINGS=$((WARNINGS+1))
    fi
else
    echo -e "${RED}✗${NC} gradlew not found"
    ERRORS=$((ERRORS+1))
fi
echo ""

# Check connected devices
echo "Checking Android devices..."
if command -v adb &> /dev/null; then
    DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
    if [ $DEVICES -gt 0 ]; then
        echo -e "${GREEN}✓${NC} $DEVICES Android device(s) connected"
        adb devices | grep "device$" | while read line; do
            echo "  - $line"
        done
    else
        echo -e "${YELLOW}⚠${NC} No Android devices connected"
        echo "  Connect device and enable USB debugging"
        WARNINGS=$((WARNINGS+1))
    fi
else
    echo -e "${YELLOW}⚠${NC} adb not found (install Android SDK platform-tools)"
    WARNINGS=$((WARNINGS+1))
fi
echo ""

# Summary
echo "=============================="
if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo ""
    echo "Ready to build:"
    echo "  cd android"
    echo "  ./gradlew build"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ $WARNINGS warning(s) - build may work${NC}"
    echo ""
    echo "You can try building:"
    echo "  cd android"
    echo "  ./gradlew build"
    exit 0
else
    echo -e "${RED}✗ $ERRORS error(s), $WARNINGS warning(s)${NC}"
    echo ""
    echo "Fix errors above before building."
    echo "See documentation:"
    echo "  - docs/JAVA_SETUP.md"
    echo "  - docs/API_INTEGRATION.md"
    echo "  - docs/BUILD_GUIDE.md"
    exit 1
fi
