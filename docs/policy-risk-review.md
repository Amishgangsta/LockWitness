# LockWitness Play Policy Risk Review

This document is a preparation aid, not legal advice. Review current Google Play policy before submission.

Official policy references reviewed on 2026-04-28:

- Google Play Permissions and APIs that Access Sensitive Information: https://support.google.com/googleplay/android-developer/answer/9888170
- Google Play Deceptive Behavior: https://support.google.com/googleplay/android-developer/answer/9888077
- Google Play Metadata: https://support.google.com/googleplay/android-developer/answer/9898842
- Google Play Malware policy: https://support.google.com/googleplay/android-developer/answer/9888380
- Google Play Developer Program Policy: https://support.google.com/googleplay/android-developer/answer/16543315

## Overall Positioning

LockWitness should be positioned as an owner-controlled, local-first failed unlock evidence recorder for the device where it is installed.

Required positioning:

- Use clear language about failed unlock evidence.
- Explain Device Admin, camera, and location permissions before use.
- State that export and share are user-initiated.
- State that captured evidence is stored locally in the current implementation.
- State that no silent transmission occurs.

## Key Policy Risks

### Device Admin

Risk: Device Admin can affect device settings and must be transparent and reversible.

Mitigation:

- Use visible onboarding.
- Explain failed unlock callback purpose.
- Explain how the user can disable Device Admin through Android settings.
- Avoid any anti-removal behavior beyond Android's standard Device Admin flow.

### Camera And Video

Risk: Camera access is sensitive and must be tied to a visible, user-enabled feature.

Mitigation:

- Explain camera use in Settings and Play Store disclosures.
- Keep video capture without audio.
- Keep manual diagnostics explicit.
- Do not claim evidence capture has been runtime-verified unless device evidence exists.

### Location

Risk: Location is sensitive and must be permission-gated and purpose-limited.

Mitigation:

- Keep location disabled by default.
- Explain location snapshot metadata before permission requests.
- Record unavailable status without breaking incident persistence.

### Export And Share

Risk: Evidence movement outside the app must be user-controlled.

Mitigation:

- Keep export manual.
- Use Android chooser flow.
- Do not configure preset destinations.
- Do not transmit evidence without user action.

### Ads And Pro

Risk: Production monetization must match Google Play billing and ads policy.

Mitigation:

- Keep test ad ID until production values are supplied.
- Keep billing failure fallback to Free mode.
- Do not claim production subscription or purchase availability until configured and verified.

### Metadata And Listing

Risk: Store metadata must accurately reflect implemented behavior.

Mitigation:

- Use screenshots from the actual app.
- Avoid claims about runtime capture until verified.
- Avoid claims about cloud, remote access, or monitoring outside the owner's device.
- Keep app category and content rating consistent with the actual feature set.

## Data Safety Draft Notes

Likely data categories to review in Play Console:

- Photos and videos: processed locally if camera evidence is enabled.
- Location: processed locally if location capture is enabled.
- Files and docs: export packages may include local evidence files.
- Device or other IDs: device model, Android version, and app version are stored in incident metadata.
- App activity: failed unlock incident records and in-app diagnostic action results are local app activity records.

Current implementation note:

- No cloud backend is implemented.
- No account system is implemented.
- No SMS or audio data is processed.
- Production ads and production billing are not configured.

## Release Review Recommendation

Before production submission, perform a real device review covering Device Admin activation, camera permission, location permission, failed unlock flow, diagnostics, history/detail, export, share chooser, and logcat. Update `PROJECT_STATE.md` with evidence and keep the Play Console Data safety answers aligned with the exact build.
