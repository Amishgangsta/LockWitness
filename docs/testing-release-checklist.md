# LockWitness Testing And Release Checklist

Use this checklist before any Play Store submission or internal testing release.

## Build Verification

- Run `.\gradlew.bat testDebugUnitTest`.
- Run `.\gradlew.bat assembleDebug`.
- Confirm the build uses no production ad IDs unless production ads are approved.
- Confirm billing configuration is test-safe or production-ready as documented.

## Device Runtime Verification

- Install the debug or release candidate build on a physical Android device or emulator.
- Launch LockWitness.
- Confirm dashboard loads.
- Enable Device Admin from the visible onboarding flow.
- Confirm Settings shows Device Admin, camera, and location status.
- Grant camera permission and test photo diagnostics.
- Grant location permission if location capture is enabled and test location diagnostics.
- Enable Master Monitoring and perform a wrong unlock attempt.
- Confirm a local incident appears in History.
- Open incident detail and confirm statuses, paths, hashes, device metadata, and notes.
- Test delete single incident.
- Test clear history.
- Test export package generation through user action.
- Test Android chooser availability through user action.
- Review logcat for crashes, permission errors, and policy-relevant warnings.

## Play Console Preparation

- Upload the privacy policy URL.
- Complete Data safety answers to match the actual build.
- Declare Device Admin, camera, and location permission use accurately.
- Confirm app access instructions explain any reviewer setup steps.
- Add review notes for Device Admin activation and wrong unlock testing.
- Confirm screenshots match the actual app.
- Confirm listing text matches implemented features only.

## Compliance Review

- Confirm all evidence modules are owner-controlled and independently toggleable.
- Confirm exports and share handoffs require user action.
- Confirm no remote backend is present unless fully disclosed in a future phase.
- Confirm no audio recording permission or audio capture path exists.
- Confirm no SMS permission or SMS path exists.
- Confirm no accessibility service or overlay permission exists.
- Confirm no anti-uninstall behavior exists beyond standard Device Admin system behavior.

## Release Candidate Notes

- Create a local ZIP backup for release candidates under the reduced backup policy.
- Record the release candidate commit hash in `PROJECT_STATE.md`.
- Record whether runtime verification was completed on real device/emulator evidence.
