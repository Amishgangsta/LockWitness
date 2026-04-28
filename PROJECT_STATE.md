# LockWitness Project State

## Product Goal
LockWitness is an owner-controlled Android failed-unlock evidence recorder.

## Current Phase
Phase 13 — Play Store Hardening.

## Verified Features
Phase 1 Android app skeleton build verified.
Phase 2 settings persistence unit tests passed.
Phase 3 local incident Room persistence tests passed.
Phase 4 failed-unlock incident shell creation logic unit tests passed.
Phase 5 photo hash/status/failure-resilience unit tests passed.
Phase 6 video hash/status/duration/failure-resilience unit tests passed.
Phase 7 location status/update/failure-resilience unit tests passed.
Phase 8 incident history mapping and delete/clear action unit tests passed.
Phase 9 local export metadata/CSV/hash/missing-media unit tests passed.
Phase 10 email/share alert toggle/status/failure-resilience unit tests passed.
Phase 11 Free/Pro gate, ad placeholder, and billing fallback unit tests passed.
Phase 12 diagnostics mapping and runtime checklist unit tests passed.

## Verified Control Status
Phase 0 repository control files and required folders verified on 2026-04-27.

## Unverified Features
Cloud backend features.
Runtime launch, Device Admin activation, failed-unlock callback behavior, real photo capture, real video capture, and real location snapshot remain unverified on device/emulator.
History navigation, media fallback display, actual manual ZIP export UI, actual share/email chooser flow, actual ads/billing environment, and Diagnostics runtime actions remain unverified on device/emulator.

## Deferred Features
Cloud, PDF export, production ads, and production billing integration.

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

## Backup Policy
Backup ZIP archives are local-only and must not be committed to Git.

Routine phases require Git checkpoint commits, not ZIP backups.

Create local ZIP backups only before and after high-risk phases, release candidates, dependency upgrades, or major refactors. High-risk phases include Device Admin, camera, video, billing, release candidate, and major build-system changes.

Each phase record must include the relevant commit hash and whether a local ZIP backup was created. If no ZIP backup is created, the phase report must say: “No ZIP backup created under reduced backup policy.”

## Last Backup
C:\Projects\LockWitness\backups\phase-12-after-20260427-234420.zip

## Last Verified Build
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 3 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 7 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 9 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 13 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 18 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 23 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 27 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 33 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 40 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 47 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android with 52 tests, 0 failures, 0 errors.
2026-04-27: `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-27: Phase 12 re-verification `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android; XML test reports show 52 tests, 0 failures, 0 errors.
2026-04-27: Phase 12 re-verification `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android with ANDROID_HOME=C:\Users\Randy\AppData\Local\Android\Sdk.
2026-04-28: Phase 13 `.\gradlew.bat testDebugUnitTest` passed from C:\Projects\LockWitness\android.
2026-04-28: Phase 13 `.\gradlew.bat assembleDebug` passed from C:\Projects\LockWitness\android.

## Known Defects
None yet.

## Next Authorized Phase
Phase 13 only until user authorizes the next phase.

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

## Last Phase 7 Verification
- Branch: main
- Starting commit: 80935f1beeeb9e97ac041b0826f6a6cfa23d1a1a
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-7-before-20260427-214123.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-7-after-20260427-214533.zip
- Location snapshot implementation: Android LocationManager snapshot using enabled last-known providers with fine/coarse permission gate.
- Location permission: ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION declared and surfaced in Settings with visible status/rationale and manual test-location action.
- Failed-unlock location path: incident shell is created first, photo and video updaters run independently, then location updater updates latitude, longitude, accuracy, provider, and locationStatus without overwriting photo/video fields.
- Tests: `LocationIncidentUpdaterTest` passed 5 tests; full `testDebugUnitTest` passed 23 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test with location permission granted and location services enabled.
- Email/share/export/ads/billing/cloud/SMS/audio/stealth work performed: none.

## Last Phase 8 Verification
- Branch: main
- Starting commit: e0c7b03440ee5176d8b73a5b75e0a4545330f469
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-8-before-20260427-222954.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-8-after-20260427-223326.zip
- Timeline UI: History screen reads Room incidents from SecurityIncidentRepository and displays records in DAO timestamp-desc order.
- Detail UI: shows trigger metadata, settings snapshot, device metadata, media paths, location fields, hashes, module statuses, and notes.
- Media preview approach: safe fallback path/status display; runtime photo/video preview not added in this phase.
- Delete/clear: single delete and clear-all actions call SecurityIncidentRepository through IncidentHistoryActions.
- Tests: `IncidentHistoryMapperTest` passed 4 tests; full `testDebugUnitTest` passed 27 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test.
- Export/email/share/ads/billing/cloud/SMS/audio/stealth work performed: none.

## User-Reported Runtime Smoke Test — After Phase 8
User-reported manual runtime verification; not independently Codex-verified.

Reported tested items:
- Device Admin activation tested
- Camera permission tested
- Location permission tested
- Failed unlock flow tested
- Incident creation tested
- Photo attempt tested
- Video attempt tested
- Location attempt tested
- History visibility tested
- Detail statuses/paths/hashes tested
- Delete single incident tested
- Clear all incidents tested

## Last Phase 9 Verification
- Branch: main
- Starting commit: 1ebfbd65e58d36e8368a4662a0fe6e3993a51e10
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-9-before-20260427-224858.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-9-after-20260427-225227.zip
- Manual export implementation: History screen provides user-initiated Export All and Export Incident actions.
- Export storage: app-local ZIP files under the app files export directory; no email/share intent, cloud upload, or automatic transmission path added.
- ZIP contents: metadata.json, incidents.csv, hashes.txt, and available photo/video media files.
- Missing media behavior: missing photo/video files are recorded in export metadata/hash text and do not fail export generation.
- Tests: `IncidentExportFormatterTest` passed 4 tests; `LocalIncidentExporterTest` passed 2 tests; full `testDebugUnitTest` passed 33 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test.
- Email/share alerts, ads, billing, cloud/network transmission, SMS, audio, stealth, hidden icon, overlay/accessibility work performed: none.

## Last Phase 10 Verification
- Branch: main
- Starting commit: f2d108735e65419066015fbec6cd0bdaba82673a
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-10-before-20260427-225840.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-10-after-20260427-230402.zip
- Email/share behavior: user-controlled Android chooser handoff from incident detail using local export package.
- Failed-unlock alert path: no sender launched; enabled email/share alerts are marked UNAVAILABLE with notes requiring user action, disabled toggles remain DISABLED.
- Manual chooser path: creates local export first, then launches chooser by explicit user button; chooser launch failure records FAILED without deleting incident/export/media/location data.
- Credentials/destinations: no hardcoded recipients, SMTP credentials, API keys, tokens, passwords, or cloud backend added.
- Tests: `AlertIncidentUpdaterTest` passed 6 tests; `AlertShareIntentBuilderTest` passed 1 test; full `testDebugUnitTest` passed 40 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test.
- Ads, billing, cloud/network transmission, SMS, audio, stealth, hidden icon, overlay/accessibility work performed: none.

## Last Phase 11 Verification
- Branch: main
- Starting commit: f0777990a48db7ac869e7537a74a1d1951b20646
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-11-before-20260427-230814.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-11-after-20260427-231308.zip
- Free/Pro model: local MonetizationState, MonetizationRepository, ProFeatureGate, and safe fallback billing service.
- Pro gates: unlimited history, video capture, location snapshot, ZIP export, and ad removal are represented in gate logic.
- Free behavior: failed-unlock monitoring and photo capture remain available; video/location/export are gated in Free mode; local timeline is limited to 10 visible records in Free mode.
- Ads: banner ad placeholder only; uses Google test banner ID `ca-app-pub-3940256099942544/6300978111`; no ad SDK, interstitial, rewarded, app-open, or production ad IDs added.
- Billing: safe abstraction only; billing unavailable falls back to Free mode without crashing.
- Tests: `ProFeatureGateTest` passed 4 tests; `MonetizationRepositoryTest` passed 3 tests; `FailedUnlockIncidentCreatorTest` includes Free-mode video/location gate coverage; full `testDebugUnitTest` passed 47 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator and Play billing/ad test environment.
- Production ad IDs, hardcoded secrets/API keys/tokens, cloud/backend transmission, SMS, audio, stealth, hidden icon, overlay/accessibility work performed: none.

## Last Phase 12 Verification
- Branch: main
- Starting commit: 38fbd7639ab83c2956e516870a03887d423242a6
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-12-before-20260427-232352.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-12-after-20260427-232743.zip
- Diagnostics UI: Diagnostics destination added with checks for Device Admin, camera permission, location permission, toggles, history/export/share availability, Free/Pro mode, app version, Android version, and device model.
- Manual diagnostics: explicit user actions call existing photo, video, location, export, and share-chooser paths only.
- Runtime checklist: in-app checklist generated for Device Admin activation, wrong unlock, photo, video, location, history/detail, export, share chooser, and Free/Pro gate tests.
- Diagnostic result model: PASS, FAIL, WARNING, NOT_TESTED, and UNAVAILABLE.
- Tests: `DiagnosticMapperTest` passed 3 tests; `RuntimeVerificationChecklistTest` passed 2 tests; full `testDebugUnitTest` passed 52 tests total.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime verification: not performed; requires physical Android device/emulator test.
- Production ad IDs, production billing implementation, hardcoded secrets/API keys/tokens, cloud/backend transmission, SMS, audio, stealth, hidden icon, overlay/accessibility work performed: none.

## Phase 12A Runtime Verification Attempt
- Branch: main
- Starting commit: b8034721815cd4784ffd2b7186a31ba86571e15b
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-12A-before-20260427-233335.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-12A-after-20260427-233820.zip
- Unit tests: `.\gradlew.bat testDebugUnitTest` passed with exit code 0.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Runtime device discovery: `adb devices` and `adb devices -l` reported no attached devices or emulators.
- Emulator availability: no `emulator.exe` was found under C:\Users\Randy\AppData\Local\Android\Sdk, and no AVD entries were found under the accessible `.android\avd` path.
- Logcat review: crash logcat could not be completed because no Android device/emulator was attached; `adb logcat -d -b crash` timed out.
- Runtime verification result: blocked. Diagnostics screen navigation, diagnostic actions, and logcat review were not runtime-verified in Codex.
- App feature work performed: none.

## Phase 12 Re-Verification
- Branch: main
- Starting commit: 296a5cd0947ffc09116c201b332369630712e52d
- Pre-phase backup: C:\Projects\LockWitness\backups\phase-12-before-20260427-234243.zip
- Post-phase backup: C:\Projects\LockWitness\backups\phase-12-after-20260427-234420.zip
- Diagnostics UI: file evidence confirms Diagnostics destination and screen remain present.
- Diagnostics checks: file evidence confirms Device Admin, camera permission, location permission, toggles, history/export/share availability, Free/Pro mode, app version, Android version, and device model checks remain present.
- Manual diagnostics: file evidence confirms explicit user-action buttons invoke existing photo, video, location, export, and share chooser paths.
- Runtime checklist: file evidence confirms Device Admin activation, wrong unlock, photo, video, location, history/detail, export, share chooser, and Free/Pro gate checklist items remain present.
- Diagnostic result model: PASS, FAIL, WARNING, NOT_TESTED, and UNAVAILABLE remain present.
- Tests: `.\gradlew.bat testDebugUnitTest` passed; XML reports show 52 tests, 0 failures, 0 errors.
- Build command: `.\gradlew.bat assembleDebug`
- Build result: passed with exit code 0.
- Source scan: no case-sensitive hits for deprecated `android.hardware.Camera`, RECORD_AUDIO, SMS, accessibility-service, overlay permission, SMTP, or audio-source APIs; only Google test banner ad ID was found.
- Runtime verification: not performed; requires physical Android device/emulator test.
- App feature work performed: none during this re-verification pass.

## Phase 12.2 Backup Policy Reduction
- Branch: main
- Starting commit: 01e97b17f0d124d93260b9766c9811cfd0175518
- Local ZIP backup created: no.
- Backup ZIP note: No ZIP backup created under reduced backup policy.
- Policy update: backup ZIP archives are local-only, less frequent, and must not be committed to Git.
- Routine phase backup method: Git checkpoint commits.
- High-risk ZIP backup triggers: Device Admin, camera, video, billing, release candidate, dependency upgrades, major refactors, and major build-system changes.
- Git ignore policy: `backups/*.zip` ignored.
- Android source files modified: none.

## Phase 13 Play Store Hardening
- Branch: main
- Starting commit: f474cb62922bb7bd09ff44eb58772a780949b278
- Local ZIP backup created: no.
- Backup ZIP note: No ZIP backup created under reduced backup policy.
- Privacy policy draft: store/privacy-policy-draft.md
- Play Store listing draft: store/play-store-listing-draft.md
- Permission disclosure document: store/permission-disclosures.md
- In-app disclosure copy recommendations: docs/in-app-disclosure-copy-recommendations.md
- Testing and release checklist: docs/testing-release-checklist.md
- Policy risk review: docs/policy-risk-review.md
- Android source files modified: none.
- Verification: `.\gradlew.bat testDebugUnitTest` passed.
- Verification: `.\gradlew.bat assembleDebug` passed.
- Language scan: Phase 13 documents contain no matches for the prohibited wording list from the task.
- Runtime verification: not performed; documentation-only phase.
