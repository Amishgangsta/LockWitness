# Lock Witness Play Store Submission Blockers

Last updated: 2026-05-23

Status: **code complete — Play Console configuration and legal hosting remain**

---

## Cleared Blockers

| Blocker | Cleared by | Evidence |
|---|---|---|
| Device connectivity and APK install | Phase 14-A | SM-G973U1 Android 12; app launched to dashboard |
| Device Admin activation | Phase 14-B | `onPasswordFailed` confirmed; master monitoring persists |
| Failed unlock callback | Phase 14-C | Room row with `triggerType=FAILED_UNLOCK` |
| Photo capture via foreground service | Phase 14-D | Camera2; 3–3.3 MB JPEGs; SHA-256 verified |
| Video capture via foreground service | Phase 14-D/18 | 3.1 MB MP4; user-verified full flow 2026-05-23 |
| Location snapshot | Phase 14-E/H | `locationStatus=SUCCESS` with warm GPS cache |
| ZIP export with hash verification | Phase 14-F | 41 MB ZIP; all photo SHA-256 hashes verified |
| Android share chooser | Phase 14-G | `shareStatus=SUCCESS` written to Room |
| Production Play Billing code | Phase 15 | PlayBillingService, ProProduct, UpgradeScreen implemented |
| Privacy/legal documents | Phase 16/18 | Updated for trial model, no ads, "Lock Witness" branding |
| Signed release build | Phase 17 | app-release.aab 3.18 MB; app-release.apk 1.52 MB |
| 7-day trial implementation | Phase 18 | Trial timer, isInTrial/trialExpired, Dashboard PlanCard states |
| App name consistency | Phase 18 | All user-facing strings: "Lock Witness" (two words) |
| Ads removed | Phase 18 | BannerAdPlaceholder no-op; NoAds ProFeature removed |
| allowBackup="false" | Phase 18 | AndroidManifest updated |
| Device Admin pre-explanation dialog | Phase 18 | Plain-language dialog before system prompt |
| Beta Pro override removed | Phase 18 | MonetizationRepository now uses real billing state + trial |

---

## Remaining Blockers (owner actions — no code required)

### 1. Play Console — Create Products

Log in to [play.google.com/console](https://play.google.com/console) and create three in-app products:

| Product ID | Type | Price |
|---|---|---|
| `lockwitness_pro_monthly` | Subscription | $2.99/month |
| `lockwitness_pro_annual` | Subscription | $19.99/year |
| `lockwitness_pro_lifetime` | One-time | $19.99 (first 100) → $39.99 standard |

Add at least one license testing account (your own Gmail) so you can verify the purchase flow without real charges.

### 2. Play Console — Verify Billing Flow

On an internal testing track build:
- [ ] Complete a test purchase (monthly or annual)
- [ ] Confirm `isPro` updates immediately after purchase — Dashboard shows "Pro Active"
- [ ] Confirm Restore Purchases works on reinstall
- [ ] Confirm billing-unavailable fallback does not crash the app

### 3. Privacy Policy — Host at a Public URL

- [ ] Add your contact email and mailing address to `store/privacy-policy-draft.md` (replace the two `[Insert...]` placeholders)
- [ ] Host the finalized policy at a stable public URL (GitHub Pages, Netlify, personal site, etc.)
- [ ] Enter that URL in Play Console → Store presence → Store settings → Privacy policy

### 4. Play Console — Data Safety Form

Complete the Data safety form. Accurate answers for this build:

| Question | Answer |
|---|---|
| Does the app collect or share user data? | Yes |
| Data types collected | Photos/videos (camera); Location (approximate and precise); App activity (purchase history via Play Billing) |
| Is all collected data encrypted in transit? | Yes (Play Billing; no other network calls) |
| Can users request data deletion? | Yes (delete incidents in-app; uninstall removes all) |
| Does the app share data with third parties? | No (no analytics, no ad SDK, no backend) |

### 5. Play Console — Store Listing

- [ ] Upload screenshots of the current app UI (Phase 18 Crimson Forensic theme)
- [ ] Confirm short description is ≤ 80 characters
- [ ] Confirm pricing in the listing matches live Play Console product prices
- [ ] Final owner review of `store/play-store-listing-draft.md`

### 6. Release Build

After the Play Console products are configured, do a final release build and upload:

```powershell
cd C:\Projects\LockWitness\android
.\gradlew.bat bundleRelease
# Upload app\build\outputs\bundle\release\app-release.aab to Play Console internal testing
```

---

## Current Build Evidence

- 55 unit tests, 0 failures (2026-05-23)
- `assembleDebug` BUILD SUCCESSFUL, installed on RF8M3278JVE
- `bundleRelease` BUILD SUCCESSFUL, 3.18 MB AAB (Phase 17 — needs rebuild after trial code)
- `assembleRelease` BUILD SUCCESSFUL, 1.52 MB APK (minified)
- All core features device-verified on SM-G973U1, Android 12
- No SMS, RECORD_AUDIO, overlay, accessibility, deprecated camera, SMTP, or hardcoded secrets in source
