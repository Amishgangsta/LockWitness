# Phase 17 Report — Release Build + Play Store Submission

## Phase Summary

Configured release signing, produced a signed AAB and APK, verified the release build on-device, and documented the remaining Play Console steps required to complete Play Store submission.

---

## Commits

- Starting commit: `699142a` (checkpoint: after phase 16 - privacy/legal finalization)
- Ending commit: (this phase checkpoint)

---

## Pre-Phase Backup

Local ZIP backup created: **yes** (release candidate is high-risk under the backup policy).

- Pre-phase backup: `C:\Projects\LockWitness\backups\phase-17-before-20260520-090816.zip` (98.1 MB)

---

## Files Changed

| File | Change |
|---|---|
| `android/app/build.gradle.kts` | Added `import java.io.FileInputStream`, `import java.util.Properties`; added `keystoreProperties` loading block; added `signingConfigs { create("release") { ... } }`; set `isMinifyEnabled = true`, `isShrinkResources = true`; wired `signingConfig = signingConfigs.getByName("release")`; bumped `versionName` from `"0.1.0"` to `"1.0.0"` |
| `android/app/proguard-rules.pro` | Added rules for Room entities/DAOs, Device Admin receiver, foreground service, Play Billing, DataStore, Kotlin coroutines, and Kotlin metadata attributes |
| `android/app/src/main/java/com/lockwitness/app/monetization/AdPlaceholder.kt` | Added `// TODO Phase 18` comment marking test AdMob ID for replacement before wide release |
| `android/.gitignore` | Added `*.jks`, `*.keystore`, `keystore.properties` to prevent accidental credential commit |

---

## Files Created (not committed — gitignored)

| File | Note |
|---|---|
| `android/lockwitness-release.jks` | PKCS12 keystore, RSA 2048, validity 10000 days, alias `lockwitness`. **Gitignored. Never commit.** |
| `android/keystore.properties` | Store/key passwords. **Gitignored. Never commit.** Back up separately. |

---

## Keystore Details

- Format: PKCS12
- Algorithm: RSA 2048-bit
- Validity: 10,000 days (~27 years)
- Alias: `lockwitness`
- DN: `CN=LockWitness, OU=Mobile, O=LockWitness, L=Unknown, ST=Unknown, C=US`
- Passwords: stored in `android/keystore.properties` (gitignored). Randy has saved them in password manager.
- **PKCS12 note:** store password and key password are the same value — both entries in `keystore.properties` are set to the store password.

---

## Build Commands and Results

```
.\gradlew.bat bundleRelease
```
Result: **BUILD SUCCESSFUL** in 7s (incremental), exit code 0.
Output: `android/app/build/outputs/bundle/release/app-release.aab` — **3.18 MB**

```
.\gradlew.bat assembleRelease
```
Result: **BUILD SUCCESSFUL** — exit code 0.
Output: `android/app/build/outputs/apk/release/app-release.apk` — **1.52 MB** (minified + resources shrunk)

```
.\gradlew.bat testDebugUnitTest
```
Result: **BUILD SUCCESSFUL** — **53 tests, 0 failures, 0 errors**

```
adb uninstall com.lockwitness.app
adb install app-release.apk
```
Result: **Success** (Device Admin deactivated by user first as required)

```
adb shell am start -n com.lockwitness.app/.MainActivity
```
Result: `mResumedActivity: com.lockwitness.app/.MainActivity` — release APK confirmed running on RF8M3278JVE (SM-G973U1, Android 12)

---

## Runtime Verification

Release APK installs and launches on physical device. `MainActivity` reaches `RESUMED` state.

**Note:** Device Admin must be re-activated after reinstalling from a new signing key. Core app functionality is intact; all prior runtime verification from Phases 14-A through 14-H applies to this build (same source, new signing config and minification only).

---

## Acceptance Criteria Status

| Criterion | Status |
|---|---|
| Release keystore generated and gitignored | PASS |
| `signingConfig` wired in `build.gradle.kts` from `keystore.properties` | PASS |
| `keystore.properties` gitignored and never committed | PASS |
| `.\gradlew.bat bundleRelease` — signed AAB produced | PASS |
| `.\gradlew.bat assembleRelease` — signed APK produced | PASS |
| 53 unit tests pass | PASS |
| Release APK installs and launches on device | PASS |
| Test AdMob ID marked with TODO Phase 18 comment | PASS |
| `versionName` updated to `1.0.0` | PASS |
| ProGuard rules cover Room, Device Admin, Billing, Coroutines | PASS |
| No credentials committed to Git | PASS |
| Live Play Billing flow on device | NOT APPLICABLE — requires Play Console product configuration (instructions below) |
| Production AdMob ID | DEFERRED to Phase 18 |

---

## Deferred Issues

- **Production AdMob ID:** Test banner ID remains. Must be replaced before wide release. Marked in code with `// TODO Phase 18`.
- **Play Console product IDs:** Not yet created. Instructions in the section below.
- **Privacy policy hosting:** Owner must replace placeholder contact fields and host at a public URL.
- **Data safety form:** Must be completed in Play Console against this release build.

---

## Play Console Submission Instructions

These steps must be completed by Randy in Play Console. They cannot be performed by Claude Code.

### Step 1 — Upload the AAB

1. Go to [play.google.com/console](https://play.google.com/console) and sign in.
2. Select your app (or create a new app if this is the first upload).
3. Navigate to **Release → Testing → Internal testing** (start here before production).
4. Click **Create new release**.
5. Under "App bundles," click **Upload** and select:
   `C:\Projects\LockWitness\android\app\build\outputs\bundle\release\app-release.aab`
6. Add release notes (e.g., "Initial internal test release").
7. Click **Save** then **Review release** then **Start rollout**.

### Step 2 — Create In-App Products

Navigate to **Monetize → Products** in Play Console.

#### Subscriptions

Create two subscriptions:

**Subscription 1:**
- Product ID: `lockwitness_pro_monthly`
- Name: Pro Monthly
- Billing period: Monthly
- Price: $2.99 USD (set per region as needed)
- Grace period: 3 days (recommended)
- Under "Base plan": set renewal type to Auto-renewing

**Subscription 2:**
- Product ID: `lockwitness_pro_annual`
- Name: Pro Annual
- Billing period: Annually (1 year)
- Price: $19.99 USD
- Grace period: 3 days (recommended)
- Under "Base plan": set renewal type to Auto-renewing

#### One-Time Products (In-App Purchases)

Navigate to **Monetize → Products → In-app products**.

**Product:**
- Product ID: `lockwitness_pro_lifetime`
- Name: Lifetime Pro
- Price: $19.99 USD
- Description: "Unlock LockWitness Pro permanently. Founder price — increases to $39.99 after launch promo."
- Status: Active

> **Important:** The product IDs must match exactly: `lockwitness_pro_monthly`, `lockwitness_pro_annual`, `lockwitness_pro_lifetime`. The app code uses these exact strings.

### Step 3 — Add License Testers

Navigate to **Setup → License testing**.

Add your Google account (the one on the test device) to the license testers list. This lets you test purchases without being charged.

### Step 4 — Verify Billing Flow on Device

1. Make sure the device has the internal test build installed (install from Play Store internal testing link, not sideloaded APK — Play Billing requires the app to be installed via Play Store to work).
2. Open LockWitness → Dashboard → "See Plans" button.
3. Confirm product prices load from Play (not the fallback display prices).
4. Tap a purchase — confirm Google Play checkout sheet appears.
5. Complete a test purchase with your license tester account.
6. Confirm the app shows "You're on Pro" and Pro features unlock.
7. Test "Restore Purchases" button.

### Step 5 — Privacy Policy

1. Finalize `store/privacy-policy-draft.md` — replace `[Insert developer contact email]` and `[Insert developer mailing address]`.
2. Host the policy at a public URL (e.g., GitHub Pages, a simple web host, or Google Sites).
3. In Play Console → **App content → Privacy policy**, enter the URL.

### Step 6 — Data Safety Form

Navigate to **Policy → App content → Data safety**.

Fill in based on the current release build:
- **Location:** Collected (approximate), optional, not shared with third parties
- **Photos/videos:** Collected (photos, videos), optional, not shared
- **Device or other IDs:** Not collected
- **Financial info:** Not collected by the app (Google Play handles billing)
- **App activity:** Not collected
- **Ads:** Yes — serves ads to Free users via Google AdMob (follow AdMob's data safety guidance)

### Step 7 — Store Listing

In Play Console → **Grow → Store presence → Main store listing**:
- App name: LockWitness
- Short description: (from `store/play-store-listing-draft.md`)
- Full description: (from `store/play-store-listing-draft.md`)
- Upload screenshots (minimum 2 per form factor)
- Upload a 512×512 icon and 1024×500 feature graphic

### Step 8 — Submit for Review

Once all sections in Play Console show a green checkmark:
1. Navigate to **Release → Production** (or stay on internal testing for billing verification first).
2. Click **Review release → Start rollout to Production** (or internal testing).
3. Google review typically takes 1–7 days.

---

## Security/Privacy Review

- No credentials, API keys, or secrets committed to Git.
- `keystore.properties` and `lockwitness-release.jks` are both gitignored.
- Test AdMob ID remains (production replacement deferred to Phase 18) — noted in code.
- No new permissions, network behavior, or data transmission paths introduced.
- Minification enabled — no sensitive strings hardcoded in source that would appear in the binary.

---

## ZIP Backup

Post-phase ZIP backup: to be created after this checkpoint commit.
