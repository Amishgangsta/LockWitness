# Phase 14-F Report — Manual Export ZIP Verification

## Phase Summary
Verified the in-app Export All action on a physical device. The user navigated to the History screen and tapped Export All. A 41 MB ZIP was created in app-private storage (`files/exports/`), pulled via `adb exec-out`, unzipped, and fully verified: structure correct, all 13 photo files present, SHA-256 hashes in `hashes.txt` match actual file hashes for all 13 photos, `metadata.json` valid JSON with correct `incidentCount`, `incidents.csv` headers and data correct. No source code changes.

## Starting Commit
`a8ef622` — checkpoint: after phase 14-E

## Ending Commit
See post-phase commit below.

## ZIP Backup
No ZIP backup created under reduced backup policy.

---

## Files Inspected
- `CLAUDE.md`, `PROJECT_STATE.md`, `AGENTS.md`
- `docs/codex-control-framework-full.md`
- `android/app/src/main/java/com/lockwitness/app/export/LocalIncidentExporter.kt`
- `android/app/src/main/java/com/lockwitness/app/export/IncidentExportFormatter.kt`

## Files Changed
None. Runtime-only verification phase.

---

## Build Commands Run
None (no source changes; existing APK from Phase 14-E used).

## Test Commands Run
None (runtime-verification phase).

---

## Runtime Evidence

### Export ZIP created on device
```
adb shell run-as com.lockwitness.app ls -la files/exports/
```
Output:
```
-rw------- 1 u0_a35 u0_a35 41009270 2026-05-20 06:49 lockwitness_all_incidents_1779274145487.zip
```
41,009,270 bytes. Created at 06:49 by user tapping Export All in the History screen.

### ZIP pulled cleanly via adb exec-out
```
adb exec-out "run-as com.lockwitness.app cat files/exports/lockwitness_all_incidents_1779274145487.zip" > /tmp/lw_export_14f.zip
ls -la /tmp/lw_export_14f.zip
```
Output: `-rw-r--r-- 1 Randy 197609 41009270` — exact size match; no truncation.

### ZIP structure
```
unzip -o /tmp/lw_export_14f.zip
```
Top-level entries:
- `metadata.json` (19,133 bytes)
- `incidents.csv` (6,791 bytes)
- `hashes.txt` (2,007 bytes)
- `photos/` — 13 JPEG files (incidents 7–19)

No `videos/` directory (correct — no video captures exist).

### Photo files in ZIP
```
ls -la photos/
```
13 files, one per incident with `photoStatus=SUCCESS` (incidents 7–19):
```
incident_7_incident_1779246097329.jpg   3,221,553 bytes
incident_8_incident_1779246098746.jpg   3,254,138 bytes
incident_9_incident_1779246105593.jpg   3,232,214 bytes
incident_10_incident_1779246113699.jpg  3,343,213 bytes
incident_11_incident_1779246804500.jpg  2,990,309 bytes
incident_12_incident_1779246807425.jpg  2,988,747 bytes
incident_13_incident_1779246811043.jpg  2,987,202 bytes
incident_14_incident_1779247165756.jpg  3,305,942 bytes
incident_15_incident_1779247168328.jpg  3,045,534 bytes
incident_16_incident_1779247171459.jpg  3,156,968 bytes
incident_17_incident_1779248124878.jpg  3,155,180 bytes
incident_18_incident_1779248127575.jpg  3,146,576 bytes
incident_19_incident_1779248130493.jpg  3,241,658 bytes
```
Incidents 1–6 (all `photoStatus=FAILED`) correctly have no photo entry in the ZIP.

### metadata.json
Valid JSON. Key fields:
- `exportVersion: 1`
- `incidentCount: 19` — matches DB count
- `missingMediaFiles: []` — no missing files
- All 19 incidents present with correct fields (`triggerType`, `photoStatus`, `imageSha256`, etc.)

### incidents.csv
Header row:
```
id,timestamp,triggerType,failedAttemptCount,deviceModel,androidVersion,appVersion,photoPath,
videoPath,latitude,longitude,locationAccuracy,locationProvider,imageSha256,videoSha256,
photoStatus,videoStatus,locationStatus,emailStatus,shareStatus,notes
```
19 data rows. Incident 19 sample:
```
"19","1779248130475","FAILED_UNLOCK","3","samsung SM-G973U1","12","0.1.0",
"/data/user/0/com.lockwitness.app/files/incident_photos/incident_1779248130493.jpg",
"","","","","","d55c009f465e0c64ba931f19e5404c314edba8559ee49d3565e926244e78fcbd","",
"SUCCESS","DISABLED","UNAVAILABLE","DISABLED","DISABLED",
"Incident shell created from failed unlock event.\nLocation unavailable: ..."
```

### SHA-256 verification — all 13 photos
`sha256sum` computed on all 13 extracted photo files and compared against `hashes.txt`:

| Incident | hashes.txt value | sha256sum result | Match |
|----------|-----------------|------------------|-------|
| 7 | `2a3c119c...` | `2a3c119c...` | PASS |
| 8 | `10554449...` | `10554449...` | PASS |
| 9 | `ff5cfa96...` | `ff5cfa96...` | PASS |
| 10 | `fb693145...` | `fb693145...` | PASS |
| 11 | `b2dddaf4...` | `b2dddaf4...` | PASS |
| 12 | `a317acb8...` | `a317acb8...` | PASS |
| 13 | `6d667805...` | `6d667805...` | PASS |
| 14 | `7e91b8f6...` | `7e91b8f6...` | PASS |
| 15 | `94fc6646...` | `94fc6646...` | PASS |
| 16 | `fd7fc399...` | `fd7fc399...` | PASS |
| 17 | `36c296ce...` | `36c296ce...` | PASS |
| 18 | `e6f9295a...` | `e6f9295a...` | PASS |
| 19 | `d55c009f...` | `d55c009f...` | PASS |

All 13 of 13 SHA-256 values match. Evidence integrity confirmed.

---

## Acceptance Criteria

| Criterion | Status | Evidence |
|---|---|---|
| Export action completes without crashing | PASS | User confirmed; ZIP file created at 06:49 |
| ZIP file exists in `files/exports/` | PASS | `ls -la`: `lockwitness_all_incidents_1779274145487.zip`, 41,009,270 bytes |
| ZIP contains `metadata.json` | PASS | `unzip` listing confirms entry; 19,133 bytes |
| ZIP contains `incidents.csv` | PASS | `unzip` listing confirms entry; 6,791 bytes |
| ZIP contains `hashes.txt` | PASS | `unzip` listing confirms entry; 2,007 bytes |
| `metadata.json` valid JSON with correct `incidentCount` | PASS | `incidentCount: 19` matches DB |
| `incidents.csv` has correct headers and data rows | PASS | 21 columns, 19 data rows confirmed |
| At least one photo file in `photos/` subdirectory | PASS | 13 photo files present |
| SHA-256 values in `hashes.txt` match actual file hashes | PASS | All 13 of 13 match via `sha256sum` |

All acceptance criteria: **PASS**.

---

## Deferred Issues
- **Video export:** No video files were present to include. Video capture runtime verification remains deferred from prior phases.
- **Individual incident export:** Only Export All was tested. The single-incident export path exists in code but was not runtime-verified in this phase.
- **Share/chooser after export:** The Android share chooser flow (launching an intent from the export result) was not tested in this phase; deferred.
- **`locationStatus=SUCCESS` path in export:** All exported incidents have `locationStatus=UNAVAILABLE` or `DISABLED`. A SUCCESS path with lat/lng populated in the export remains unverified.

## Recommended Next Phase
Phase 14-G — Android share/chooser verification: use the in-app Share action on a single incident export, confirm the Android chooser launches correctly, and verify the local incident record is not modified or destroyed by the share attempt.
