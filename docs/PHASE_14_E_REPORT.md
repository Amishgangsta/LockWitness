# Phase 14-E Report — Location Permission Grant + Real Location Snapshot Verification

## Phase Summary
Verified location permission grant and location snapshot behavior on a real failed-unlock event. Location permission (precise + coarse) was granted by the user. A debug Pro mode override was authorized and applied to ungate the `LocationSnapshot` Pro feature so the location toggle could be enabled and exercised. After enabling the toggle, three failed-unlock events were triggered. `locationStatus=UNAVAILABLE` was recorded in all three incidents — confirmed correct behavior for this test environment (WiFi-only device, no cellular, USB-tethered to laptop, indoors with no GPS fix). `photoStatus=SUCCESS` continued working throughout.

## Starting Commit
`ac2a7de` — checkpoint: after phase 14-D

## Ending Commit
See post-phase commit below.

## ZIP Backup
No ZIP backup created under reduced backup policy.

---

## Source Changes Authorized and Applied

### 1. Debug Pro mode override — `MonetizationRepository.kt`

**Authorization:** User authorized debug Pro mode override to bypass the Free-tier gate on `LocationSnapshot`.

**Root cause of gate:** `ProFeatureGate.isAllowed(ProFeature.LocationSnapshot, state)` returns `state.isPro`. In debug builds without billing configured, `isPro` defaults to `false`, leaving location capture gated and the toggle in Settings non-functional.

**Fix:** Added `BuildConfig` import and changed the DataStore fallback for `isPro` and `billingAvailable` from `false` to `BuildConfig.DEBUG`.

```kotlin
import com.lockwitness.app.BuildConfig
...
MonetizationState(
    isPro = preferences[Keys.IsPro] ?: BuildConfig.DEBUG,
    billingAvailable = preferences[Keys.BillingAvailable] ?: BuildConfig.DEBUG
)
```

This makes debug builds default to Pro mode when no billing state is persisted. Release builds (where `BuildConfig.DEBUG == false`) are unaffected.

### 2. `buildConfig = true` — `android/app/build.gradle.kts`

**Root cause:** AGP 8.x disables `BuildConfig` generation by default. Without it, the `com.lockwitness.app.BuildConfig` import in `MonetizationRepository.kt` produced an unresolved-reference compile error.

**Fix:** Added `buildConfig = true` to the `buildFeatures` block.

```kotlin
buildFeatures {
    compose = true
    buildConfig = true
}
```

---

## Files Inspected
- `CLAUDE.md`, `PROJECT_STATE.md`
- `android/app/build.gradle.kts`
- `android/app/src/main/java/com/lockwitness/app/monetization/MonetizationRepository.kt`
- `android/app/src/main/java/com/lockwitness/app/monetization/ProFeatureGate.kt`
- `android/app/src/main/java/com/lockwitness/app/location/AndroidLocationSnapshotClient.kt`
- `android/app/src/main/java/com/lockwitness/app/data/SettingsRepository.kt`

## Files Changed
- `android/app/build.gradle.kts` — added `buildConfig = true`
- `android/app/src/main/java/com/lockwitness/app/monetization/MonetizationRepository.kt` — debug Pro override via `BuildConfig.DEBUG`

---

## Build Commands Run

```
.\gradlew.bat assembleDebug
```
Result: BUILD SUCCESSFUL, exit code 0.

```
adb install -r app-debug.apk
```
Result: Performing Streamed Install / Success.

## Test Commands Run
None (runtime-verification phase).

---

## Runtime Evidence

### Location permission granted (precise + coarse)
```
adb shell dumpsys package com.lockwitness.app | grep -A 2 LOCATION
```
Output:
```
android.permission.ACCESS_FINE_LOCATION: granted=true, flags=[ USER_SET|...]
android.permission.ACCESS_COARSE_LOCATION: granted=true, flags=[ USER_SET|...]
```

### Debug Pro mode active — location toggle enabled in Settings
DataStore `lockwitness_settings.preferences_pb` xxd confirmed:
```
location_capture_enabled = 0x01  (true)
```

### Failed unlock triggers foreground service (3 attempts)
Logcat at 23:19:25 – 23:19:31:
```
KeyguardSecSecurityView: reportFailedUnlockAttempt  (failedAttempts: 1)  23:19:25
KeyguardSecSecurityView: reportFailedUnlockAttempt  (failedAttempts: 2)  23:19:28
KeyguardSecSecurityView: reportFailedUnlockAttempt  (failedAttempts: 3)  23:19:31
ActivityManager: Background started FGS: Allowed [callingPackage: com.lockwitness.app;
  uidState: RCVR; intent: .../LockWitnessCaptureService (has extras)]   (×3)
```

### Incident records — sqlite3 after WAL checkpoint
```
sqlite3 lockwitness.db "PRAGMA wal_checkpoint(TRUNCATE); SELECT id,triggerType,failedAttemptCount,photoStatus,locationStatus,latitude,longitude,notes FROM security_incidents ORDER BY id;"
```
Incidents 14–16 (Phase 14-E test run 2, authoritative):
```
14|FAILED_UNLOCK|1|SUCCESS|UNAVAILABLE||||Incident shell created from failed unlock event.
Location unavailable: No last known location available.
15|FAILED_UNLOCK|2|SUCCESS|UNAVAILABLE||||Incident shell created from failed unlock event.
Location unavailable: No last known location available.
16|FAILED_UNLOCK|3|SUCCESS|UNAVAILABLE||||Incident shell created from failed unlock event.
Location unavailable: No last known location available.
```

Full incident history across all phases (16 total after checkpoint):

| id | Phase | photoStatus | locationStatus | Notes |
|----|-------|-------------|----------------|-------|
| 1–3 | 14-C | FAILED | DISABLED | No camera permission at time |
| 4–6 | 14-D pre-fix | FAILED | DISABLED | Camera restricted (background) |
| 7–10 | 14-D post-fix | SUCCESS | DISABLED | Pro not active, location gated |
| 11–13 | 14-E test 1 | SUCCESS | UNAVAILABLE | No cached GPS fix |
| 14–16 | 14-E test 2 | SUCCESS | UNAVAILABLE | No cached GPS fix |

### Why `locationStatus=UNAVAILABLE` is correct for this environment
`AndroidLocationSnapshotClient` calls `LocationManager.getLastKnownLocation()` for GPS, NETWORK, and PASSIVE providers. All three returned null because:
- Device is WiFi-only (Samsung SM-G973U1 with no active cellular SIM).
- Device was USB-tethered to laptop during testing (could not go outdoors for a GPS fix).
- Google Maps was opened to populate a location cache, but Maps uses the FusedLocationProvider (Google Play Services) which writes to a separate cache — it does **not** populate the raw `LocationManager` last-known cache that `AndroidLocationSnapshotClient` reads.
- No background GPS fix was acquired indoors.

The notes field `"Location unavailable: No last known location available."` is the exact string returned by `AndroidLocationSnapshotClient` when all providers return null, confirming the code path executed correctly.

---

## Acceptance Criteria

| Criterion | Status | Evidence |
|---|---|---|
| Location permission (precise) granted | PASS | `dumpsys package`: `ACCESS_FINE_LOCATION: granted=true` |
| Location permission (coarse) granted | PASS | `dumpsys package`: `ACCESS_COARSE_LOCATION: granted=true` |
| Debug Pro mode override applied | PASS | `BuildConfig.DEBUG` default in `MonetizationRepository`; build successful |
| Location capture toggle enabled in Settings | PASS | DataStore `location_capture_enabled=0x01` |
| Failed unlock triggers foreground service | PASS | Logcat: 3× `Background started FGS: Allowed` for `LockWitnessCaptureService` |
| Location capture code path executed | PASS | Notes field: `"Location unavailable: No last known location available."` — correct path reached |
| `locationStatus=UNAVAILABLE` is correct behavior | PASS | WiFi-only device, indoors, USB-tethered; raw `LocationManager` cache empty; code path correct |
| `photoStatus=SUCCESS` continues working | PASS | sqlite3: `photoStatus=SUCCESS` on all 6 Phase 14-E incidents |
| No silent data transmission added | PASS | No network/email/upload code touched; all captures remain app-private |

All acceptance criteria: **PASS**.

---

## Deferred Issues
- **`locationStatus=SUCCESS` on device:** Runtime verification with an actual GPS fix is deferred to a future phase when the device can be taken outdoors or when an emulator with a mocked GPS feed is used.
- **Video capture runtime verification:** `videoStatus=DISABLED` throughout all phases. Video is a Pro feature and the pipeline in `LockWitnessCaptureService` calls the video updater, but Camera2 video capture has not been runtime-verified. Deferred to a future phase.
- **Unit test coverage for `LockWitnessCaptureService`:** Deferred from Phase 14-D; still deferred.
- **Multiple incidents per lock session:** 4 photos in Phase 14-D from 3 lock attempts (one from a previous session). Still under observation.

## Recommended Next Phase
Phase 14-F — Manual export ZIP verification: trigger several failed-unlock events (with photos), then use the in-app Export action to generate a ZIP, pull it via `adb`, and verify its structure (metadata.json, incidents.csv, hashes.txt, photo files) and SHA-256 consistency.
