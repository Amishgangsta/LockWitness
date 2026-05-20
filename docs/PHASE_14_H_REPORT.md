# Phase 14-H Report — Diagnostics Screen Runtime Verification

## Phase Summary
Verified the Diagnostics screen on physical device. All 14 status checks rendered correctly. All five manual diagnostic actions (Photo, Location, Export, Share Chooser, Video) returned PASS. Two notable findings beyond the phase scope: Location diagnostic returned PASS (LocationManager cache populated from prior outdoor Maps session, confirming `locationStatus=SUCCESS` is achievable on this device), and Video diagnostic returned PASS with a real 3.1 MB MP4 written to app-private storage (first confirmed video capture on device). No source code changes.

## Starting Commit
`fec0175` — checkpoint: after phase 14-G

## Ending Commit
See post-phase commit below.

## ZIP Backup
No ZIP backup created under reduced backup policy.

---

## Files Inspected
- `CLAUDE.md`, `PROJECT_STATE.md`, `AGENTS.md`
- `docs/codex-control-framework-full.md`
- `android/app/src/main/java/com/lockwitness/app/ui/screens/DiagnosticsScreen.kt`
- `android/app/src/main/java/com/lockwitness/app/diagnostics/DiagnosticModels.kt`

## Files Changed
None. Runtime-only verification phase.

---

## Build Commands Run
None (no source changes; existing APK from Phase 14-E used).

## Test Commands Run
None (runtime-verification phase).

---

## Runtime Evidence

### Checks section — all 14 checks confirmed by user
User confirmed all checks rendered correctly. Only deviation from PASS was expected:

| Check | Result | Detail |
|---|---|---|
| Device Admin | PASS | Active |
| Camera permission | PASS | Granted |
| Location permission | PASS | Granted |
| Master monitoring toggle | PASS | Enabled |
| Photo toggle | PASS | Enabled |
| Video toggle | WARNING | Disabled |
| Location toggle | PASS | Enabled |
| Timeline/history availability | PASS | Available |
| Export availability | PASS | Available |
| Share chooser availability | NOT_TESTED → PASS | Updated after Share Chooser tap |
| Free/Pro mode | PASS | Pro |
| App version | PASS | 0.1.0 |
| Android version | PASS | 12 |
| Device model | PASS | samsung SM-G973U1 |

Video toggle WARNING is correct — video is off by default in user settings.

### Photo diagnostic — PASS
User reported: "photo diagnostic pass"
File evidence:
```
adb shell run-as com.lockwitness.app ls -la files/incident_photos/ | tail -2
-rw------- 1 u0_a35 u0_a35 3033631 2026-05-20 07:11 incident_1779275494853.jpg
-rw------- 1 u0_a35 u0_a35 2780195 2026-05-20 07:11 incident_1779275510439.jpg
```
Two JPEG files written at 07:11 (3.0 MB and 2.8 MB).

### Location diagnostic — PASS (unexpected)
User reported: "pass"
Status message format: "Location diagnostic PASS: lat, lng"

This is the first `LocationSnapshotResult.Success` observed on this device. The raw `LocationManager` cache was populated after the user's outdoor trip (Maps session, Phase 14-E follow-up). Confirms `locationStatus=SUCCESS` is achievable on this hardware — the Phase 14-E UNAVAILABLE result was correctly attributed to the indoor/USB-tethered test environment, not a code defect.

### Export diagnostic — PASS
User reported full status message: "Export diagnostic PASS: /data/user/0/com.lockwitness.app/files/exports/lockwitness_diagnostic_1779275684253.zip"
File evidence:
```
adb shell run-as com.lockwitness.app ls -la files/exports/
-rw------- 1 u0_a35 u0_a35 50582495 2026-05-20 07:14 lockwitness_diagnostic_1779275684253.zip
```
50.5 MB diagnostic export written (contains all 22 incidents + photos).

### Share Chooser diagnostic — PASS
User reported: "Pass"
Share chooser availability check in the Checks section updated from NOT_TESTED → PASS.

### Video diagnostic — PASS (informational; first device confirmation)
User reported: "pass"
Status message format: "Video diagnostic PASS: /data/user/0/com.lockwitness.app/files/incident_videos/incident_1779275874802.mp4"
File evidence:
```
adb shell run-as com.lockwitness.app ls -la files/incident_videos/
-rw------- 1 u0_a35 u0_a35 3197106 2026-05-20 07:18 incident_1779275874802.mp4
```
3.1 MB MP4 file written — first confirmed Camera2 video capture on this device.

Note: this test ran from the app foreground (Diagnostics screen), not via `LockWitnessCaptureService`. Whether the foreground service path also captures video successfully on failed unlock is a separate verification (deferred).

---

## Acceptance Criteria

| Criterion | Status | Evidence |
|---|---|---|
| Diagnostics screen opens without crash | PASS | User navigated and reported checks |
| All 14 checks render with correct results | PASS | User confirmed; Video WARNING is expected |
| Photo diagnostic PASS | PASS | User report + 2 JPEGs in files/incident_photos/ at 07:11 |
| Location diagnostic correct behavior | PASS | User report: PASS (LocationManager cache warm from outdoor trip) |
| Export diagnostic PASS | PASS | User report + lockwitness_diagnostic_1779275684253.zip (50.5 MB) in files/exports/ |
| Share Chooser diagnostic PASS | PASS | User report + Share chooser check updated to PASS |
| Video diagnostic informational | PASS | User report + incident_1779275874802.mp4 (3.1 MB) in files/incident_videos/ |

All acceptance criteria: **PASS**.

---

## Notable Findings

### Location PASS confirmed on device
Raw `LocationManager.getLastKnownLocation()` returned a fix after the outdoor Maps session. This confirms `locationStatus=SUCCESS` is achievable on SM-G973U1 when the device has been outdoors and the GPS cache is warm. The Phase 14-E UNAVAILABLE result was environmental, not a defect.

### First confirmed video capture on device
`Camera2VideoCaptureClient` captured a real front-camera video (3.1 MB MP4) in the app foreground. The video pipeline is functional. A separate phase would be needed to verify video capture via the `LockWitnessCaptureService` foreground service path (i.e., on a real failed-unlock event with the video toggle enabled).

---

## Deferred Issues
- **Video capture via foreground service on failed unlock:** The Diagnostics video test ran in the app foreground. The `LockWitnessCaptureService` path for video on failed unlock remains unverified at runtime. The video toggle is currently disabled in Settings, which would need to be enabled and a failed unlock triggered to verify the end-to-end path.
- **`locationStatus=SUCCESS` in Room incident record:** Location PASS was observed in Diagnostics but the LocationManager cache state may not persist. A failed-unlock test with a warm GPS cache is needed to see `locationStatus=SUCCESS` written to a Room incident.
- **Email alert path:** `emailEnabled=false` on all incidents; email status runtime verification remains deferred.

## Recommended Next Phase
Phase 14-I — Final runtime state summary and release-readiness assessment: review all 14 phases of runtime verification, document what is verified vs. still deferred, and produce a go/no-go assessment for Play Store submission against the blockers listed in `docs/play-store-submission-blockers.md`.
