# LockWitness Build Protocol

LockWitness is built under a phase-gated, verification-first Codex protocol.

## Required Codex Behavior

Before modifying code:
1. Read `AGENTS.md`.
2. Read `PROJECT_STATE.md`.
3. Run `git status`.
4. Identify branch and commit.
5. Restate active phase goal.
6. List in-scope and out-of-scope items.
7. Create a pre-phase backup zip.

After modifying code:
1. Run verification commands.
2. Report exact results.
3. Update `PROJECT_STATE.md`.
4. Create post-phase backup zip.
5. Create a git checkpoint commit if possible.
6. Stop and wait for authorization.

## Current Authorized Phase

Phase 0 only.

No Android feature work is authorized until Phase 0 is completed and reviewed.
