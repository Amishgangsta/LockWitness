# Phase 14-D Report — Camera Permission Grant + Real Photo Capture Verification

## Phase Summary
Verified real front-camera photo capture on a failed unlock event. A second source defect was discovered and fixed: `LockWitnessDeviceAdminReceiver` ran photo capture in a plain coroutine, which Android 12 immediately blocks from camera access (background camera restriction). Fix: moved the full capture pipeline into a `LockWitnessCaptureService` foreground service with `foregroundServiceType="camera"`. After the fix, four JPEG photo files (3.1–3.3 MB each) were written to app-private storage, and `photoStatus=SUCCESS` with SHA-256 was confirmed in the Room WAL.

## Starting Commit
`ec857d7` — checkpoint: after phase 14-C

## Ending Commit
See post-phase commit below.

## ZIP Backup
No ZIP backup created under reduced backup policy.

---

## Defect Found and Fixed

### Root Cause
`LockWitnessDeviceAdminReceiver.onPasswordFailed` launched a `CoroutineScope(SupervisorJob() + Dispatchers.IO)` directly. Android 12+ enforces a hard background camera restriction: non-foreground, non-foreground-service processes that open the camera are immediately disconnected.

**Evidence of defect (pre-fix logcat):**
```
CameraService: Camera 1: Opened. Client: com.lockwitness.app
CameraService: Camera 1: Access for "com.lockwitness.app" has been restricted,
               isUidActive true, isIndividualSensorPrivacyEnabled false
Camera3-Device: disconnectImpl: E
CameraService: Closed Camera 1. Client was: com.lockwitness.app
```

### Fix Applied (authorized by user)
1. Created `LockWitnessCaptureService` — a `Service` with `android:foregroundServiceType="camera"` that owns the full capture pipeline (incident shell creation + photo + video + location + alert).
2. Updated `AndroidManifest.xml` — added `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CAMERA`, `POST_NOTIFICATIONS` permissions and the service declaration.
3. Updated `LockWitnessDeviceAdminReceiver.onPasswordFailed` — replaced inline coroutine with `startForegroundService(Intent(...LockWitnessCaptureService...))`.

### Fix Verification
**Post-fix logcat:**
```
ActivityManager: Background started FGS: Allowed [callingPackage: com.lockwitness.app;
  uidState: RCVR; intent: Intent { cmp=com.lockwitness.app/.capture.LockWitnessCaptureService }]
CameraService: Camera 1: Opened. Client: com.lockwitness.app (PID 31169)
CameraManagerGlobal: Camera 1 CAMERA_STATE_OPEN  → com.lockwitness.app
CameraManagerGlobal: Camera 1 CAMERA_STATE_ACTIVE → com.lockwitness.app
CameraManagerGlobal: Camera 1 CAMERA_STATE_IDLE   → com.lockwitness.app
CameraManagerGlobal: Camera 1 CAMERA_STATE_CLOSED → com.lockwitness.app
```
No restriction. Full OPEN → ACTIVE → IDLE → CLOSED cycle confirmed.

---

## Files Inspected
- `CLAUDE.md`, `PROJECT_STATE.md`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/java/com/lockwitness/app/admin/LockWitnessDeviceAdminReceiver.kt`
- `android/app/src/main/java/com/lockwitness/app/photo/PhotoIncidentUpdater.kt`
- `android/app/src/main/java/com/lockwitness/app/photo/Camera2PhotoCaptureClient.kt`
- `android/app/src/main/java/com/lockwitness/app/photo/LocalPhotoStore.kt`

## Files Changed
- `android/app/src/main/AndroidManifest.xml` — added permissions + service declaration
- `android/app/src/main/java/com/lockwitness/app/admin/LockWitnessDeviceAdminReceiver.kt` — replaced coroutine with `startForegroundService`
- `android/app/src/main/java/com/lockwitness/app/capture/LockWitnessCaptureService.kt` — new foreground service
- `docs/PHASE_14_D_REPORT.md` (this file — new)
- `PROJECT_STATE.md` (phase record appended)

## Build Commands Run
```
.\gradlew.bat assembleDebug
```
Result: BUILD SUCCESSFUL in 43s, 37 actionable tasks: 11 executed, 26 up-to-date. Exit code 0.

```
adb install -r app-debug.apk
```
Result: Performing Streamed Install / Success.

## Test Commands Run
None (runtime-verification phase).

---

## Runtime Evidence

### Camera permission verified
```
adb shell dumpsys package com.lockwitness.app | grep -A 2 CAMERA
```
Output: `android.permission.CAMERA: granted=true, flags=[ USER_SET|...]`

### Failed unlock triggers foreground service (post-fix)
Logcat at 23:01:35 — 23:01:38:
```
KeyguardSecSecurityView: reportFailedUnlockAttempt  (failedAttempts: 1)
ActivityManager: Background started FGS: Allowed [callingPackage: com.lockwitness.app;
  uidState: RCVR; intent: .../LockWitnessCaptureService (has extras)]
Camera 1: Opened. Client: com.lockwitness.app (PID 31169, UID 10035)
Camera 1 CAMERA_STATE_OPEN  → CAMERA_STATE_ACTIVE → CAMERA_STATE_IDLE → CAMERA_STATE_CLOSED
finishCameraOps: Finish camera ops, package name = com.lockwitness.app
```

### Photo files written to app-private storage
```
adb shell run-as com.lockwitness.app ls -la files/incident_photos/
```
Output:
```
-rw------- 1 u0_a35 u0_a35 3221553 2026-05-19 23:01 incident_1779246097329.jpg
-rw------- 1 u0_a35 u0_a35 3254138 2026-05-19 23:01 incident_1779246098746.jpg
-rw------- 1 u0_a35 u0_a35 3232214 2026-05-19 23:01 incident_1779246105593.jpg
-rw------- 1 u0_a35 u0_a35 3343213 2026-05-19 23:01 incident_1779246113699.jpg
```
Four JPEG files, 3.1–3.3 MB each.

### photoStatus=SUCCESS and SHA-256 in Room WAL
WAL hex decode (offset 0x1dd30):
```
photoPath:   .../files/incident_photos/incident_1779246097329.jpg
imageSha256: 2a3c119cf3b2277c48da9e4f05b33727454e52ced1f676ef8b15b8f11b985ab2
photoStatus: SUCCESS
```

---

## Acceptance Criteria

| Criterion | Status | Evidence |
|---|---|---|
| Camera permission granted | PASS | `dumpsys package`: `CAMERA: granted=true` |
| Failed unlock triggers foreground service | PASS | Logcat: `ActivityManager: Background started FGS: Allowed` for `LockWitnessCaptureService` |
| Camera opens without restriction (foreground service) | PASS | Logcat: full OPEN→ACTIVE→IDLE→CLOSED cycle; no "Access restricted" message |
| Photo file written to app-private storage | PASS | `ls -la files/incident_photos/`: 4 JPEGs, 3.1–3.3 MB each |
| `photoStatus = SUCCESS` in Room | PASS | WAL hex decode: `SUCCESS` adjacent to `incident_photos/` path |
| `photoPath` populated (non-null) | PASS | WAL: `incident_1779246097329.jpg` path present |
| `imageSha256` populated (non-null) | PASS | WAL: `2a3c119cf3b2277c48da9e4f05b33727454e52ced1f676ef8b15b8f11b985ab2` |

All acceptance criteria: **PASS**.

## Deferred Issues
- **Unit test coverage for `LockWitnessCaptureService`:** The service was not unit-tested in this phase. A unit test would require mocking the full capture pipeline or using a test coroutine dispatcher. Deferred.
- **Multiple incidents per lock session:** 4 photos created from 3 lock attempts (one extra from a previous session). The exact per-callback incident count warrants a follow-up observation in later phases.
- **Video/location capture:** `videoStatus=DISABLED` (Free tier, video gated) and `locationStatus=DISABLED` (location permission not yet granted). Both remain unverified on device — scheduled for future phases.

## Recommended Next Phase
Phase 14-E — Location permission grant + real location snapshot verification: grant location permission, trigger a failed unlock, verify `locationStatus=SUCCESS` and lat/lng populated in the Room incident record.
