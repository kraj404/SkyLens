# ⚠️ Java Development Kit Required

The Android build requires JDK 17 or higher to be installed.

## Quick Install (macOS)

### Option 1: Homebrew (Recommended)
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install OpenJDK 17
brew install openjdk@17

# Link it for system Java wrappers
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Add to shell profile (for zsh)
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Verify installation
java -version
```

### Option 2: Official Oracle JDK
1. Download from: https://www.oracle.com/java/technologies/downloads/
2. Choose "macOS" → "ARM64 DMG Installer" (for M1/M2 Mac) or "x64 DMG Installer" (Intel Mac)
3. Install the downloaded .dmg file
4. Verify: `java -version`

### Option 3: Azul Zulu (Alternative)
```bash
brew install --cask zulu17
```

## Verify Installation

After installing, verify:
```bash
java -version
# Expected output:
# openjdk version "17.0.x"
```

Check JAVA_HOME:
```bash
echo $JAVA_HOME
# Expected: /opt/homebrew/opt/openjdk@17 or similar

# If not set:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc
```

## Next Steps

Once Java is installed, run:
```bash
cd /Users/I808883/app/claude/android
./gradlew build
```

## Troubleshooting

### "Unable to locate a Java Runtime"
- Ensure JDK 17+ is installed (not just JRE)
- Check JAVA_HOME is set correctly
- Restart terminal after installation

### "JAVA_HOME is set to an invalid directory"
```bash
# Find correct path
/usr/libexec/java_home -V

# Set it
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Multiple Java versions installed
```bash
# List all installed versions
/usr/libexec/java_home -V

# Select specific version
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

---

**Note:** The build will NOT work until Java 17+ is installed. This is a prerequisite for Android development.
