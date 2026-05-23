# LockWitness — Product Overview

**Tamper-evident Android evidence recording. Premium-priced. Zero infrastructure overhead.**

---

## The Problem

Smartphone theft is the most common form of personal property crime in the United States. Intimate partner surveillance, employee device tampering, and unauthorized access by known parties are even more prevalent — and harder to prove.

When someone tries to access your phone without permission, there is typically no record of the attempt. The device locks again. The intruder walks away. The owner has no evidence.

Android's built-in lock screen logs nothing meaningful and exposes nothing to the owner. There is no native mechanism for capturing who attempted access, when, or how many times.

---

## The Product

LockWitness is a native Android application that fills this gap. It activates via the Android Device Administration API — a system-level permission that requires the owner's explicit consent — and silently records evidence on every failed unlock attempt.

**Evidence captured per incident:**
- Front-camera photograph (JPEG, 3–3.3 MB, Camera2 API)
- Front-camera silent video clip (MP4, Camera2 API, no audio)
- GPS coordinates with accuracy and provider metadata
- Timestamp, device model, Android version, app version
- Failed attempt count for the session
- SHA-256 cryptographic hash for each captured file

**Evidence handling:**
- Stored entirely in app-private, on-device storage
- Never automatically transmitted anywhere
- Never accessible to other apps
- Exportable by the owner as a ZIP package (photos, incidents.csv, hashes.txt, metadata.json)
- Shareable via Android's standard chooser — the owner selects the destination every time

**Forensic integrity:**
SHA-256 hashes are calculated at capture time and stored alongside each file. The export ZIP includes a `hashes.txt` that allows independent verification of every file in the package. This is the same integrity model used in professional forensic tooling — applied to a consumer device.

---

## Target Users

LockWitness is positioned for users who have a legitimate, personal reason to document unauthorized access to their own device. Primary segments:

| Segment | Use Case |
|---|---|
| Individuals in contentious domestic situations | Document tampering by a partner, ex-partner, or family member |
| Travelers and remote workers | Evidence of room entry, device access in high-risk environments |
| Professionals with sensitive device contents | Document insider threats, coworker access, employer surveillance |
| Security-conscious general consumers | Peace of mind; awareness of who touches their device |
| Small business owners | Company-issued device monitoring without MDM overhead |

The common thread: these are people who need proof, not just suspicion — and who cannot rely on enterprise MDM or law enforcement tools to get it.

---

## Why Android

Android's Device Administration API gives LockWitness a capability that iOS explicitly blocks: the ability to receive a callback when a lock screen attempt fails. This is not a workaround or a vulnerability exploit. It is a documented, policy-compliant API that Google provides specifically for this type of owner-controlled device monitoring.

This gives LockWitness a durable platform advantage. An equivalent iOS product cannot exist without Apple's cooperation.

---

## Business Model

### Pricing

**7-day trial at base level. No permanent free tier.**

| Plan | Price |
|---|---|
| Monthly | $2.99 / month |
| Annual | $19.99 / year |
| Lifetime | $39.99 one-time |

The trial gives prospective customers enough time to verify the app works on their device and experience the core value proposition. It is intentionally not a full Pro trial — users get monitoring and photo capture, which demonstrates the concept, but video, GPS, and export require a paid plan.

### Why no permanent free tier

Free users in utility apps exhibit predictably low engagement: they open the app once or twice, leave it running, and rarely convert. More importantly, they rarely leave positive reviews — the users who love a product enough to advocate for it publicly are the ones who invested in it.

Giving the full experience away permanently signals low confidence in the product's value. A $2.99/month baseline is not a barrier for a user with a genuine need. It is a filter: users who subscribe are self-selecting as people who take their security seriously, which is exactly the user who will engage with the product and represent it well.

This is the same logic that drives premium SaaS pricing. The goal is not maximum install count — it is a paying user base that is small, engaged, and high signal-to-noise.

### Unit Economics

| Metric | Figure |
|---|---|
| Infrastructure cost | $0 (no backend, no cloud, no servers) |
| Ongoing marginal cost per user | $0 |
| Play Store fee | 15% after the first $1M/year (small developer tier) |
| Effective monthly revenue at $2.99 | ~$2.54 per paying monthly subscriber |
| Break-even paying users | 1 (positive margin on the first sale) |
| Annual plan effective monthly | ~$1.42/month retained revenue |
| Lifetime at $39.99 | One-time; no churn risk |

There is no hosting bill, no API cost, no support infrastructure, and no team overhead required to keep the product running. Every dollar of revenue above Play Store fees is margin.

### Revenue Scenarios

| Monthly Paying Subscribers | Monthly Revenue (after 15% fee) | Annual Revenue |
|---|---|---|
| 100 | ~$254 | ~$3,048 |
| 500 | ~$1,270 | ~$15,240 |
| 1,000 | ~$2,540 | ~$30,480 |
| 5,000 | ~$12,700 | ~$152,400 |

Annual and lifetime plans shift the revenue curve favorably — annual locks in 12 months at the ~44% discount, and lifetime creates a cohort of committed advocates with zero renewal overhead.

---

## Product Quality Signals

LockWitness was built to a level of engineering discipline unusual for an independent Android app.

**18 development phases**, each with:
- Explicit acceptance criteria
- Unit test coverage before implementation proceeds
- Device-level runtime verification (not just build verification)
- Documented evidence for every verified claim

**53 unit tests, 0 failures** as of the Phase 17 release build.

**Phase-by-phase device verification** on SM-G973U1 (Android 12):
- Device Admin activation and `onPasswordFailed` callback confirmed via `dumpsys`
- Photo capture confirmed: 4 JPEGs written to app-private storage, SHA-256 hashes verified
- Video capture confirmed: 3.1 MB MP4 written to app-private storage
- GPS confirmed: `locationStatus=SUCCESS` in Room incident record after warm-cache outdoor test
- ZIP export confirmed: 41 MB package, all 13 photo hashes verified against `sha256sum`
- Share chooser confirmed: `shareStatus=SUCCESS` written to Room after user-initiated share

**Release build characteristics:**
- Signed AAB: 3.18 MB
- Signed APK: 1.52 MB (ProGuard minified)
- `isMinifyEnabled=true`, `isShrinkResources=true`
- No deprecated APIs, no `RECORD_AUDIO`, no SMS, no accessibility services, no overlay permissions

---

## Technical Architecture

| Component | Implementation |
|---|---|
| Monitoring hook | Android Device Admin `onPasswordFailed` |
| Photo capture | Camera2 API, foreground service (Android 12+ compliant) |
| Video capture | Camera2 API, foreground service, no audio |
| Location | Android LocationManager, fine + coarse permission |
| Evidence storage | Room database + app-private file storage |
| Settings | DataStore Preferences |
| Integrity | SHA-256 via `java.security.MessageDigest` |
| Monetization | Google Play Billing v7.1.1 |
| UI | Jetpack Compose, Material 3, HorizontalPager swipe navigation |
| Minimum API | Android (target: modern Android; Camera2 + Device Admin required) |

The entire stack is local. There is no backend service, no cloud storage, no analytics SDK, no crash reporter, and no ad SDK beyond the banner placeholder. The production surface is exactly what the user sees.

---

## Competitive Landscape

There is no direct equivalent on the Play Store that combines:
- Device Admin-level failed-unlock detection
- Camera2-based silent photo + video capture
- SHA-256 forensic hashing
- User-controlled local export
- A coherent, professional UI

Existing apps in adjacent categories are either spyware-adjacent (covert, marketed deceptively), technically shallow (screenshot-only), or abandoned. LockWitness is the only product in this category built to explicit policy-compliance standards with documented forensic methodology.

---

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Google Play policy changes around Device Admin | App uses a documented, compliant API. Monitoring purpose is disclosed. |
| Low organic discoverability | Addressable via ASO; niche but high-intent search terms |
| One-star reviews from users who misunderstand the product | Clear onboarding, biometric disclaimer in-app and in store listing |
| Android version fragmentation (camera restrictions) | Camera2 foreground service approach handles Android 12+ restrictions |
| User does not complete Device Admin activation | Setup screen walks through the step; Diagnostics shows readiness score |

---

## Current Status

- **Phase 18 UI polish** in progress (Crimson Forensic theme, HorizontalPager swipe navigation, Diagnostics redesign).
- **Release AAB** signed and ready: 3.18 MB, Phase 17.
- **Remaining before live submission:** Production Play Console product configuration, production AdMob ID, 7-day trial implementation, privacy policy hosting, Data safety form.
- **No backend infrastructure required** at any point in the launch process.

---

## Summary

LockWitness is a technically rigorous, policy-compliant, infrastructure-free Android security product. It addresses a real and underserved need with a capability that only Android can provide. The unit economics are exceptional — zero marginal cost, zero infrastructure, positive margin on the first subscriber. The pricing strategy is deliberate: premium positioning with a time-limited trial creates the conditions for a small, high-quality user base that converts and advocates. There is no scenario where this product costs money to operate.
