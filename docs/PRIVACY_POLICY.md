# SkyLens Privacy Policy

**Last Updated:** March 23, 2026

## Introduction

SkyLens ("we", "our", "the app") is committed to protecting your privacy. This policy explains how we collect, use, and protect your personal information.

## Information We Collect

### 1. Location Data
- **What:** GPS coordinates during flight tracking
- **Why:** To identify landmarks visible from your airplane window
- **Storage:** Stored locally on your device and optionally in our secure cloud database (if signed in)
- **Retention:** Trip history is kept indefinitely unless you delete it

### 2. Google Account Information
- **What:** Email address, name, profile photo
- **Why:** Authentication and personalized experience
- **Storage:** Supabase secure database (encrypted at rest)
- **Retention:** Until you delete your account

### 3. Device Information
- **What:** Device model, OS version, app version
- **Why:** Bug fixing and performance optimization
- **Storage:** Anonymous analytics only
- **Retention:** 90 days

## How We Use Your Information

1. **Landmark Identification:** Your GPS location is used to query our database for nearby landmarks
2. **AI Story Generation:** Landmark data (not your personal location) is sent to Claude AI (Anthropic) to generate educational stories
3. **Trip History:** Your flight paths and landmarks seen are saved for your personal review
4. **Service Improvement:** Anonymous usage data helps us improve the app

## Data Sharing

We share data with these third-party services:

1. **Google (Authentication only)**
   - Purpose: Google Sign-In OAuth flow
   - Data: Email, name, profile photo
   - Policy: [Google Privacy Policy](https://policies.google.com/privacy)

2. **Supabase (Backend hosting)**
   - Purpose: Database and authentication infrastructure
   - Data: User account, trip history
   - Location: US-based servers (encrypted)
   - Policy: [Supabase Privacy Policy](https://supabase.com/privacy)

3. **Anthropic (AI story generation)**
   - Purpose: Generate landmark stories and commentary
   - Data: Landmark names and descriptions ONLY (not your personal location)
   - Policy: [Anthropic Privacy Policy](https://www.anthropic.com/legal/privacy)

4. **Cloudflare (CDN)**
   - Purpose: Deliver map tiles and images
   - Data: Anonymous HTTP requests
   - No logging of personal information

**We NEVER:**
- Sell your location data
- Share your trips with third parties
- Track you outside the app
- Use your data for advertising

## Data Security

- **Encryption in Transit:** All data uses HTTPS/TLS encryption
- **Encryption at Rest:** Android Keystore for tokens, encrypted database storage
- **Authentication:** Secure OAuth 2.0 flow with Google
- **Access Control:** Your data is only accessible to you when authenticated

## Your Rights

You have the right to:

1. **Access Your Data:** View all your trips and account information in the app
2. **Delete Your Data:** Delete individual trips or your entire account from Settings
3. **Export Your Data:** Request a copy of your data in GeoJSON format
4. **Opt Out:** Use the app without signing in (local-only mode)
5. **Revoke Access:** Disconnect Google Sign-In from your Google Account settings

## Children's Privacy

SkyLens is not directed to children under 13. We do not knowingly collect information from children.

## Location Permissions

**Why we need location permissions:**
- **ACCESS_FINE_LOCATION:** Track your precise aircraft position
- **ACCESS_BACKGROUND_LOCATION:** Continue tracking when app is minimized during flight

You can revoke these permissions at any time in Android Settings, but the app will not function without location access.

## Data Retention

- **Trip History:** Kept indefinitely unless you delete
- **Location Data:** Only stored as part of trip history
- **Authentication Tokens:** Valid for 1 hour, automatically refreshed
- **Account Deletion:** All your data is permanently deleted within 30 days

## Changes to This Policy

We may update this privacy policy from time to time. Changes will be posted on this page with an updated "Last Updated" date.

## Contact Us

For questions about this privacy policy or data practices:
- **Email:** privacy@skylens.app
- **GitHub Issues:** https://github.com/yourusername/skylens/issues

## Legal Compliance

This app complies with:
- **GDPR** (EU General Data Protection Regulation)
- **CCPA** (California Consumer Privacy Act)
- **Google Play Store** data safety requirements
- **Android** privacy best practices

---

**By using SkyLens, you agree to this privacy policy.**
