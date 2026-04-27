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
7. Create a pre-phase backup zip in `backups/`.

During work:
1. Stay inside the active phase.
2. Do not opportunistically refactor unrelated files.
3. Log unrelated issues under “Deferred Issues.”
4. Do not claim success without evidence.

After work:
1. Run build/tests.
2. Report exact commands and results.
3. Update `PROJECT_STATE.md`.
4. Create post-phase backup zip.
5. Create a git checkpoint commit if possible.
6. Stop. Do not proceed to the next phase without user authorization.

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
