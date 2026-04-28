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
7. Create a Git checkpoint commit if repository state has changed.
8. Create a local ZIP backup only when the phase is high-risk.

After modifying code:
1. Run verification commands.
2. Report exact results.
3. Update `PROJECT_STATE.md`.
4. Create a local post-phase ZIP backup only when the phase is high-risk.
5. Create a git checkpoint commit if possible.
6. Stop and wait for authorization.

## Reduced Backup Policy

- Do not commit backup ZIP files.
- Keep backup ZIP files local only.
- Routine phases require Git checkpoint commits, not ZIP backups.
- Create local ZIP backups only before and after high-risk phases, release candidates, dependency upgrades, or major refactors.
- High-risk phases include Device Admin, camera, video, billing, release candidate, and major build-system changes.
- `PROJECT_STATE.md` must record the commit hash and whether a local backup was created.
- If no backup ZIP is created, the phase report must say: “No ZIP backup created under reduced backup policy.”

## Current Authorized Phase

Phase 0 only.

No Android feature work is authorized until Phase 0 is completed and reviewed.
