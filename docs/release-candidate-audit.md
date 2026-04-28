# LockWitness Release Candidate Audit

Date: 2026-04-28

Branch: main

Starting commit: 5170e5f48b3b31179a14cd8b6bddf6299578781f

This audit is a control checkpoint only. It does not claim runtime verification that has not occurred.

## Scope

Phase 14 reviewed build/test status, source risk surface, documentation readiness, release blockers, and final pre-release actions.

No Android app source files were intentionally modified in this phase.

## Verification Evidence

Available local verification:

- `.\gradlew.bat testDebugUnitTest` completed with `BUILD SUCCESSFUL`.
- `.\gradlew.bat assembleDebug` completed with `BUILD SUCCESSFUL`.

Source scans:

- No case-sensitive matches for deprecated `android.hardware.Camera`.
- No case-sensitive matches for `RECORD_AUDIO`, `SEND_SMS`, `SmsManager`, `AccessibilityService`, `SYSTEM_ALERT_WINDOW`, `ACTION_MANAGE_OVERLAY_PERMISSION`, `setAudioSource`, `MediaRecorder.AudioSource`, `SMTP`, or `smtp`.
- Broader scan for network/secret indicators found XML namespace strings and existing Device Admin callback context, not a cloud/backend implementation.
- Ad ID scan found only the existing Google test banner ID in `AdPlaceholder.kt`.

## Verified Status From Local Evidence

- Unit test suite passes locally.
- Debug build passes locally.
- Store/privacy documentation exists.
- Diagnostics and runtime checklist documentation exist.
- Reduced backup policy is documented.
- Local pre-phase release-candidate backup was created under the reduced backup policy.

## Not Runtime-Verified By Codex

The following remain not independently runtime-verified in Codex:

- Physical Android device launch.
- Device Admin activation.
- Failed unlock callback.
- Photo capture.
- Video capture.
- Location snapshot.
- History/detail navigation on a device.
- Export ZIP generation on a device.
- Android chooser/share flow on a device.
- Diagnostics manual actions on a device.
- Logcat review from an attached Android device or emulator.

## Release Blockers

The app must not be treated as production-release-ready until these blockers are completed with evidence:

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

## Documentation Readiness

Created or reviewed documentation:

- `store/privacy-policy-draft.md`
- `store/play-store-listing-draft.md`
- `store/permission-disclosures.md`
- `docs/in-app-disclosure-copy-recommendations.md`
- `docs/testing-release-checklist.md`
- `docs/policy-risk-review.md`
- `docs/runtime-test-plan.md`
- `docs/play-store-submission-blockers.md`
- `docs/monetization-production-checklist.md`

## Final Pre-Release Action List

1. Run the full runtime test plan on a physical Android device or emulator.
2. Capture evidence for Device Admin activation and failed unlock incident creation.
3. Capture evidence for photo, video, location, history/detail, export, and chooser flows.
4. Review logcat for crashes, security warnings, permission issues, and policy-relevant errors.
5. Configure production AdMob only after final ad policy review.
6. Configure production Play Billing only after product IDs, pricing, and entitlement behavior are final.
7. Replace placeholder privacy policy contact fields.
8. Complete final privacy/legal review.
9. Update Play Console Data safety answers to match the exact release build.
10. Create a release-candidate tag only after runtime blockers are cleared.

## Release Candidate Decision

Status: blocked for production submission.

Reason: local build/test checks pass, but independent runtime verification and production monetization setup are not complete.
