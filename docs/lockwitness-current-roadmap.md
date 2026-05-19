# LockWitness Current App Plan and Roadmap

Version: 1.0  
Build: 2026-04-28  
Created: 2026-04-28  
Author: Randy Vickers  
Platform: Android  
Hardened: Yes  
Purpose: Backup roadmap and current-state reference for LockWitness development.  
Status: Release-candidate control pass complete; runtime/device verification pending.

---

# 1. Product Summary

LockWitness is an owner-controlled Android failed-unlock evidence recorder.

The app is designed to detect failed unlock/access attempts on the owner’s own Android device, preserve configurable local evidence, and let the owner review/export that evidence.

Core product principle:

> Local-first, owner-controlled, explicit-permission device access evidence.

LockWitness is not a covert surveillance tool, not a hidden camera app, not a stealth app, and not a stalkerware product.

---

# 2. Core Feature Set

LockWitness currently targets these configurable features:

1. Device Admin activation
2. Failed unlock detection
3. Front-camera photo capture
4. Optional short video capture
5. Optional location snapshot
6. Local incident timeline
7. Incident detail screen
8. SHA-256 evidence hashing
9. Manual ZIP/CSV/metadata/hash export
10. User-controlled Android share/chooser handoff
11. Free/Pro gating model
12. Ad placeholder using Google test banner ID only
13. Diagnostics/reliability screen
14. Runtime verification checklist
15. Play Store compliance documentation

Each sensitive feature is independently toggle-gated where applicable.

---

# 3. Current Development Status

## Completed Phases

### Phase 0 — Control Setup
Status: Complete

Purpose:
- Created repo control structure.
- Added AGENTS.md.
- Added PROJECT_STATE.md.
- Added README_BUILD_PROTOCOL.md.
- Created initial build discipline and Codex control protocol.

### Phase 0.1 — Control Framework Cleanup
Status: Complete

Purpose:
- Cleaned docs/codex-control-framework-full.md.
- Removed script wrapper artifacts.
- Preserved long-form Codex doctrine.

### Phase 1 — App Skeleton
Status: Complete

Purpose:
- Created Android Kotlin/Jetpack Compose project.
- Added Dashboard, Settings, History, and About placeholders.
- Verified debug build.

### Phase 2 — Settings + Toggles
Status: Complete

Purpose:
- Added DataStore-backed settings.
- Added independent toggles:
  - Master monitoring
  - Photo capture
  - Video capture
  - Video duration
  - Location capture
  - Local timeline
  - Email alert
  - Share alert
  - Evidence hashing
- Verified settings persistence by unit tests.

### Phase 3 — Local Data Model
Status: Complete

Purpose:
- Added Room database.
- Added SecurityIncident entity.
- Added DAO.
- Added repository.
- Added tests for insert/read/delete/clear behavior.

### Phase 4 — Device Admin + Failed Unlock Detection
Status: Complete

Purpose:
- Added DeviceAdminReceiver.
- Added manifest receiver declaration.
- Added policy XML.
- Added Device Admin activation/status UI.
- Failed unlock creates SecurityIncident shell.
- Unit tests passed.

Runtime status:
- User-reported smoke test indicates this worked.
- Not independently Codex-verified on device.

### Phase 5 — Photo Capture
Status: Complete

Purpose:
- Added Camera2 still-photo capture.
- Added camera permission declaration/rationale.
- Added local photo save.
- Added SHA-256 hash for photo.
- Added incident status/path/hash updates.
- Added manual test-photo pathway.
- Unit tests passed.

Runtime status:
- User-reported smoke test indicates this worked.
- Not independently Codex-verified on device.

### Phase 6 — Video Capture
Status: Complete

Purpose:
- Added optional short video capture using Camera2.
- Added selected duration support.
- No audio recording.
- Added local MP4 save.
- Added SHA-256 hash for video.
- Added incident video status/path/hash updates.
- Added manual test-video pathway.
- Unit tests passed.

Runtime status:
- User-reported smoke test indicates this worked.
- Not independently Codex-verified on device.

### Phase 7 — Location Snapshot
Status: Complete

Purpose:
- Added location permission declaration/rationale.
- Added Android LocationManager snapshot client.
- Added incident location updater.
- Added manual test-location pathway.
- Added tests for location status/update/failure resilience.

Runtime status:
- User-reported smoke test indicates this worked.
- Not independently Codex-verified on device.

### Phase 8 — Timeline + Detail UI
Status: Complete

Purpose:
- Connected History screen to Room-backed incident timeline.
- Added incident detail screen.
- Added safe fallback display for photo/video paths.
- Added delete single incident.
- Added clear all incidents.
- Added empty/loading/error state handling.
- Unit tests passed.

Runtime status:
- User-reported smoke test indicates this worked.
- Not independently Codex-verified on device.

### Phase 8.1 — Record User-Reported Runtime Smoke Test
Status: Complete

Purpose:
- Recorded user-reported manual runtime verification in PROJECT_STATE.md.
- Clarified that verification was user-reported, not independently Codex-verified.

Reported tested items:
- Device Admin activation
- Camera permission
- Location permission
- Failed unlock flow
- Incident creation
- Photo attempt
- Video attempt
- Location attempt
- History visibility
- Detail statuses/paths/hashes
- Delete single incident
- Clear all incidents

### Phase 9 — Export ZIP/CSV/Metadata/Hashes
Status: Complete

Purpose:
- Added manual local ZIP export.
- ZIP includes:
  - metadata.json
  - incidents.csv
  - hashes.txt
  - available photo files
  - available video files
- Export is user-initiated only.
- No email/upload/share/transmission added.
- Unit tests passed.

Runtime status:
- Not independently runtime-verified.

### Phase 10 — Email / Share Alerts
Status: Complete

Purpose:
- Added user-controlled Android chooser/share handoff.
- Added FileProvider for local export package.
- No automatic email.
- No SMTP.
- No hardcoded destination.
- No cloud/network backend.
- Unit tests passed.

Runtime status:
- Not independently runtime-verified.

### Phase 11 — Ads + Pro Monetization
Status: Complete as scaffolding only

Purpose:
- Added Free/Pro model.
- Added Pro feature gates:
  - Unlimited history
  - Video capture
  - Location snapshot
  - Export ZIP
  - No ads
- Added safe billing fallback abstraction.
- Added banner ad placeholder.
- Used Google test banner ID only:
  - ca-app-pub-3940256099942544/6300978111

Not implemented:
- Production AdMob
- Production Play Billing
- Real SKU/product configuration

### Phase 12 — Diagnostics + Reliability Testing
Status: Complete

Purpose:
- Added diagnostics models.
- Added Diagnostics screen.
- Added RuntimeVerificationChecklist.
- Added diagnostic checks for permissions, toggles, device info, version, and app state.
- Unit tests passed.

Runtime status:
- Requires physical Android device or emulator.

### Phase 12.2 — Backup Policy Reduction
Status: Complete

Purpose:
- Backup ZIPs are now local-only.
- Routine phases use Git checkpoints instead of committed ZIP backups.
- .gitignore ignores backups/*.zip.
- No backup ZIP files should be committed going forward.

### Phase 13 — Play Store Hardening
Status: Complete

Purpose:
- Created privacy policy draft.
- Created Play Store listing draft.
- Created permission disclosure document.
- Created in-app disclosure copy recommendations.
- Created testing/release checklist.
- Created policy-risk review.
- Build/tests still passed.
- No Android source files changed.

### Phase 14 — Final QA / Release Candidate Control Pass
Status: Complete

Purpose:
- Created release-candidate audit.
- Created runtime test plan.
- Created Play Store submission blockers.
- Created monetization production checklist.
- Verified tests/build/source scans.
- No Android source files changed.

Current status:
- Release-candidate checkpoint complete.
- Runtime/device verification remains a blocker.
- Production monetization remains a blocker.
- Final legal/privacy review remains a blocker.

---

# 4. Current Verification State

## Verified by Build/Test

The project has repeatedly passed:

- Gradle unit tests
- Debug build
- Source scans for prohibited behaviors

Latest Phase 14 result:
- testDebugUnitTest: BUILD SUCCESSFUL
- assembleDebug: BUILD SUCCESSFUL
- No prohibited behavior found by source scan
- No Android source files changed in Phase 14

## User-Reported Runtime Verification

User reported successful manual smoke testing after Phase 8 involving:

- Device Admin activation
- Camera permission
- Location permission
- Failed unlock flow
- Incident creation
- Photo/video/location attempts
- History visibility
- Detail statuses/paths/hashes
- Delete and clear actions

Classification:
- User-reported manual runtime verification.
- Not independently Codex-verified.

## Still Not Independently Verified

The following still require physical Android device or emulator testing:

1. App launch
2. Full navigation
3. Device Admin activation
4. Failed unlock trigger
5. Photo capture
6. Video capture
7. Location snapshot
8. History/detail display
9. Export ZIP generation through UI
10. Android chooser/share flow
11. Diagnostics actions
12. Free/Pro gate behavior on device
13. Production AdMob
14. Production Play Billing

---

# 5. Current Release Blockers

LockWitness is not ready for public Play Store production release until these are completed:

1. Physical Android device runtime test
2. Device Admin activation test
3. Failed unlock test
4. Photo capture test
5. Video capture test
6. Location snapshot test
7. History/detail verification
8. Export ZIP test
9. Android chooser/share test
10. Diagnostics screen test
11. Production AdMob setup or ads disabled
12. Production Play Billing setup or Pro disabled
13. Final Data Safety form review
14. Final privacy policy review
15. Closed testing requirement planning
16. Production app signing/release build

---

# 6. Immediate Next Phase

## Phase 15 — Real-Device Runtime QA

Purpose:
Runtime-test the current release candidate on a physical Android device.

Recommended device:
- Old Samsung Android phone, if Android version is compatible and camera/location hardware work.

Core Phase 15 checklist:

1. Enable Developer Options.
2. Enable USB Debugging.
3. Connect phone to PC.
4. Verify adb detects the device.
5. Build debug APK.
6. Install APK.
7. Launch app.
8. Open Settings.
9. Enable Device Admin.
10. Grant camera permission.
11. Grant location permission.
12. Enable monitoring.
13. Enable photo.
14. Enable video.
15. Enable location.
16. Lock phone.
17. Enter wrong PIN/password.
18. Unlock correctly.
19. Open LockWitness.
20. Check History.
21. Open incident detail.
22. Confirm statuses/paths/hashes.
23. Test export ZIP.
24. Test Android chooser/share.
25. Test delete single incident.
26. Test clear all.
27. Record failures.
28. Collect logcat if failures occur.

Expected output:
- Runtime QA report.
- PROJECT_STATE.md update.
- If failures occur, create repair phase before production setup.

---

# 7. Post-Phase-15 Roadmap

## Phase 16 — Runtime Defect Repair, if needed

Purpose:
Fix only defects discovered during Phase 15.

Rules:
- One bug class per repair pass where possible.
- No new features.
- Preserve verified behavior.
- Run tests/build after repair.
- Re-test affected path on device.

Potential repair categories:
- Device Admin trigger not firing
- Photo capture failure
- Video capture failure
- Location failure
- Export failure
- Share chooser failure
- Permission-state mismatch
- Samsung battery/background restriction issue

## Phase 17 — Production Monetization Setup

Purpose:
Replace scaffolding with production-ready monetization if desired.

Tasks:
1. Create AdMob account if not already done.
2. Create production app/ad unit.
3. Replace test ad ID only after ready.
4. Configure Play Billing products.
5. Create Pro product or subscription.
6. Verify billing fallback remains safe.
7. Verify ads do not appear for Pro.
8. Confirm no secrets committed.

Decision point:
- If monetization setup delays launch, launch with ads/billing disabled and add monetization update later.

## Phase 18 — Play Console Submission Package

Purpose:
Prepare app for closed testing.

Tasks:
1. Create Play Console app.
2. Upload app icon.
3. Upload screenshots.
4. Add short description.
5. Add full description.
6. Add privacy policy URL.
7. Complete Data Safety form.
8. Complete content rating questionnaire.
9. Complete target audience declaration.
10. Complete permissions declarations if required.
11. Upload release build/AAB.
12. Create closed testing track.

## Phase 19 — Closed Testing

Purpose:
Satisfy Google Play testing requirement if applicable.

Likely requirement for new personal accounts:
- At least 12 opted-in testers
- 14 continuous days

Recommended:
- Recruit 15–20 testers.
- Track opt-in status.
- Track install status.
- Track device model and Android version.
- Collect feedback.

Tester checklist:
1. Install app.
2. Open app.
3. Enable Device Admin.
4. Grant permissions.
5. Trigger failed unlock.
6. Review incident.
7. Test export/share.
8. Report issues.

## Phase 20 — Production Access / Launch

Purpose:
Apply for production access and launch once requirements are satisfied.

Tasks:
1. Submit production access request.
2. Answer testing questions.
3. Address Google feedback.
4. Upload production release.
5. Monitor crashes, reviews, installs, and conversion.
6. Patch quickly if needed.

---

# 8. Monetization Plan

## Free Tier

Recommended Free features:

- Failed unlock monitoring
- Photo capture
- Limited local timeline
- Basic history/detail view
- Light banner ad, if production ads are enabled

## Pro Tier

Recommended Pro features:

- No ads
- Unlimited history
- Video capture
- Location snapshot
- Export ZIP/CSV/metadata/hashes
- Share/export convenience
- Advanced diagnostics or extended retention

## Recommended Pricing

Initial options:

- Lifetime Pro: $7.99 to $14.99
- Annual Pro: $14.99 to $24.99
- Monthly Pro: optional, $1.99 to $2.99

Recommendation:
Start with Lifetime Pro or Annual Pro. Avoid subscription-only at launch.

---

# 9. Play Store Positioning

## Recommended App Title

LockWitness: Failed Unlock Alerts

## Short Description

Capture and review failed unlock attempts on your own Android device.

## Core Positioning

LockWitness helps you record failed unlock attempts on your own Android device with local evidence, explicit permissions, and user-controlled export/share.

## Language to Use

- owner-controlled
- local-first
- failed unlock evidence
- private timeline
- explicit permissions
- user-initiated export
- no silent transmission

## Language to Avoid

- spy
- secret
- hidden
- stalker
- undetectable
- covert
- surveillance
- catch your partner
- hidden camera
- silent upload

---

# 10. Technical Architecture

## Current Stack

- Android
- Kotlin
- Jetpack Compose
- Room
- DataStore
- Camera2
- LocationManager
- FileProvider
- Gradle
- Unit tests
- Local-only export

## Major Modules

- admin/
  - Device Admin receiver
  - Failed unlock incident creator
  - Device info provider

- data/incident/
  - SecurityIncident entity
  - DAO
  - Room database
  - Repository

- photo/
  - Camera2 photo capture client
  - Local photo store
  - Photo incident updater
  - SHA-256 hasher

- video/
  - Camera2 video capture client
  - Local video store
  - Video incident updater

- location/
  - Android location snapshot client
  - Location incident updater

- export/
  - Export formatter
  - Export models
  - Local incident exporter

- alert/
  - Alert incident updater
  - Android share intent builder

- monetization/
  - Free/Pro model
  - Feature gates
  - Ad placeholder
  - Monetization repository

- diagnostics/
  - Diagnostic models
  - Runtime verification checklist

- ui/
  - Dashboard
  - Settings
  - History
  - Diagnostics
  - About

---

# 11. Backup Policy

Current policy:
- Backup ZIP archives are local-only.
- Do not commit backup ZIP files.
- Routine phases require Git checkpoint commits, not ZIP backups.
- Create local ZIP backups only before/after high-risk phases, release candidates, dependency upgrades, or major refactors.
- .gitignore should ignore backups/*.zip.
- PROJECT_STATE.md must record commit hashes and backup status.

High-risk phases:
- Device Admin changes
- Camera changes
- Video changes
- Billing changes
- Release candidate changes
- Build-system/dependency upgrades
- Major refactors

---

# 12. Current Priority Order

1. Push latest Phase 14 commit to GitHub.
2. Prepare old Samsung Android phone.
3. Run Phase 15 Real-Device Runtime QA.
4. Fix runtime blockers, if any.
5. Decide whether to launch with monetization enabled or deferred.
6. Prepare Play Console closed testing package.
7. Recruit 15–20 testers.
8. Run 14-day closed test if required.
9. Apply for production access.
10. Launch.

---

# 13. Realistic Timeline From Current State

Assuming Phase 14 is pushed:

## If old Samsung is ready immediately

- Real-device QA: 1–3 days
- Runtime fixes: 1–7 days depending on defects
- Play Console package: 1–4 days
- Closed testing: 14–21 days
- Production access review: 3–10+ days

Estimated public launch:
- Best case: 3–4 weeks
- Realistic: 4–6 weeks

## If device testing is delayed

Add delay until physical device/emulator testing is complete.

No public release should occur before runtime testing.

---

# 14. Revenue Outlook

LockWitness can be a small-income app if execution is clean.

Realistic early revenue:

- First 30 days after launch: $0–$50
- 3 months after launch: $25–$250
- Strong execution: $250–$1,000/month possible
- $1,000/month likely requires active ASO, reviews, marketing, and Pro conversion

Main revenue levers:

1. App reliability
2. Play Store screenshots
3. Reviews
4. Pro upgrade clarity
5. Search keywords
6. No creepy branding
7. Trust positioning
8. Early tester feedback

---

# 15. Key Risks

## Technical Risks

- Samsung background/battery restrictions
- Device Admin behavior differences
- Camera2 behavior while locked
- Video capture reliability
- Location unavailable states
- Export file permission behavior
- Share chooser/FileProvider issues

## Store Policy Risks

- Device Admin sensitivity
- Camera/location permissions
- Misleading or covert language
- Data Safety form mismatch
- Claiming no transmission while share/export behavior exists
- Ads/billing misconfiguration

## Business Risks

- Low discoverability
- Weak conversion
- Too few reviews
- Competitor saturation
- User distrust of camera/security apps
- Poor retention if users only use app rarely

---

# 16. Decision Points Before Launch

Before closed testing:

1. Is production AdMob enabled or deferred?
2. Is Pro billing enabled or deferred?
3. Is video a Free feature or Pro feature?
4. Is location a Free feature or Pro feature?
5. Is export a Free feature or Pro feature?
6. What is the exact pricing?
7. What is the privacy policy URL?
8. Who are the 15–20 testers?
9. Is the old Samsung test clean?
10. Are release blockers documented as resolved?

---

# 17. Recommended Immediate Next Command Set

Push current repo state:

```powershell
Set-Location "C:\Projects\LockWitness"

git status
git push
git status