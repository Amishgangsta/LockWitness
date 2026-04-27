# LockWitness Project State

## Product Goal
LockWitness is an owner-controlled Android failed-unlock evidence recorder.

## Current Phase
Phase 6 — Video Capture.

## Verified Features
Phase 1 Android app skeleton build verified.
Phase 2 settings persistence unit tests passed.
Phase 3 local incident Room persistence tests passed.
Phase 4 failed-unlock incident shell creation logic unit tests passed.
Phase 5 photo hash/status/failure-resilience unit tests passed.
Phase 6 video hash/status/duration/failure-resilience unit tests passed.

## Verified Control Status
Phase 0 repository control files and required folders verified on 2026-04-27.

## Unverified Features
Location provider, email/share/export, ads, billing, and cloud features.
Runtime launch, Device Admin activation, failed-unlock callback behavior, real photo capture, and real video capture remain unverified on device/emulator.

## Deferred Features
Ads, billing, cloud, PDF export, email provider integration.

## Active Stack
Kotlin, Jetpack Compose, CameraX or Camera2, Room, DataStore, WorkManager.

## Core Feature Toggles
- Master monitoring
- Photo capture
- Short video capture
- GPS/location capture
- Local timeline
- Email alert
- Share alert
- Manual export
- SHA-256 hashing

## Current Rule
Codex must not proceed beyond the active phase without user authorization.

## Last Backup
C:\Projects\LockWitness\backups\phase-6-after-20260427-194706.zip

## Last Verified Build
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 3 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 7 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 9 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 13 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 18 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.

## Known Defects
None yet.

## Next Authorized Phase
Phase 6 only until user authorizes the next phase.

## Initial Bootstrap Backup
C:\Projects\LockWitness\backups\phase-0-initial-20260427-171116.zip

## Last Phase 0 Verification
- Branch: main
- Commit before verification: e9bde10dfaeb5d7733eb6d95cde6fb0e12d4cc9b
- Pre-phase checkpoint commit: 4bc2abe
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-0-before-20260427-173944.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-0-after-20260427-174015.zip
- Required control files: present
- Required project folders: present
- App feature work performed: none

## Last Phase 0.1 Verification
- Branch: main
- Starting commit: b4d3d8f68c57719e203df9cb4b73980154bc36af
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-0.1-before-20260427-174250.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-0.1-after-20260427-174354.zip
- Control framework document cleaned to readable Markdown doctrine only.
- App feature work performed: none

## Last Phase 1 Verification
- Branch: main
- Starting commit: 0b7b17f994c93ecc21a0d4b4d1014d9838b716b6
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-1-before-20260427-175338.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-1-after-20260427-181311.zip
- Android project path: C:\Projects\LockWitness\android
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires device/emulator test.
- App feature work performed: skeleton/navigation placeholders only.
- Security/camera/location/ads/billing/email work performed: none.

## Last Phase 2 Verification
- Branch: main
- Starting commit: 75ea4fe4dbc787574aa3372b4660353ad39ce712
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-2-before-20260427-182722.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-2-after-20260427-183428.zip
- Settings persistence: DataStore Preferences.
- Settings tests: `SettingsRepositoryTest` passed 3 tests, 0 failures, 0 errors.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires device/emulator test.
- Security/camera/location/ads/billing/email provider work performed: none.

## Last Phase 3 Verification
- Branch: main
- Starting commit: b0be8e80c901da39befc852f87b7207440f1f4da
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-3-before-20260427-184043.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-3-after-20260427-185140.zip
- Persistence: Room database with SecurityIncident entity, DAO, and repository.
- Tests: `SecurityIncidentRepositoryTest` passed 4 tests, 0 failures, 0 errors; full `testDebugUnitTest` passed 7 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires device/emulator test.
- DeviceAdmin/camera/location-provider/email/share/export/ads/billing/cloud work performed: none.

## Last Phase 4 Verification
- Branch: main
- Starting commit: 5b5c85c3b2bde19469294abffd402e94d48eacbd
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-4-before-20260427-191619.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-4-after-20260427-192659.zip
- Device Admin receiver: added.
- Device Admin policy XML: added.
- Failed-unlock shell creator: creates local SecurityIncident shell when master monitoring is enabled.
- Tests: `FailedUnlockIncidentCreatorTest` passed 2 tests; full `testDebugUnitTest` passed 9 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test with Device Admin enabled.
- Camera/photo/video capture/location-provider/email/share/export/ads/billing/cloud/SMS/audio work performed: none.

## Last Phase 5 Verification
- Branch: main
- Starting commit: f4493ba10388ccb8d335532e5622fa726dd10421
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-5-before-20260427-193136.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-5-after-20260427-193646.zip
- Photo capture implementation: Camera2 front-camera still photo capture.
- Camera permission: declared and surfaced in Settings with visible status/rationale and manual test-photo action.
- Failed-unlock photo path: incident shell is created first, then photo capture updates photoPath, imageSha256, and photoStatus.
- Tests: `PhotoIncidentUpdaterTest` passed 3 tests; `Sha256HasherTest` passed 1 test; full `testDebugUnitTest` passed 13 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test with camera permission granted.
- Video/location-provider/email/share/export/ads/billing/cloud/SMS/audio/stealth work performed: none.

## Last Phase 6 Verification
- Branch: main
- Starting commit: 653e27f21932bc6309756eb87a738967d4feea6b
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-6-before-20260427-194122.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-6-after-20260427-194706.zip
- Video capture implementation: Camera2 front-camera short video capture with camera permission gate.
- Audio recording: not implemented; no RECORD_AUDIO permission, setAudioSource call, or MediaRecorder.AudioSource usage found by source scan.
- Failed-unlock video path: incident shell is created first, photo updater runs independently, then video updater updates videoPath, videoSha256, and videoStatus without overwriting photo fields.
- Tests: `VideoIncidentUpdaterTest` passed 5 tests; full `testDebugUnitTest` passed 18 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test with camera permission granted.
- Location-provider/email/share/export/ads/billing/cloud/SMS/audio/stealth work performed: none.
