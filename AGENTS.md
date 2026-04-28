# LockWitness Codex Rules

## Product Goal
Build LockWitness: an Android Kotlin Jetpack Compose app that records failed unlock/access attempts on the owner’s own device and preserves configurable evidence.

## Core Features
Each feature must be independently switchable:
- Master monitoring
- Photo capture
- Short video capture: 5/10/15/30 seconds
- GPS/location capture
- Local incident timeline
- Email alert
- Share alert
- Manual export ZIP/PDF/CSV
- SHA-256 evidence hashing

## Non-Negotiable Constraints
- Kotlin + Jetpack Compose.
- Use CameraX or Camera2.
- Do not use deprecated `android.hardware.Camera`.
- Do not hardcode secrets, emails, API keys, tokens, or SMTP credentials.
- Do not silently upload, email, share, or transmit captured media.
- No hidden icon.
- No stealth mode.
- No SMS.
- No audio recording.
- No overlay abuse.
- No accessibility-service abuse.
- No anti-uninstall tricks.
- No feature outside the active phase.

## Codex Anti-Drift Rules
Before modifying code:
1. Read this `AGENTS.md`.
2. Read `PROJECT_STATE.md` if it exists.
3. Run `git status`.
4. Identify branch and commit.
5. Restate the active phase goal.
6. List in-scope and out-of-scope work.
7. Create a Git checkpoint commit if repository state has changed.
8. Create a local ZIP backup only when the phase is high-risk.

During work:
1. Stay inside the active phase.
2. Do not opportunistically refactor unrelated files.
3. Log unrelated issues under “Deferred Issues.”
4. Do not claim success without evidence.

After work:
1. Run build/tests.
2. Report exact commands and results.
3. Update `PROJECT_STATE.md`.
4. Create a local post-phase ZIP backup only when the phase is high-risk.
5. Create a git checkpoint commit if possible.
6. Stop. Do not proceed to the next phase without user authorization.

## Backup Policy
Backup ZIP archives are local-only safeguards and must not be committed to Git.

Routine phases require Git checkpoint commits, not ZIP backups.

Create local ZIP backups only before and after high-risk phases, release candidates, dependency upgrades, or major refactors. High-risk phases include Device Admin, camera, video, billing, release candidate, and major build-system changes.

`PROJECT_STATE.md` must record the relevant commit hash and whether a local ZIP backup was created. If no backup ZIP is created, the phase report must say: “No ZIP backup created under reduced backup policy.”

## Truth Rule
Never say “works,” “complete,” “ready,” or “verified” unless supported by build output, test output, runtime/device evidence, or file evidence.

If runtime testing is not possible, say:
“Implemented but not runtime-verified.”

## Failure Rule
Optional modules must fail independently.
A failed photo/video/location/email/share/export module must not destroy the incident record.

## Phase Report Required
Every phase must end with:

Phase:
Goal:
Branch:
Starting commit:
Ending commit:
Backup before:
Backup after:
Files changed:
Files created:
Files deleted:
Verification commands:
Verification results:
Acceptance criteria:
Verified working:
Not verified:
Known defects:
Deferred issues:
Next recommended phase:
STOP STATUS:
