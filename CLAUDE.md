# Claude Code Instructions — LockWitness

This file is the entry point for every Claude Code session in this repository. It is subordinate to `docs/codex-control-framework-full.md`, which remains the authoritative control framework. Where this file and the framework appear to disagree, the framework wins.

LockWitness is an owner-controlled Android failed-unlock evidence recorder. It is not spyware, not a stealth app, not a hidden surveillance tool. The product direction is non-negotiable and is defined in the control framework.

---

## Required reading at session start

Before proposing or making any change, read these files in this order:

1. `CLAUDE.md` (this file).
2. `docs/codex-control-framework-full.md` (full authoritative framework — rules, constraints, phase gates).
3. `PROJECT_STATE.md` (current phase, verified vs unverified status, last backup, last verified build).
4. `AGENTS.md` (if present — supplementary agent rules).
5. The phase document for the active phase, if one exists under `docs/PHASE_*.md`.

Restate the active phase, its acceptance criteria, and the specific files you plan to touch back to the user before doing anything. Wait for explicit authorization before proceeding.

---

## Non-negotiable behaviors

These rules apply to every session, every phase, every prompt. They are not subject to "but the user said" exceptions; if the user appears to ask for something that violates them, stop and confirm.

- **Phase-gated work only.** Do not perform any work outside the active phase. If a requested change requires expanding scope, stop and ask for authorization.
- **No claims without evidence.** Forbidden phrases: "this should work," "the feature is implemented successfully," "the app is ready," "everything is wired up," "camera capture works," and any variant. Replace with claims tied to build output, test output, runtime evidence, or file evidence — for example, "Gradle build passed. Evidence: `.\gradlew.bat assembleDebug` completed with exit code 0."
- **No runtime claims from compilation.** If a feature has only been compiled or unit-tested, state explicitly: "Implemented but not runtime-verified. Requires device/emulator test."
- **No fabricated test results.** If a test was not run, do not summarize it as run.
- **No drift.** If you find an unrelated issue, log it under "Deferred Issues" in the phase report. Do not fix it unless it blocks the active phase.
- **No deletion of working code without explicit user authorization and a backup.** If you believe a deletion is needed, propose it, explain why, and wait.
- **No new "agents," diagnostic scripts, or tooling without explicit authorization.** The instrumentation surface should shrink as the app stabilizes, not grow.

---

## Absolute product constraints

The control framework lists these in full. The ones most likely to be relevant in routine work:

- Do not use deprecated `android.hardware.Camera`. Use CameraX or Camera2.
- Do not hardcode credentials, API keys, emails, tokens, SMTP passwords, or any secret.
- Do not silently upload, email, share, or transmit captured media. All transmission is user-initiated.
- Do not add SMS, call-log, contacts, microphone/audio, overlay abuse, accessibility-service abuse, hidden icon, stealth mode, or anti-uninstall behavior.
- Do not introduce any feature outside the active phase's scope.

---

## Repository operational details

**Repository root:** `C:\Projects\LockWitness`
**Android Gradle project:** `C:\Projects\LockWitness\android`
**Backups:** `C:\Projects\LockWitness\backups\` (local-only, never committed)
**Android SDK:** `C:\Users\Randy\AppData\Local\Android\Sdk` (set as `ANDROID_HOME`)

**Build commands (run from `C:\Projects\LockWitness\android`):**

```powershell
.\gradlew.bat assembleDebug         # Debug build
.\gradlew.bat assembleRelease       # Release build (when signing configured)
.\gradlew.bat bundleRelease         # AAB for Play Store
.\gradlew.bat testDebugUnitTest     # Unit tests
.\gradlew.bat lint                  # Lint check
```

**ADB commands (test device must be connected with USB debugging enabled):**

```powershell
adb devices                                              # Confirm device visible
adb install -r app\build\outputs\apk\debug\app-debug.apk # Install APK
adb logcat -c                                            # Clear logcat buffer
adb logcat | findstr LockWitness                         # Filter logs to app
adb shell run-as <package> ls -la files                  # Inspect app-private storage
```

**Git checkpoint pattern (per control framework backup protocol):**

```powershell
git status
git add -A
git commit -m "checkpoint: before phase X - <phase name>"
# work
git add -A
git commit -m "checkpoint: after phase X - <phase name>"
```

Local ZIP backups are required only before and after high-risk phases (Device Admin, camera, video, billing, release candidate, major build-system changes). Routine phases use Git checkpoints only. The phase report must state explicitly whether a ZIP backup was created or note "No ZIP backup created under reduced backup policy."

---

## Device runtime verification — the human/Claude Code split

Beginning with the runtime verification phases (Phase 14 onward), some work cannot be done by Claude Code alone. The split:

**Claude Code performs:**
- Build and install APKs via Gradle and adb
- Launch the app, navigate via `adb shell input` where possible
- Read logcat in real time
- Inspect app-private storage via `run-as`
- Verify file hashes, Room database contents, exported ZIP structure
- Generate phase report with captured evidence

**The user (Randy) performs:**
- Grant Device Admin permission via system UI
- Grant runtime permissions via system prompts (camera, location, etc.)
- Trigger actual failed-unlock events (enter wrong PIN/pattern on the device)
- Take screenshots of UI states for the phase report
- Confirm visual UI behavior that adb cannot reliably introspect

Coordinate explicitly. Tell the user when you need a physical action, name it precisely ("Please enter a wrong PIN three times now and tell me when finished"), then read the resulting state. Never assume a physical action happened — wait for confirmation.

---

## Phase report requirements

Every phase produces a report. The report must contain:

- Phase name and starting commit hash
- Ending commit hash
- Files inspected
- Files changed (must all be within the phase's scope)
- Build commands run, with exit codes
- Test commands run, with results
- Runtime evidence (logcat excerpts, screenshots, file listings, hash values) where applicable
- Whether a ZIP backup was created (path, or "No ZIP backup created under reduced backup policy")
- Acceptance criteria status (each criterion: PASS / FAIL / NOT APPLICABLE with reason)
- Deferred issues discovered but not addressed
- Recommended next phase

Reports are committed under `docs/PHASE_<N>_REPORT.md`. `PROJECT_STATE.md` is updated with the phase outcome.

---

## What to never do without explicit authorization

- Modify `docs/codex-control-framework-full.md`
- Delete or rewrite prior phase reports
- Touch `backups/`
- Touch production Play Console configuration
- Touch production billing or ads configuration
- Initialize Git in a repo that already has Git
- Force-push, rebase, or rewrite Git history
- Run `git clean` or `git reset --hard` against the working tree
- Install dependencies outside the active phase's scope
- Modify keystore files or signing configuration

---

## Change log

| Date | Change | Authorized by |
| --- | --- | --- |
| 2026-05-17 | Initial CLAUDE.md created as Claude Code entry point. Points at existing control framework as authority. | Randy Vickers |
