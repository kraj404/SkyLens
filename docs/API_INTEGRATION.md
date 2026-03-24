# 🔌 API Integration Guide

## Overview

SkyLens integrates with three key external services:
1. **Supabase** - Authentication, database, storage
2. **Claude API** - AI-powered landmark stories and narration
3. **Google OAuth** - User authentication

---

## 1. Supabase Setup

### Create Project

1. Go to [supabase.com](https://supabase.com)
2. Click "New Project"
3. Set project name: `skylens`
4. Choose region closest to target users
5. Set strong database password
6. Wait for project initialization (~2 minutes)

### Get API Credentials

From Supabase Dashboard → Settings → API:

```properties
# Required for Android app
SUPABASE_URL=https://xxxxxxxxxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Add these to [local.properties](../android/local.properties.example):
```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
```

### Run Database Migrations

Navigate to Supabase Dashboard → SQL Editor and execute each migration file in order:

1. [001_create_users.sql](migrations/001_create_users.sql)
2. [002_create_airports.sql](migrations/002_create_airports.sql)
3. [003_create_landmarks.sql](migrations/003_create_landmarks.sql)
4. [004_create_trips.sql](migrations/004_create_trips.sql)

Verify tables created:
```sql
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public';
```

Expected output:
- users
- airports
- landmarks
- trips
- trip_events

### Enable Google Authentication

1. Dashboard → Authentication → Providers
2. Find "Google" provider
3. Toggle enabled
4. Keep this tab open - you'll need the Redirect URL

---

## 2. Google OAuth Setup

### Create Google Cloud Project

1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create new project: "SkyLens"
3. Enable APIs:
   - Google+ API (for user info)

### Configure OAuth Consent Screen

1. APIs & Services → OAuth consent screen
2. Choose "External" (unless you have Workspace)
3. Fill required fields:
   - **App name:** SkyLens
   - **User support email:** your-email@gmail.com
   - **Developer contact:** your-email@gmail.com
4. Add scopes:
   - `openid`
   - `email`
   - `profile`
5. Add test users (during development)
6. Save and continue

### Create OAuth Credentials

#### Android Client ID

1. APIs & Services → Credentials → Create Credentials
2. Choose "OAuth 2.0 Client ID"
3. Application type: **Android**
4. Package name: `com.skylens`
5. Get SHA-1 fingerprint:

```bash
# Debug keystore (for development)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copy the SHA-1 fingerprint and paste into Google Console.

6. Click "Create"
7. Note the Client ID (not needed in code, but save for reference)

#### Web Client ID

1. Create another credential: OAuth 2.0 Client ID
2. Application type: **Web application**
3. Name: "SkyLens Web Client"
4. Authorized redirect URIs:
   - Add the Supabase callback URL from earlier
   - Format: `https://xxxxxxxxxxxxx.supabase.co/auth/v1/callback`
5. Click "Create"
6. **Copy the Client ID** - this goes in your app

Add to [local.properties](../android/local.properties.example):
```properties
GOOGLE_WEB_CLIENT_ID=123456789-abc123def456.apps.googleusercontent.com
```

### Configure Supabase Google Auth

Back in Supabase Dashboard → Authentication → Providers → Google:

1. Enable the provider
2. Paste the **Web Client ID** from Google Console
3. Paste the **Web Client Secret** from Google Console
4. Click Save

---

## 3. Claude API Setup

### Get API Key

1. Go to [console.anthropic.com](https://console.anthropic.com)
2. Sign up or log in
3. Navigate to API Keys
4. Click "Create Key"
5. Copy the key (starts with `sk-ant-api03-...`)
6. **Important:** Save it securely - you can't view it again

Add to [local.properties](../android/local.properties.example):
```properties
CLAUDE_API_KEY=sk-ant-api03-your-key-here
```

### Verify API Access

Test with curl:
```bash
curl https://api.anthropic.com/v1/messages \
  -H "content-type: application/json" \
  -H "x-api-key: $CLAUDE_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-haiku-4.5-20251001",
    "max_tokens": 1024,
    "messages": [
      {"role": "user", "content": "Hello, Claude!"}
    ]
  }'
```

Expected response:
```json
{
  "id": "msg_...",
  "type": "message",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "Hello! How can I help you today?"
    }
  ],
  "model": "claude-haiku-4.5-20251001",
  "stop_reason": "end_turn",
  "usage": {
    "input_tokens": 10,
    "output_tokens": 12
  }
}
```

### Cost Estimation

For 10 users during MVP:

**Pre-caching landmark stories** (one-time):
- 1000 landmarks × 500 tokens avg = 500,000 output tokens
- Haiku 4.5: $1.25 per million output tokens
- **Cost: ~$0.63 one-time**

**Real-time features** (per user per flight):
- Flight narrator: 10 narrations × 300 tokens = 3,000 tokens
- Q&A: 5 questions × 400 tokens = 2,000 tokens
- Trip summary: 1 × 600 tokens = 600 tokens
- **Total per flight: ~5,600 output tokens = $0.007 per flight**

**Monthly estimate (10 users, 2 flights each):**
- 20 flights × $0.007 = **$0.14/month**

**Total MVP cost: $0.63 setup + $0.14/month ongoing**

---

## 4. Complete Configuration Example

### local.properties

Create `android/local.properties` with all credentials:

```properties
# Supabase Configuration
SUPABASE_URL=https://abcdefghijk.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFiY2RlZmdoaWprIiwicm9sZSI6ImFub24iLCJpYXQiOjE2ODAwMDAwMDAsImV4cCI6MTk5NTU3NjAwMH0.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Google OAuth
GOOGLE_WEB_CLIENT_ID=123456789-abc123def456ghi789jkl012.apps.googleusercontent.com

# Claude API
CLAUDE_API_KEY=sk-ant-api03-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Constants.kt

The app reads these values from BuildConfig:

```kotlin
// android/app/src/main/java/com/skylens/util/Constants.kt
object Constants {
    // Read from BuildConfig (configured in build.gradle.kts)
    val SUPABASE_URL = BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
    val GOOGLE_WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val CLAUDE_API_KEY = BuildConfig.CLAUDE_API_KEY

    // Other constants...
}
```

### build.gradle.kts

The build file loads from local.properties:

```kotlin
// android/app/build.gradle.kts
android {
    defaultConfig {
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        buildConfigField("String", "SUPABASE_URL",
            "\"${properties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",
            "\"${properties.getProperty("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID",
            "\"${properties.getProperty("GOOGLE_WEB_CLIENT_ID")}\"")
        buildConfigField("String", "CLAUDE_API_KEY",
            "\"${properties.getProperty("CLAUDE_API_KEY")}\"")
    }
}
```

---

## 5. Security Best Practices

### Never Commit Secrets

Add to `.gitignore`:
```gitignore
# Local configuration
local.properties
*.keystore
*.jks

# Supabase service role key (if you have one)
.env
.env.local
```

### Use Environment Variables for CI/CD

For GitHub Actions or other CI:

```yaml
# .github/workflows/build.yml
env:
  SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
  SUPABASE_ANON_KEY: ${{ secrets.SUPABASE_ANON_KEY }}
  GOOGLE_WEB_CLIENT_ID: ${{ secrets.GOOGLE_WEB_CLIENT_ID }}
  CLAUDE_API_KEY: ${{ secrets.CLAUDE_API_KEY }}
```

### Rotate Keys Regularly

- Claude API keys: Rotate every 90 days
- Supabase: Monitor usage, rotate if suspicious activity
- Google OAuth: Only rotate if compromised

---

## 6. Testing Integration

### Test Authentication Flow

```kotlin
// Manual test in Android Studio Logcat
class AuthTest {
    @Test
    fun testGoogleSignIn() = runBlocking {
        val signInManager = GoogleSignInManager(context)
        val result = signInManager.signIn()
        assert(result.isSuccess)
    }
}
```

### Test Claude API

```kotlin
class ClaudeApiTest {
    @Test
    fun testGenerateStory() = runBlocking {
        val client = ClaudeApiClient()
        val result = client.generateLandmarkStory(
            landmarkName = "Mount Fuji",
            landmarkType = "MOUNTAIN",
            elevation = 3776,
            country = "Japan"
        )
        assert(result.isSuccess)
        println(result.getOrNull())
    }
}
```

### Test Supabase Connection

```kotlin
class SupabaseTest {
    @Test
    fun testFetchAirports() = runBlocking {
        val repository = AirportRepository(dao, supabase)
        val airports = repository.searchAirports("Tokyo")
        assert(airports.isNotEmpty())
    }
}
```

---

## 7. Monitoring & Debugging

### Supabase Logs

Dashboard → Logs → API:
- Monitor auth requests
- Check database query performance
- Track failed requests

### Claude API Usage

Console → Usage:
- Monitor token consumption
- Track costs
- Set spending limits

### Android Logcat

Filter by tag:
```bash
adb logcat -s SkyLens:* Supabase:* Claude:*
```

Key log messages to watch:
- `Auth: Sign-in successful`
- `Claude: Generated story in 1.2s`
- `Supabase: Fetched 47 landmarks`
- `Location: GPS accuracy: 10m`

---

## 8. Troubleshooting

### "Invalid API Key" (Claude)

- Verify key starts with `sk-ant-api03-`
- Check for extra spaces or line breaks
- Regenerate key if needed

### "Invalid OAuth client" (Google)

- Verify package name is exactly `com.skylens`
- Ensure SHA-1 fingerprint matches your keystore
- Wait 5 minutes after adding client ID (propagation delay)

### "CORS error" (Supabase)

- Ensure using `https://` not `http://`
- Check Supabase URL ends with `.supabase.co`
- Verify anon key is for correct project

### "Table does not exist"

- Run migrations in correct order
- Check Supabase dashboard → Table Editor
- Verify PostGIS extension enabled:
  ```sql
  CREATE EXTENSION IF NOT EXISTS postgis;
  ```

---

## 9. Production Checklist

Before launching:

- [ ] All API keys configured in production environment
- [ ] Google OAuth consent screen verified
- [ ] Supabase project upgraded to paid tier (if needed)
- [ ] Claude API spending limit set
- [ ] Row Level Security policies tested
- [ ] API keys rotated from development values
- [ ] Monitoring alerts configured
- [ ] Backup strategy for Supabase database
- [ ] Privacy policy updated with third-party services

---

## 10. Support Resources

### Supabase
- [Documentation](https://supabase.com/docs)
- [Discord Community](https://discord.supabase.com)
- Support: support@supabase.io

### Claude API
- [Documentation](https://docs.anthropic.com)
- [API Reference](https://docs.anthropic.com/claude/reference)
- Support: support@anthropic.com

### Google OAuth
- [Android Integration Guide](https://developers.google.com/identity/sign-in/android)
- [OAuth 2.0 Docs](https://developers.google.com/identity/protocols/oauth2)
- [Console](https://console.cloud.google.com)

---

**Next Steps:** [Build the app](BUILD_GUIDE.md)
