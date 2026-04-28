# LockWitness Play Store Submission Blockers

Date: 2026-04-28

Status: blocked for production Play Store submission.

## Blocking Items

These items must be independently completed before production submission:

- Physical Android device runtime test.
- Device Admin activation test.
- Failed unlock test.
- Photo capture test.
- Video capture test.
- Location snapshot test.
- Export ZIP test.
- Android chooser/share test.
- Production AdMob setup.
- Production Play Billing setup.
- Final privacy/legal review.

## Runtime Evidence Blockers

The app has local build and unit-test evidence, but Codex has not independently completed runtime verification on an attached device or emulator.

Required evidence:

- Device/emulator model and Android version.
- Installed build commit or APK version.
- Screenshots or screen recordings for core flows.
- Logcat review summary.
- Incident record samples with sensitive values redacted.

## Monetization Blockers

Production monetization is not complete.

Required AdMob work:

- Replace test ad placeholder with approved production setup only after policy review.
- Confirm production ad unit IDs are not hardcoded in an unsafe way.
- Confirm Pro users do not see ads.
- Confirm ad failure does not block core incident logging.

Required Play Billing work:

- Define production product IDs.
- Configure Play Console products.
- Verify purchase, restore, cancellation, and billing-unavailable flows.
- Confirm Free mode remains usable if billing fails.

## Policy And Documentation Blockers

Required before submission:

- Replace privacy policy placeholder contact fields.
- Host privacy policy at a public URL.
- Complete Play Console Data safety form against the exact release build.
- Ensure screenshots and listing text match the app behavior.
- Complete final privacy/legal review.

## Non-Blocking Local Evidence

The following evidence supports readiness for runtime testing, but does not clear production release:

- Unit tests pass locally.
- Debug build passes locally.
- Store/privacy documentation drafts exist.
- Permission disclosures exist.
- Runtime test plan exists.
- Source scans did not find prohibited SMS, audio, overlay, accessibility, SMTP, or deprecated camera APIs.
