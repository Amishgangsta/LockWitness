# LockWitness Permission Disclosure Text

Use these disclosures in Play Console, onboarding copy, Settings explanations, and review notes where applicable.

## Device Admin

LockWitness uses Android Device Admin only after the device owner enables it. This access allows the app to receive failed unlock attempt callbacks and create a local incident record. The user can disable Device Admin access through Android system settings.

## Camera

LockWitness requests camera permission so the device owner can enable front-camera photo or short video evidence for failed unlock incidents and diagnostic tests. Video capture is designed without audio recording.

## Location

LockWitness requests location permission only when the device owner enables location snapshot capture. Location metadata may include latitude, longitude, accuracy, and provider information, and is stored with the local incident record.

## Local Storage And Export

LockWitness stores incident metadata, media paths, hashes, and export packages locally on the device. Manual exports may include metadata, CSV timeline data, hashes, and available media files.

## Share And Export Chooser

LockWitness uses Android chooser flows only when the user selects an export or share action. The user chooses the destination app. LockWitness does not send evidence to a preset destination.

## Ads And Pro Placeholder

The current app includes Free/Pro scaffolding and a test banner ad placeholder. Production ads and production billing require final configuration, Play Console setup, and policy review before release.

## Recommended Combined Disclosure

LockWitness is local-first and owner-controlled. Device Admin, camera, and location access are used only for user-enabled failed unlock evidence features. Export and share actions are user-initiated, and captured evidence is not silently transmitted.
