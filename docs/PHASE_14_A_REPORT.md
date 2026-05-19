# Phase 14-A Report — Device Connectivity, APK Install, Launch to Dashboard

Phase: 14-A — Device Connectivity + APK Install + Launch to Dashboard
Goal: Confirm physical Android device is reachable via adb, install the current debug APK, launch the app, and verify the dashboard activity reaches the RESUMED state with device evidence.
Branch: main
Starting commit: f65275a (add release candidate control audit)
Ending commit: (see post-phase checkpoint below)
Backup before: No ZIP backup created under reduced backup policy. Git checkpoint commit created.
Backup after: No ZIP backup created under reduced backup policy. Git checkpoint commit created.

---

## In scope

- Running `adb devices` to confirm device RF8M3278JVE is attached and authorized.
- Identifying applicationId from `android/app/build.gradle.kts`.
- Checking for a prior install of `com.lockwitness.app` on the device.
- Building the debug APK via `.\gradlew.bat assembleDebug`.
- Installing the APK via `adb install -r`.
- Launching `com.lockwitness.app/.MainActivity` via `adb shell am start`.
- Collecting logcat and `dumpsys activity activities` evidence.
- Writing this phase report and updating `PROJECT_STATE.md`.

## Out of scope

- Any Android source code changes.
- Device Admin activation.
- Permission grants.
- Failed-unlock flow.
- Any Phase 14-B or later work.

---

## Files changed

- `PROJECT_STATE.md` — updated with Phase 14-A result.

## Files created

- `docs/PHASE_14_A_REPORT.md` — this report.

## Files deleted

- None.

---

## Pre-phase file review

Three untracked files were found in the working tree before this phase began. They were read and assessed but not committed pending user decision:

### `docs/lockwitness-current-roadmap.md`
A comprehensive project roadmap authored by Randy Vickers on 2026-04-28.

**Conflict assessment:** No conflict with the control framework or phase plan. The roadmap is consistent with `PROJECT_STATE.md` and the control framework in all product constraints, feature set, backup policy, and verification requirements. The only discrepancy is naming: the roadmap refers to the next runtime verification phase as "Phase 15," while the user named it "Phase 14-A." That is a user prerogative and not a framework conflict.

### `Codex Lockwitness prompt.txt`
Single line: the complete Codex master prompt from the control framework. An informational/historical copy of the agent control rules. No conflict.

### `docs/New Text Document.txt`
Empty file (1 line, no content).

**User decision on these files is pending. They have not been committed.**

---

## Preflight steps performed

### Step 1 — `adb devices`

Command: `adb devices`

Result:
```
List of devices attached
RF8M3278JVE	device
```

Evidence: RF8M3278JVE is present and in authorized (`device`) state.

### Step 2 — applicationId

Source file: `android/app/build.gradle.kts`, line 13.

Result: `applicationId = "com.lockwitness.app"`

### Step 3 — Prior install check

Command: `adb shell pm list packages | grep lockwitness`

Result: No output — `com.lockwitness.app` was NOT installed on the device.

Prior install found: **No.** No uninstall was performed. Clean slate confirmed.

### Step 4 — Debug APK build

Command: `.\gradlew.bat assembleDebug` (from `C:\Projects\LockWitness\android`)

Result:
```
BUILD SUCCESSFUL in 1m
37 actionable tasks: 37 up-to-date
```

Exit code: 0. All tasks up-to-date. APK located at `android/app/build/outputs/apk/debug/app-debug.apk`.

### Step 5 — APK install

Command: `adb install -r app\build\outputs\apk\debug\app-debug.apk`

Result:
```
Performing Streamed Install
Success
```

### Step 6 — App launch

Logcat cleared before launch: `adb logcat -c`

Command: `adb shell am start -n com.lockwitness.app/.MainActivity`

Result:
```
Starting: Intent { cmp=com.lockwitness.app/.MainActivity }
```

### Step 7 — Dashboard confirmation

#### Logcat evidence (ActivityTaskManager)

```
05-19 15:25:11.268  1145  5298  I ActivityTaskManager: START u0 {flg=0x10000000 cmp=com.lockwitness.app/.MainActivity} from uid 2000
05-19 15:25:12.365  1145  1446  I ActivityTaskManager: Displayed com.lockwitness.app/.MainActivity: +1s93ms
```

Interpretation: The system recorded a `START` intent for `MainActivity` and then a `Displayed` event after 1.093 seconds, confirming the activity rendered on screen.

#### Additional logcat evidence (WindowManager / SurfaceFlinger)

```
05-19 15:25:12.424  1145  5298  V WindowManager: Relayout Window{a5b1d95 u0 Splash Screen com.lockwitness.app}
05-19 15:25:12.511   946   946  D SurfaceFlinger: com.lockwitness.app/com.lockwitness.app.MainActivity$_3310#0
05-19 15:25:12.562  1145  2047  D MdnieScenarioControlService: packageName : com.lockwitness.app  className : com.lockwitness.app.MainActivity
05-19 15:25:17.738  3310  3366  D ProfileInstaller: Installing profile for com.lockwitness.app
```

App process PID: 3310.

#### `dumpsys activity activities` evidence (key lines)

```
mResumedActivity: ActivityRecord{c0e6b20 u0 com.lockwitness.app/.MainActivity t12663}

* Task{1878cd9 #12663 type=standard A=10035:com.lockwitness.app visible=true mode=fullscreen}
  mActivityComponent=com.lockwitness.app/.MainActivity
  mRootProcess=ProcessRecord{550da9b 3310:com.lockwitness.app/u0a35}
  baseDir=/data/app/.../com.lockwitness.app-A5DhBQ7Ew01Jxqgl_C-BAg==/base.apk
  dataDir=/data/user/0/com.lockwitness.app

Resumed: ActivityRecord{c0e6b20 u0 com.lockwitness.app/.MainActivity t12663}
ResumedActivity: ActivityRecord{c0e6b20 u0 com.lockwitness.app/.MainActivity t12663}
mCurrentFocus=Window{ee8504b u0 com.lockwitness.app/com.lockwitness.app.MainActivity}
mFocusedApp=ActivityRecord{c0e6b20 u0 com.lockwitness.app/.MainActivity t12663}
topDisplayFocusedRootTask=Task{1878cd9 #12663 type=standard A=10035:com.lockwitness.app visible=true}
```

Interpretation: `MainActivity` is RESUMED, on top, visible, full-screen, and holds input focus. This is the state of a fully launched foreground activity.

---

## Verification performed

| Command | Result | Exit code |
|---|---|---|
| `adb devices` | RF8M3278JVE device | 0 |
| `adb shell pm list packages \| grep lockwitness` | (no output — not installed) | 0 |
| `.\gradlew.bat assembleDebug` | BUILD SUCCESSFUL, 37 tasks up-to-date | 0 |
| `adb install -r app-debug.apk` | Performing Streamed Install / Success | 0 |
| `adb shell am start -n com.lockwitness.app/.MainActivity` | Starting: Intent { cmp=com.lockwitness.app/.MainActivity } | 0 |
| `adb logcat -d` (filtered) | Displayed +1s93ms; SurfaceFlinger active; ProfileInstaller fired | — |
| `adb shell dumpsys activity activities` (filtered) | mResumedActivity / Resumed / mCurrentFocus all = MainActivity | 0 |

---

## Acceptance criteria

[x] RF8M3278JVE present and authorized in `adb devices`
[x] applicationId identified from Gradle config: `com.lockwitness.app`
[x] Prior install check performed; result reported (no prior install found)
[x] `.\gradlew.bat assembleDebug` passed with exit code 0
[x] `adb install -r` succeeded
[x] `adb shell am start` launched without error
[x] `ActivityTaskManager: Displayed com.lockwitness.app/.MainActivity` present in logcat
[x] `dumpsys activity activities` shows `mResumedActivity` = `com.lockwitness.app/.MainActivity`
[x] Activity is in RESUMED state, visible, fullscreen, with input focus

---

## Verified working

- Physical device RF8M3278JVE is adb-reachable and authorized.
- Debug APK builds successfully (37 tasks, exit code 0).
- APK installs cleanly from cold (no prior install).
- App process launches and `MainActivity` reaches RESUMED state.
- Dashboard activity is the top focused activity with input focus.
- Evidence: `ActivityTaskManager: Displayed com.lockwitness.app/.MainActivity: +1s93ms` and `dumpsys activity activities` output above.

## Not verified

- Visual appearance of the dashboard UI (no screenshot taken; physical device required for visual confirmation).
- Navigation between screens.
- Any feature-level behavior (Device Admin, camera, location, history, export, etc.).
- These are out of scope for Phase 14-A.

## Known defects

None discovered during Phase 14-A.

## Deferred issues

- `docs/lockwitness-current-roadmap.md`, `Codex Lockwitness prompt.txt`, and `docs/New Text Document.txt` are untracked files in the working tree. User decision on keep/commit/delete is pending.

## Security/privacy review

No Android source files were modified. No new permissions, network calls, credentials, or transmission paths were introduced. This phase was read-only with respect to app source code.

## Play Store risk review

No changes to app source, manifest, or assets. No Play Store risk introduced.

## Next recommended phase

Phase 14-B — Device Admin activation verification (physical device, user action required to tap "Activate" in system UI).

## STOP STATUS

STOPPED. All Phase 14-A acceptance criteria met. Awaiting user authorization before proceeding to Phase 14-B.
