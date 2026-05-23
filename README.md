# LockWitness

**Owner-controlled Android failed-unlock evidence recorder.**  
Built for individuals who need tamper-evident, locally stored proof of unauthorized access attempts.

---

## What It Does

LockWitness runs as a Device Admin on your Android phone. When someone enters a wrong PIN, password, or pattern, the app silently records evidence to your device — photo, video, GPS coordinates, and a full audit trail. Everything stays local until *you* choose to export or share it.

> **Note:** Face and fingerprint unlock failures are not captured. Android does not expose biometric failure events to third-party apps. LockWitness monitors PIN, password, and pattern failures only.

---

## Verified Features (Phase 17 — Release Build)

All features below are device-verified on SM-G973U1, Android 12, unless noted as build-verified only.

| Feature | Status | Notes |
|---|---|---|
| Device Admin monitoring | Device-verified | `watch-login` policy; `onPasswordFailed` callback confirmed |
| Front-camera photo capture | Device-verified | Camera2; foreground service; 3–3.3 MB JPEGs; SHA-256 hashed |
| Front-camera video capture | Device-verified | Camera2; no audio; 3.1 MB MP4 confirmed via Diagnostics |
| GPS location snapshot | Device-verified | LocationManager; SUCCESS confirmed with warm GPS cache |
| Local incident history | Build-verified | Room database; timestamp-desc order; swipe-to-delete |
| Incident detail view | Build-verified | Trigger metadata, file paths, SHA-256 hashes, device info |
| SHA-256 integrity hashing | Device-verified | All photo hashes verified against exported `hashes.txt` |
| ZIP evidence export | Device-verified | 41 MB ZIP; metadata.json, incidents.csv, hashes.txt, photos/ |
| Android share/chooser | Device-verified | Share intent; `shareStatus=SUCCESS` written to Room |
| Settings toggles | Device-verified | DataStore persistence across force-stop/relaunch confirmed |
| Diagnostics screen | Device-verified | All 14 checks rendered; Photo/Video/Location/Export all PASS |
| Master monitoring toggle | Device-verified | Enable/disable persists correctly |
| Email/share alert toggle | Build-verified | User-initiated chooser only; no auto-transmission |
| Free/Pro feature gate | Build-verified | ProFeatureGate; billing fallback to base mode |
| Upgrade / Pro purchase UI | Build-verified | Play Billing v7.1.1; production config pending Play Console |

---

## Pricing

**7-day free trial** at base level. No permanent free tier.

| Plan | Price |
|---|---|
| Monthly | $2.99 / month (auto-renewing) |
| Annual | $19.99 / year (auto-renewing; ~44% savings) |
| Lifetime | $39.99 one-time |

Trial includes: failed-unlock monitoring, photo capture, last 10 incidents, basic diagnostics.  
Pro adds: full history, video capture, GPS snapshots, ZIP export, advanced diagnostics, ad-free.

---

## Architecture

| Layer | Technology |
|---|---|
| UI | Jetpack Compose (Material 3), HorizontalPager swipe navigation |
| Navigation | Jetpack Navigation + HorizontalPager for main tabs |
| Persistence | Room (incidents) + DataStore Preferences (settings) |
| Camera | Camera2 API (photo + video, foreground service) |
| Location | Android LocationManager |
| Background | Foreground service (camera capture on failed unlock) |
| Monetization | Google Play Billing v7.1.1 |
| Security | SHA-256 (java.security.MessageDigest), no network transmission |

---

## Build

All commands run from `C:\Projects\LockWitness\android`.

```powershell
.\gradlew.bat assembleDebug         # Debug APK
.\gradlew.bat assembleRelease       # Signed release APK (requires keystore.properties)
.\gradlew.bat bundleRelease         # AAB for Play Store
.\gradlew.bat testDebugUnitTest     # Unit tests (53 tests, 0 failures as of Phase 17)
.\gradlew.bat lint                  # Lint
```

**Last verified builds:**
- `assembleDebug` — BUILD SUCCESSFUL, 2026-05-23 (Phase 18 UI changes)
- `bundleRelease` — BUILD SUCCESSFUL, 3.18 MB AAB, 2026-05-20 (Phase 17)
- `assembleRelease` — BUILD SUCCESSFUL, 1.52 MB APK (minified), 2026-05-20 (Phase 17)
- Unit tests — 53 tests, 0 failures, 0 errors, 2026-05-20 (Phase 17)

---

## Security Constraints

These are enforced at the code level and are non-negotiable:

- No `android.hardware.Camera` (deprecated). Camera2 only.
- No `RECORD_AUDIO` permission. Video is silent.
- No SMS, contacts, call-log, microphone, overlay, or accessibility-service access.
- No hardcoded credentials, API keys, email addresses, or SMTP configuration.
- No automatic transmission of captured media. All export/share is user-initiated.
- No stealth mode, hidden icon, or anti-uninstall behavior.
- Evidence stored in app-private storage; never world-readable.

---

## Development Protocol

This project uses a phase-gated development protocol. See:

- `CLAUDE.md` — session entry point and non-negotiable rules
- `docs/codex-control-framework-full.md` — authoritative control framework
- `PROJECT_STATE.md` — current phase, verified features, last builds
- `AGENTS.md` — supplementary agent rules
- `README_BUILD_PROTOCOL.md` — build and backup procedures
