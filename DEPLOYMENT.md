# SkyLens Deployment Guide

Complete deployment instructions for production.

## 🚀 Backend Deployment (Fly.io)

### 1. Prerequisites

```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Login to Fly
fly auth login
```

### 2. Create Postgres Database

```bash
# Create Postgres app
fly postgres create --name skylens-db --region iad

# Get connection string
fly postgres connect -a skylens-db
```

### 3. Enable PostGIS Extension

```sql
-- Connect to database and run:
CREATE EXTENSION IF NOT EXISTS postgis;
```

### 4. Deploy Backend

```bash
cd backend

# Create app
fly launch --name skylens-backend --region iad

# Set secrets
fly secrets set DATABASE_URL="postgresql://..." \
  CLAUDE_API_KEY="sk-ant-..." \
  R2_ACCESS_KEY_ID="..." \
  R2_SECRET_ACCESS_KEY="..." \
  R2_BUCKET_NAME="skylens-packs"

# Deploy
fly deploy

# Check status
fly status
fly logs
```

### 5. Import Data

```bash
# SSH into Fly machine
fly ssh console

# Or run locally against production DB
python3 scripts/import_airports.py airports.dat $DATABASE_URL
python3 scripts/import_landmarks.py $DATABASE_URL
```

## 📱 Android App Deployment

### 1. Configure Build

Update `android/app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "SUPABASE_URL", "\"https://your-project.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key\"")
        buildConfigField("String", "BACKEND_API_URL", "\"https://skylens-backend.fly.dev\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"your-client-id.apps.googleusercontent.com\"")
    }

    signingConfigs {
        release {
            storeFile file("../keystore.jks")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias "skylens"
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 2. Generate Keystore

```bash
cd android

keytool -genkey -v -keystore keystore.jks \
  -alias skylens \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Store passwords securely!
export KEYSTORE_PASSWORD="your-password"
export KEY_PASSWORD="your-key-password"
```

### 3. Build Release APK/AAB

```bash
# Build Android App Bundle
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab

# Or build APK
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### 4. Play Store Submission

1. **Create Play Store Listing**
   - Go to https://play.google.com/console
   - Create new app
   - Fill in store listing details

2. **Upload AAB**
   - Internal testing track first
   - Upload `app-release.aab`

3. **Complete Data Safety Form**
   - Location data: Collected, required, not shared
   - User account info: Optional, for cloud sync
   - Declare Claude API usage

4. **Add Privacy Policy**
   - Host at https://skylens.app/privacy
   - Link in Play Store listing

5. **Submit for Review**
   - Target internal testing first
   - Then production after testing

## 🔒 Security Checklist

### API Keys
- [x] Never commit API keys to git
- [x] Use BuildConfig for Android secrets
- [x] Use Fly secrets for backend
- [x] Rotate keys regularly

### Database
- [x] Use connection pooling
- [x] Enable SSL for database connections
- [x] Use prepared statements (SQLx does this)
- [x] Regular backups

### HTTPS
- [x] Enforce HTTPS on all endpoints
- [x] Use CORS properly
- [x] Validate all inputs

## 📊 Monitoring

### Backend Monitoring

```bash
# View logs
fly logs -a skylens-backend

# Check metrics
fly status -a skylens-backend

# Scale instances
fly scale count 2 -a skylens-backend

# Scale resources
fly scale vm shared-cpu-1x -a skylens-backend
fly scale memory 512 -a skylens-backend
```

### Database Monitoring

```bash
# Check database status
fly status -a skylens-db

# View database metrics
fly postgres db list -a skylens-db

# Create backup
fly postgres backup create -a skylens-db
```

### Android Monitoring

Use Firebase Performance Monitoring:

```kotlin
// Add to app/build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-perf:20.5.0")
}

// Initialize in Application class
FirebasePerformance.getInstance()
```

## 💾 Backup Strategy

### Database Backups

```bash
# Automated daily backups (Fly.io)
fly postgres backup list -a skylens-db

# Manual backup
pg_dump $DATABASE_URL > backup-$(date +%Y%m%d).sql

# Restore from backup
psql $DATABASE_URL < backup-20260324.sql
```

### Cloudflare R2 Backups

```bash
# R2 buckets have versioning enabled
# Configure lifecycle rules in Cloudflare dashboard
```

## 🔄 CI/CD Pipeline

### GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Fly
        uses: superfly/flyctl-actions/setup-flyctl@master

      - name: Deploy to Fly
        run: flyctl deploy --remote-only
        working-directory: ./backend
        env:
          FLY_API_TOKEN: ${{ secrets.FLY_API_TOKEN }}

  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build AAB
        working-directory: ./android
        run: ./gradlew bundleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}

      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_STORE_JSON }}
          packageName: com.skylens.app
          releaseFiles: android/app/build/outputs/bundle/release/app-release.aab
          track: internal
```

## 📈 Scaling

### Current Setup (MVP)
- Backend: 1 shared-cpu-1x instance (256MB RAM)
- Database: Postgres free tier
- Cost: ~$7/month

### Scale to 100 users
- Backend: 2 shared-cpu-2x instances (512MB RAM each)
- Database: Postgres Hobby tier
- Cost: ~$30/month

### Scale to 1000 users
- Backend: 4 dedicated-cpu-1x instances (2GB RAM each)
- Database: Postgres Standard tier with read replicas
- Cost: ~$200/month

## 🐛 Troubleshooting

### Backend won't start
```bash
# Check logs
fly logs -a skylens-backend

# Common issues:
# 1. DATABASE_URL not set → fly secrets set DATABASE_URL=...
# 2. Migrations failed → fly ssh console, run migrations manually
# 3. Port binding issue → Check main.rs uses PORT env var
```

### Database connection issues
```bash
# Test connection
fly postgres connect -a skylens-db

# Check connection string format
# Should be: postgresql://user:pass@host:port/dbname?sslmode=require
```

### Android build failures
```bash
# Clean build
./gradlew clean build

# Check Java version
java -version  # Must be 17+

# Verify dependencies
./gradlew dependencies
```

## 📞 Support

- Backend Issues: Check Fly.io status page
- Android Issues: Check Google Play Console
- Database Issues: Check Supabase/Neon dashboard

---

**Last Updated**: 2026-03-24
