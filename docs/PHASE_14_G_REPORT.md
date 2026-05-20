# Phase 14-G Report — Android Share/Chooser Verification

## Phase Summary
Verified the in-app Send button launches the Android share chooser and correctly updates the incident record. Share alert toggle was enabled in Settings (DataStore confirmed), a fresh failed-unlock incident was created with `shareEnabled=true`, the Send button was tapped on incident 22, and the Android chooser appeared. Post-send DB query confirms `shareStatus=SUCCESS` written to incident 22, with `photoPath`, `imageSha256`, `photoStatus`, and all other fields intact. No source code changes.

## Starting Commit
`3b08364` — checkpoint: after phase 14-F

## Ending Commit
See post-phase commit below.

## ZIP Backup
No ZIP backup created under reduced backup policy.

---

## Files Inspected
- `CLAUDE.md`, `PROJECT_STATE.md`, `AGENTS.md`
- `docs/codex-control-framework-full.md`
- `android/app/src/main/java/com/lockwitness/app/alert/AlertShareIntentBuilder.kt`
- `android/app/src/main/java/com/lockwitness/app/alert/AlertIncidentUpdater.kt`
- `android/app/src/main/java/com/lockwitness/app/ui/screens/HistoryScreen.kt`

## Files Changed
None. Runtime-only verification phase.

---

## Build Commands Run
None (no source changes; existing APK from Phase 14-E used).

## Test Commands Run
None (runtime-verification phase).

---

## Runtime Evidence

### Share toggle enabled in Settings
DataStore `lockwitness_settings.preferences_pb` xxd confirmed:
```
00000040: 010a 190a 1373 6861 7265 5f61 6c65 7274  .....share_alert
00000050: 5f65 6e61 626c 6564 1202 0801            _enabled....
```
`share_alert_enabled = 0x01` (true).

### Fresh incidents created with shareEnabled=true
Three failed-unlock attempts created incidents 20–22. Pre-send sqlite3 query:
```
20|FAILED_UNLOCK|1|1|SUCCESS|UNAVAILABLE|Incident shell created from failed unlock event.
Location unavailable: No last known location available.
Share alert requires user-initiated chooser action; no automatic share was sent.

21|FAILED_UNLOCK|2|1|SUCCESS|UNAVAILABLE|...
22|FAILED_UNLOCK|3|1|SUCCESS|UNAVAILABLE|...
```
All three: `shareEnabled=1`, `photoStatus=SUCCESS`. Note confirms share alert path ran at capture time (no automatic send).

### Android chooser launched
User tapped Send on incident 22 (detail card). User confirmed: "another menu popped up asking who to send to" — Android share chooser appeared without crash.

### Post-send incident 22 record — full field check
```
sqlite3: SELECT id,shareEnabled,photoStatus,photoPath,imageSha256,shareStatus,notes FROM security_incidents WHERE id=22;
```
Result:
```
id:          22
shareEnabled: 1
photoStatus: SUCCESS
photoPath:   /data/user/0/com.lockwitness.app/files/incident_photos/incident_1779274987038.jpg
imageSha256: d97ef57e07ff717b33f99eab5159bf3fcf3f4c3016227c23fd5e00d7558abe10
shareStatus: SUCCESS
notes:       Incident shell created from failed unlock event.
             Location unavailable: No last known location available.
             Share alert requires user-initiated chooser action; no automatic share was sent.
             Manual user-controlled share/email chooser launched.
```

`shareStatus` updated to `SUCCESS`. `photoPath`, `imageSha256`, `photoStatus` all unchanged. Notes correctly appended "Manual user-controlled share/email chooser launched." without overwriting prior notes.

---

## Acceptance Criteria

| Criterion | Status | Evidence |
|---|---|---|
| Share toggle enabled in Settings | PASS | DataStore: `share_alert_enabled=0x01` |
| Fresh incident created with `shareEnabled=true` | PASS | sqlite3: incidents 20–22 have `shareEnabled=1` |
| `photoStatus=SUCCESS` on new incident | PASS | sqlite3: incident 22 `photoStatus=SUCCESS` |
| Send button active on incident detail | PASS | User tapped Send without being blocked by disabled state |
| Android chooser launched without crash | PASS | User: "another menu popped up asking who to send to" |
| `shareStatus=SUCCESS` written to Room | PASS | sqlite3: incident 22 `shareStatus=SUCCESS` post-send |
| `photoPath`, `imageSha256`, `photoStatus` unchanged after send | PASS | sqlite3: all three fields intact; notes appended, not overwritten |

All acceptance criteria: **PASS**.

---

## Deferred Issues
- **Actual share destination:** User saw the chooser but did not complete a send to any destination — verifying the chooser itself was the goal of this phase. The downstream behavior (Gmail, Files, etc.) is outside LockWitness scope.
- **Email alert path:** `emailEnabled=false` on all incidents tested. The email-specific code path (`emailStatus=SUCCESS`) was not runtime-verified in this phase.
- **Video export/share:** No video files exist in any incident. The video share path remains unverified.

## Recommended Next Phase
Phase 14-H — Diagnostics screen runtime verification: open the Diagnostics screen, verify all status checks render correctly (Device Admin active, camera granted, location granted, Pro mode), and confirm the manual test-photo action runs without crash.
