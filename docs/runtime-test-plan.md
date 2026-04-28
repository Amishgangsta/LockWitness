# LockWitness Runtime Test Plan

This plan must be run on a physical Android device or emulator. Command-line build success is not runtime verification.

## Test Setup

- Install the current release-candidate APK.
- Confirm the installed build matches the intended commit.
- Keep logcat running during tests.
- Use synthetic incidents and screenshots only; do not use another person's private data.

## Required Runtime Tests

### Launch And Navigation

- Launch LockWitness.
- Confirm dashboard is visible.
- Navigate to Settings, History, Diagnostics, and About.
- Confirm no crash occurs.

Evidence to record:

- Device/emulator model.
- Android version.
- Installed APK version.
- Screenshot or screen recording.
- Logcat summary.

### Device Admin

- Open Device Admin onboarding/status.
- Activate Device Admin from the Android system flow.
- Return to LockWitness.
- Confirm the app shows active status.

Evidence to record:

- Screenshot of active status.
- Logcat summary.

### Failed Unlock

- Enable Master Monitoring.
- Lock the device.
- Enter an incorrect unlock credential.
- Unlock successfully.
- Open LockWitness History.
- Confirm a new incident exists.

Evidence to record:

- Incident timestamp.
- Trigger type.
- Failed attempt count if available.
- Logcat summary.

### Photo Capture

- Grant camera permission.
- Enable photo capture.
- Trigger manual diagnostics or failed unlock flow.
- Confirm photo status is recorded.
- Confirm photo path and hash are present on success, or failure is recorded without deleting the incident.

Evidence to record:

- Permission state.
- Incident detail status.
- Photo path/hash or failure note.
- Logcat summary.

### Video Capture

- Confirm video is allowed by current Free/Pro state.
- Enable video capture and select each supported duration where feasible: 5, 10, 15, and 30 seconds.
- Trigger manual diagnostics or failed unlock flow.
- Confirm video status is recorded.
- Confirm video path and hash are present on success, or failure is recorded without deleting the incident.
- Confirm no audio permission prompt appears.

Evidence to record:

- Selected duration.
- Incident detail status.
- Video path/hash or failure note.
- Logcat summary.

### Location Snapshot

- Confirm location is allowed by current Free/Pro state.
- Grant location permission.
- Enable location capture.
- Trigger manual diagnostics or failed unlock flow.
- Confirm location status is recorded.
- Confirm latitude, longitude, accuracy, and provider are present on success, or unavailable/failure status is recorded without deleting the incident.

Evidence to record:

- Permission state.
- Provider/service state.
- Incident detail location fields or failure note.
- Logcat summary.

### History And Detail

- Open History.
- Confirm incidents are listed newest first.
- Open a detail screen.
- Confirm metadata, statuses, paths, hashes, notes, and device fields display.
- Delete a single incident.
- Clear all incidents.

Evidence to record:

- Screenshots before and after delete/clear.
- Logcat summary.

### Export ZIP

- Trigger manual export.
- Confirm the ZIP package is created.
- Inspect ZIP contents for `metadata.json`, `incidents.csv`, `hashes.txt`, and available media files.
- Confirm missing media is handled gracefully if applicable.

Evidence to record:

- Export file path.
- ZIP content listing.
- Logcat summary.

### Android Chooser And Share

- Trigger the user-initiated share/chooser flow.
- Confirm Android chooser appears.
- Cancel without sending, or send to a controlled test destination.
- Confirm no preset destination is used.

Evidence to record:

- Screenshot of chooser.
- Export/share status in incident detail if applicable.
- Logcat summary.

### Diagnostics

- Open Diagnostics.
- Confirm Device Admin, camera permission, location permission, toggles, timeline/history, export availability, share chooser, Free/Pro mode, app version, Android version, and device model are displayed.
- Run manual diagnostic actions only through visible buttons.

Evidence to record:

- Screenshot of Diagnostics.
- Action result text.
- Logcat summary.

## Pass Criteria

Runtime testing passes only when every required test has device/emulator evidence and no unresolved crash, data-loss, silent-transmission, or policy-risk issue remains.
