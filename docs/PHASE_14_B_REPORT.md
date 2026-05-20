# Phase 14-B Report — Device Admin Activation + Master Monitoring Toggle Persistence

## Phase Summary
Verified Device Admin activation flow from within the app UI, confirmed Device Admin active via `dumpsys device_policy`, and verified that the master monitoring toggle persists its ON and OFF states across force-stop/relaunch cycles via DataStore.

## Starting Commit
`5398410` — cleanup: track CLAUDE.md, gitignore .claude/, remove empty root placeholder

## Ending Commit
`5398410` — no source code changes; same commit as start.

## ZIP Backup
No ZIP backup created under reduced backup policy. (Phase 14-B is a runtime-verification-only pass with no source changes.)

## Files Inspected
- `CLAUDE.md`
- `PROJECT_STATE.md`
- `android/app/src/main/java/com/lockwitness/app/admin/DeviceAdminStatus.kt`
- `android/app/src/main/java/com/lockwitness/app/data/SettingsRepository.kt`
- `android/app/src/main/java/com/lockwitness/app/ui/screens/SettingsScreen.kt`

## Files Changed
- `docs/PHASE_14_B_REPORT.md` (this file — new)
- `PROJECT_STATE.md` (phase record appended)

No Android source files were modified.

## Build Commands Run
None. The debug APK installed in Phase 14-A was used without rebuild. No source changes occurred that would require a new build.

## Test Commands Run
None. This is a runtime-verification-only phase; unit tests were last verified in Phase 14-A / Phase 14.

## Runtime Evidence

### Step 1 — Device Admin activation from app UI
User navigated to the Settings screen via the bottom navigation bar. The **Device Admin** card showed status "inactive" with an "Activate" button. User tapped "Activate," which launched `DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN` with explanation text. User granted Device Admin via the system prompt. The Settings screen updated to show "Status: active."

### Step 2 — Device Admin active verification
```
adb shell dumpsys device_policy | grep -i lockwitness
```
Output:
```
    com.lockwitness.app/.admin.LockWitnessDeviceAdminReceiver:
```
Result: Device Admin component confirmed active in device policy manager.

### Step 3 — Master monitoring toggled ON
User tapped the Master monitoring toggle on the Settings screen. Toggle confirmed ON by user ("toggled on").

### Step 4 — Force-stop, relaunch, verify ON persists
```
adb shell am force-stop com.lockwitness.app
adb shell am start -n com.lockwitness.app/.MainActivity
adb shell run-as com.lockwitness.app xxd ./files/datastore/lockwitness_settings.preferences_pb
```
Output:
```
00000000: 0a1f 0a19 6d61 7374 6572 5f6d 6f6e 6974  ....master_monit
00000010: 6f72 696e 675f 656e 6162 6c65 6412 0208  oring_enabled...
00000020: 01                                       .
```
Interpretation: Key `master_monitoring_enabled`, protobuf boolean value `0x01` = **true**. Persistence of ON state confirmed.

### Step 5 — Master monitoring toggled OFF
User tapped the Master monitoring toggle to turn it OFF. Toggle confirmed OFF by user ("toggled off").

### Step 6 — Force-stop, relaunch, verify OFF persists
```
adb shell am force-stop com.lockwitness.app
adb shell am start -n com.lockwitness.app/.MainActivity
adb shell run-as com.lockwitness.app xxd ./files/datastore/lockwitness_settings.preferences_pb
```
Output:
```
00000000: 0a1f 0a19 6d61 7374 6572 5f6d 6f6e 6974  ....master_monit
00000010: 6f72 696e 675f 656e 6162 6c65 6412 0208  oring_enabled...
00000020: 00                                       .
```
Interpretation: Key `master_monitoring_enabled`, protobuf boolean value `0x00` = **false**. Persistence of OFF state confirmed.

## Acceptance Criteria

| Criterion | Status | Evidence |
|---|---|---|
| Device Admin activation flow triggered from app UI | PASS | User tapped "Activate" button on Settings screen; system Device Admin dialog launched |
| User granted Device Admin via system prompt | PASS | User confirmed; Settings screen updated to "Status: active" |
| Device Admin verified active via `dumpsys device_policy` | PASS | `com.lockwitness.app/.admin.LockWitnessDeviceAdminReceiver` present in dumpsys output |
| Master monitoring toggled ON from app UI | PASS | User confirmed "toggled on" |
| Master monitoring ON persists across force-stop/relaunch | PASS | DataStore xxd shows `master_monitoring_enabled` = `0x01` after relaunch |
| Master monitoring toggled OFF from app UI | PASS | User confirmed "toggled off" |
| Master monitoring OFF persists across force-stop/relaunch | PASS | DataStore xxd shows `master_monitoring_enabled` = `0x00` after relaunch |

All acceptance criteria: **PASS**.

## Deferred Issues
None discovered during this phase.

## Recommended Next Phase
Phase 14-C — Failed-unlock callback: trigger actual failed unlock events on-device and verify that a SecurityIncident record is created in the Room database.
