# LockWitness Codex Control Framework

## Purpose

This document establishes the Codex control framework for building LockWitness.

It exists to prevent Codex drift, hallucinated status reports, unverified completion claims, scope creep, destructive changes, skipped verification, missing backups, and loss of project direction between phases.

LockWitness must be built under a phase-gated, verification-first, no-drift development protocol.

## Project Objective

Build a polished Android Kotlin Jetpack Compose app named LockWitness that records failed unlock/access attempts on the user's own Android device, captures configurable evidence, stores it locally, and prepares for monetization.

## Non-Negotiable Product Direction

LockWitness is an owner-controlled, local-first failed-unlock evidence recorder.

LockWitness is not spyware, not a stealth app, and not a hidden camera app. It must not use deceptive, stalkerware, or covert-surveillance patterns.

## Core Feature Model

Each major feature must be independently switchable:

1. Master monitoring toggle
2. Photo capture toggle
3. Short video toggle with 5/10/15/30 second duration selector
4. GPS/location toggle
5. Local timeline toggle
6. Email alert toggle
7. Share alert toggle
8. Manual export action
9. SHA-256 hashing / evidence integrity mode

## Absolute Constraints

- Do not use deprecated `android.hardware.Camera`.
- Use CameraX or Camera2 for camera work.
- Do not hardcode credentials, API keys, emails, tokens, SMTP passwords, or secrets.
- Do not silently upload, email, share, or transmit captured media.
- Do not add SMS, call-log, contacts, microphone/audio recording, overlay abuse, accessibility-service abuse, hidden icon, stealth mode, or anti-uninstall tricks.
- Do not add features outside the active phase.
- Do not claim a feature works unless it has been verified by build output, test output, runtime/device evidence, or file evidence.
- Do not mark a phase complete if acceptance criteria are unmet.
- Do not fabricate test results.
- Do not infer runtime success from compilation alone.
- Do not delete working code without first creating a backup and explaining why deletion is necessary.

## Truth And Verification Rules

All completion claims must be tied to evidence.

Allowed claim example: "Gradle build passed. Evidence: `./gradlew assembleDebug` completed with exit code 0."

Allowed claim example: "Toggle persistence appears verified. Evidence: `SettingsRepositoryTest` passed."

Forbidden claims unless supported by actual build, test, runtime, or file evidence:

- "This should work."
- "The feature is implemented successfully."
- "The app is ready."
- "Everything is wired up."
- "Camera capture works."

If runtime verification cannot be performed in the current environment, state: "Implemented but not runtime-verified. Requires device/emulator test."

## Phase Lock

Work in numbered phases only. Do not proceed to the next phase unless the user authorizes it after reviewing the phase report.

Only perform work inside the active phase. If a requested change requires expanding scope, stop and ask for authorization.

If unrelated issues are discovered, log them under "Deferred Issues" and do not fix them unless they block the active phase.

## Backup Protocol

Backup ZIP archives are local-only safeguards and must not be committed to Git.

Routine phases require Git checkpoint commits, not ZIP backups.

Create local ZIP backups only before and after high-risk phases, release candidates, dependency upgrades, or major refactors. High-risk phases include Device Admin, camera, video, billing, release candidate, and major build-system changes.

Before each phase:

1. Inspect Git status.
2. Create a Git checkpoint if the repository is initialized and repository state has changed:
   - `git status`
   - `git add -A`
   - `git commit -m "checkpoint: before phase X - <phase name>"`
3. For high-risk phases only, create a local full project archive:
   - `backups/phase-X-before-YYYYMMDD-HHMMSS.zip`

After each phase:

1. Create a Git checkpoint:
   - `git add -A`
   - `git commit -m "checkpoint: after phase X - <phase name>"`
2. For high-risk phases only, create a local full project archive:
   - `backups/phase-X-after-YYYYMMDD-HHMMSS.zip`

If Git is not initialized, initialize Git unless instructed otherwise. Never rely only on working-tree state. `PROJECT_STATE.md` must record the relevant commit hash and whether a local ZIP backup was created. If no backup ZIP is created, the phase report must say: “No ZIP backup created under reduced backup policy.”

## Project State Maintenance

`PROJECT_STATE.md` must be updated after each phase with:

- Current phase
- Verified and unverified work
- Backup paths
- Latest relevant commit
- Known defects
- Deferred issues
- Next authorized phase

The state file must not claim feature completion without evidence.

## Phase Gates

### Phase 0 — Repository and Control Setup

Acceptance criteria:

- Git initialized or verified.
- `PROJECT_STATE.md` exists.
- `backups/` directory exists.
- `README_BUILD_PROTOCOL.md` exists.
- Initial backup ZIP exists.
- No app feature work performed.

### Phase 1 — Android Project Baseline

Acceptance criteria:

- Android project builds.
- App launches to dashboard.
- Navigation works.
- No failed-unlock, camera, location, or email code yet.
- Verification: `./gradlew assembleDebug` passes.

### Phase 2 — Settings Toggles

Acceptance criteria:

- All toggles exist.
- Toggles persist using DataStore.
- UI shows permission status per feature.
- Unit tests or manual persistence test documented.
- No camera capture yet unless authorized.

### Phase 3 — Device Admin Incident Shell

Acceptance criteria:

- Device Admin onboarding exists.
- `DeviceAdminReceiver` registered correctly.
- Failed unlock event creates local incident shell.
- Incident includes timestamp, trigger type, failed attempt count, and device info.
- Verification includes device/emulator test evidence or states that physical device verification is required.

### Phase 4 — Photo Capture

Acceptance criteria:

- Uses CameraX or Camera2, not deprecated Camera API.
- Manual test capture works.
- Failed-unlock photo capture is attempted only if the photo toggle is enabled.
- Incident survives even if photo capture fails.
- SHA-256 hash generated for photo.

### Phase 5 — Video Capture

Acceptance criteria:

- Video toggle exists.
- Duration selector works: 5/10/15/30 seconds.
- No audio recording.
- Video path and SHA-256 hash are stored.
- If video fails, photo/log still survive.

### Phase 6 — Location Capture

Acceptance criteria:

- Location toggle exists.
- Permission rationale exists.
- Incident logs location when permission and provider are available.
- If unavailable, incident records `locationStatus` without failing.

### Phase 7 — Timeline, Detail, Delete, Export

Acceptance criteria:

- History screen lists incidents.
- Detail screen shows media, metadata, statuses, and hashes.
- Delete single incident works.
- Clear history works.
- ZIP export includes media, `metadata.json`, `hashes.txt`, and CSV.

### Phase 8 — Email And Share

Acceptance criteria:

- No hardcoded credentials.
- User-controlled configuration only.
- Email/share failure does not destroy local incident.
- Share uses Android-safe intent/provider abstraction.

### Phase 9 — Ads And Billing

Acceptance criteria:

- AdMob IDs use debug/test IDs only until production values are supplied.
- Billing is isolated behind feature flags.
- App still works if ads or billing fail.
- Pro feature gates are clear but not hostile.

### Phase 10 — Store And Policy Readiness

Acceptance criteria:

- Privacy policy draft created.
- Permission disclosure text created.
- Store listing draft created.
- App contains no spyware/stalkerware language.
- No hidden icon, stealth mode, SMS, audio, overlay abuse, or silent upload.

## No Progress Without Proof

Do not begin the next phase until:

1. Current phase acceptance criteria are checked one by one.
2. Verification evidence is reported.
3. `PROJECT_STATE.md` is updated.
4. Backup policy is followed and documented.
5. User authorizes the next phase.

If any criterion fails, stop, report the failure, provide a repair plan, and do not proceed to new features.

## Phase Report Format

Every phase report must use this structure:

```text
Phase:
Goal:
Branch:
Starting commit:
Ending commit:
Backup before:
Backup after:

In scope:
Out of scope:

Files changed:
Files created:
Files deleted:

Verification performed:
- Command:
- Result:
- Evidence:

Acceptance criteria:
[ ] Criterion 1
[ ] Criterion 2
[ ] Criterion 3

Verified working:
Not verified:
Known defects:
Deferred issues:
Security/privacy review:
Play Store risk review:
Next recommended phase:
STOP STATUS:
```

## Regression Rule

After each phase, run previous phase verification steps again where possible.

A new phase is not complete if it breaks an earlier verified control or product behavior.

## Feature Flag Rule

Each major feature must be independently disableable in code and settings.

A broken optional feature must not crash the app or block the core incident log.

## Incident Resilience Rule

On failed unlock, always attempt to create an incident shell first.

Then modules run independently:

- Photo
- Video
- Location
- Email/share
- Export

Failure of one module must be recorded in status fields and must not prevent the incident from saving.

## Complete Codex Master Prompt

You are building LockWitness under a strict phase-gated control protocol.

Your job is not to rush features. Your job is to produce verified, backed-up, phase-complete work without hallucinated claims, drift, or scope creep.

Before modifying code:

1. Read `AGENTS.md`.
2. Read `PROJECT_STATE.md` if it exists.
3. Read `README_BUILD_PROTOCOL.md` if it exists.
4. Read `docs/codex-control-framework-full.md` if it exists.
5. Inspect Git status.
6. Identify current branch and commit.
7. Restate the active phase goal.
8. Restate the product goal.
9. List in-scope and out-of-scope items.
10. Create a pre-phase local ZIP backup only when the phase is high-risk.
11. Create a pre-phase Git checkpoint if possible.

During work:

1. Stay inside the active phase.
2. Do not add unauthorized features.
3. Do not remove working code without backup.
4. Do not hardcode secrets.
5. Do not use deprecated `android.hardware.Camera`.
6. Do not add stealth, hidden icon, SMS, audio recording, overlays, silent uploads, or anti-uninstall tricks.
7. Log deferred issues instead of chasing unrelated fixes.

After work:

1. Run verification commands.
2. Report exact results.
3. Run regression checks from prior phases where possible.
4. Update `PROJECT_STATE.md`.
5. Create post-phase local ZIP backup only when the phase is high-risk.
6. Create post-phase Git checkpoint if possible.
7. Produce the required phase report.
8. Stop.

Truth rule: Never claim a feature works unless verified by command output, test output, emulator/device result, or file evidence.

If something cannot be runtime-tested in the environment, state: "Implemented but not runtime-verified. Requires device/emulator test."

Phase progression rule: Do not proceed to the next phase unless the user authorizes it after reviewing the phase report.

## Operational Summary

LockWitness development is controlled by phase, evidence, backups, and explicit authorization.

Code is not considered complete because it was edited. Work is considered phase-complete only when the phase acceptance criteria are met, verification evidence is reported, `PROJECT_STATE.md` is updated, backups are created, and the required report is delivered.

No Android feature work is authorized during Phase 0 or Phase 0.1.
