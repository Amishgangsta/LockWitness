\# ============================================================

\# LockWitness — Restore codex-control-framework-full.md

\# ============================================================



Set-Location "C:\\Projects\\LockWitness"



New-Item -ItemType Directory -Path ".\\docs" -Force | Out-Null



@'

\# LockWitness Codex Control Framework — Full Reference Doctrine



\## Purpose



This document establishes the full Codex control framework for building LockWitness.



It exists to prevent:



\- Codex drift

\- hallucinated status reports

\- unverified completion claims

\- scope creep

\- destructive changes

\- skipped verification

\- missing backups

\- loss of project direction between phases



LockWitness must be built under a phase-gated, verification-first, no-drift development protocol.



\---



\## LockWitness Codex Control Directive



```text

PROJECT: LockWitness

BUILD MODE: Phase-gated, verification-first, no-drift development.



Primary objective:

Build a polished Android Kotlin Jetpack Compose app named LockWitness that records failed unlock/access attempts on the user’s own Android device, captures configurable evidence, stores it locally, and prepares for monetization.



Non-negotiable product direction:

LockWitness is an owner-controlled, local-first failed-unlock evidence recorder.

It is not spyware.

It is not a stealth app.

It is not a hidden camera app.

It must not use deceptive, stalkerware, or covert-surveillance patterns.



Core feature model:

Each feature must be independently switchable:

1\. Master monitoring toggle

2\. Photo capture toggle

3\. Short video toggle + duration selector

4\. GPS/location toggle

5\. Local timeline toggle

6\. Email alert toggle

7\. Share alert toggle

8\. Manual export action

9\. SHA-256 hashing / evidence integrity mode



Absolute constraints:

\- Do not use deprecated android.hardware.Camera.

\- Do not hardcode credentials, API keys, emails, tokens, SMTP passwords, or secrets.

\- Do not silently upload, email, share, or transmit captured media.

\- Do not add SMS, call-log, contacts, microphone/audio recording, overlay, accessibility-service abuse, hidden icon, stealth mode, or anti-uninstall tricks unless specifically authorized in a later phase.

\- Do not add features outside the active phase.

\- Do not claim a feature works unless it has been verified by build/test/log/output evidence.

\- Do not mark a phase complete if acceptance criteria are unmet.

\- Do not fabricate test results.

\- Do not infer success from compilation alone.

\- Do not delete working code without first creating a backup and explaining why deletion is necessary.



Development discipline:

Work in numbered phases only.



At the start of every phase:

1\. Restate the active phase goal.

2\. Restate the project goal.

3\. List in-scope tasks.

4\. List out-of-scope tasks.

5\. Inspect current repository state.

6\. Identify current branch/commit.

7\. Create a backup before modifying code.



At the end of every phase:

1\. Run all required verification commands.

2\. Report exact results.

3\. List changed files.

4\. List created files.

5\. List deleted files.

6\. State what was verified.

7\. State what was not verified.

8\. State known defects/blockers.

9\. Create a backup archive of the complete project.

10\. Create a checkpoint commit if Git is available.

11\. Refresh the build state summary.

12\. Stop and wait for instruction before proceeding to the next phase.

