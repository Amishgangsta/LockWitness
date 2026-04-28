# LockWitness Privacy Policy Draft

Last updated: 2026-04-28

This draft is for Play Store review preparation and must be reviewed by the app owner before publication.

## App Summary

LockWitness is an owner-controlled, local-first Android app that helps the device owner record failed unlock evidence on their own device. The app stores incident records locally and lets the user decide which evidence modules are enabled.

## Data LockWitness May Process

LockWitness may process the following data when the user enables the related features:

- Failed unlock incident metadata: timestamp, trigger type, failed attempt count, device model, Android version, and app version.
- Camera evidence: front-camera photo or short video files, only when the related setting is enabled and the camera permission is granted.
- Location evidence: latitude, longitude, accuracy, and provider metadata, only when location capture is enabled and location permission is granted.
- Local evidence integrity data: SHA-256 hashes for available media files when evidence hashing is enabled.
- Export package data: user-selected local incident records, metadata, hashes, and available media files.
- Monetization state: local Free or Pro status and test ad placeholder status during development.

## Local-First Storage

Incident records, media files, hashes, and export packages are stored on the user's device. LockWitness does not run a cloud backend in the current implementation and does not transmit incident evidence without user action.

## Permissions

LockWitness requests permissions only for user-visible features:

- Device Admin: used to receive failed unlock attempt callbacks after the user explicitly enables Device Admin access.
- Camera: used for user-enabled photo and video evidence capture.
- Location: used for user-enabled location snapshot metadata.
- Local storage/export access: used to create local export packages and attach files to user-initiated chooser flows.

## Sharing And Export

Exports and share handoffs are user-initiated. LockWitness creates a local export package and opens an Android chooser only when the user selects the related action. LockWitness does not send evidence to a preset destination.

## Ads And Pro Placeholder

The current monetization implementation is a local Free/Pro model with a test banner ad placeholder. Production advertising IDs and production billing configuration must be added only after final review and policy validation.

## Data Retention And Deletion

Incident records remain on the device until the user deletes a single incident or clears history. Local export packages remain on the device until the user deletes them through normal device file management or app data cleanup.

## User Controls

The user can enable or disable major evidence modules independently in Settings:

- Master monitoring
- Photo capture
- Video capture
- Location capture
- Local timeline
- Email alert status handling
- Share alert status handling
- Evidence hashing

## No Silent Transmission

LockWitness is designed for local evidence capture and user-controlled sharing. It does not silently transmit captured media, location metadata, incident records, or export packages.

## Children

LockWitness is intended for device owners and is not designed for children.

## Contact

Developer contact: [Insert developer contact email]

Mailing address: [Insert developer mailing address if required]

## Review Notes

Before publishing, verify this policy matches the production build, Play Console Data safety answers, app permissions, SDKs, ads, billing, and any future network behavior.
