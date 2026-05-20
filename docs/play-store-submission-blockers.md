# LockWitness Play Store Submission Blockers

Last updated: 2026-05-20

Status: **blocked for production Play Store submission** — runtime verification complete; monetization code complete; Play Console configuration and legal review remain.

---

## Cleared Blockers (Phases 14–15)

These items were blocking as of Phase 14 and have since been cleared:

| Blocker | Cleared by | Evidence |
|---|---|---|
| Physical Android device runtime test | Phase 14-A | SM-G973U1, Android 12, RF8M3278JVE; app launched to dashboard |
| Device Admin activation | Phase 14-B | `dumpsys device_policy` confirms receiver active; master monitoring toggle persists |
| Failed unlock callback | Phase 14-C | `onPasswordFailed` fires; Room row created with `triggerType=FAILED_UNLOCK` |
| Photo capture | Phase 14-D | 4 JPEGs in `files/incident_photos/`, `photoStatus=SUCCESS`, `imageSha256` populated |
| Video capture | Phase 14-H | 3.1 MB MP4 via Diagnostics; `videoStatus=SUCCESS` |
| Location snapshot | Phase 14-H | `locationStatus=SUCCESS` with warm GPS cache; `locationStatus=UNAVAILABLE` correct on WiFi-only with no fix |
| Export ZIP | Phase 14-F | 41 MB ZIP; 13 photos, all SHA-256 hashes verified |
| Android share/chooser | Phase 14-G | Chooser launched; `shareStatus=SUCCESS` written to Room |
| Production Play Billing code | Phase 15 | `PlayBillingService`, `ProProduct`, `UpgradeScreen`, `DashboardScreen` implemented and build-verified; 53 unit tests pass |
| Privacy/legal documents updated for billing | Phase 16 | `privacy-policy-draft.md`, `play-store-listing-draft.md`, `permission-disclosures.md` updated with subscription/billing terms |

---

## Remaining Blockers

These items must be completed before production submission:

### Play Console Configuration (Phase 17)

- [ ] Create product IDs in Play Console:
  - `lockwitness_pro_monthly` — subscription, $2.99/month
  - `lockwitness_pro_annual` — subscription, $19.99/year
  - `lockwitness_pro_lifetime` — one-time purchase, $19.99 (founder); update to $39.99 after promo
- [ ] Add license testing account(s) to Play Console for end-to-end billing flow verification
- [ ] Verify live purchase, restore, cancellation, and billing-unavailable flows on device
- [ ] Confirm Free mode remains functional if Play Billing is unavailable

### AdMob Production Setup

- [ ] Create production AdMob app and banner ad unit
- [ ] Replace test banner ID (`ca-app-pub-3940256099942544/6300978111`) with production ad unit ID
- [ ] Confirm Pro users do not see ads
- [ ] Confirm ad failure does not block core incident logging

### Privacy Policy Hosting

- [ ] Replace placeholder contact fields (`[Insert developer contact email]`, `[Insert developer mailing address]`) in `store/privacy-policy-draft.md`
- [ ] Host the finalized privacy policy at a stable public URL
- [ ] Enter the privacy policy URL in Play Console

### Play Console Data Safety Form

- [ ] Complete Data safety form against the exact release build
- [ ] Confirm data collection/sharing answers are accurate for AdMob, Play Billing, camera, location, and local-only storage

### Release Build

- [ ] Configure release signing (keystore, `signingConfig` in `build.gradle.kts`)
- [ ] Run `.\gradlew.bat bundleRelease` to produce signed AAB
- [ ] Upload AAB to Play Console internal testing track
- [ ] Verify billing flow end-to-end on internal testing track build

### Final Legal/Policy Review

- [ ] Owner review of finalized `privacy-policy-draft.md` before hosting
- [ ] Owner review of `play-store-listing-draft.md` before submission
- [ ] Confirm app name, description, and screenshots match actual app behavior
- [ ] Confirm subscription auto-renewal disclosures meet Google Play Billing requirements

---

## Non-Blocking Evidence (supports readiness)

- 53 unit tests pass locally
- Debug build passes locally (`assembleDebug`)
- All core features runtime-verified on SM-G973U1, Android 12
- Store, privacy, and permission documentation updated
- Source scan: no SMS, audio, overlay, accessibility, SMTP, deprecated camera APIs, or hardcoded secrets found
- All monetization product IDs and display models defined in source
