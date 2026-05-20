# LockWitness Privacy Policy Draft

Last updated: 2026-05-20

This draft is for Play Store review preparation and must be reviewed by the app owner before publication. Before publishing, replace placeholder fields and host this policy at a public URL.

## App Summary

LockWitness is an owner-controlled, local-first Android app that helps the device owner record failed unlock evidence on their own device. The app stores incident records locally and lets the user decide which evidence modules are enabled.

## Data LockWitness May Process

LockWitness may process the following data when the user enables the related features:

- **Failed unlock incident metadata:** timestamp, trigger type, failed attempt count, device model, Android version, and app version.
- **Camera evidence:** front-camera photo or short video files, only when the related setting is enabled and the camera permission is granted.
- **Location evidence:** latitude, longitude, accuracy, and provider metadata, only when location capture is enabled and location permission is granted.
- **Local evidence integrity data:** SHA-256 hashes for available media files when evidence hashing is enabled.
- **Export package data:** user-selected local incident records, metadata, hashes, and available media files.
- **Purchase and subscription state:** Free or Pro status, determined locally via Google Play Billing. LockWitness does not store or transmit payment card or billing account details — these are handled entirely by Google Play.

## Local-First Storage

Incident records, media files, hashes, and export packages are stored on the user's device. LockWitness does not run a cloud backend and does not transmit incident evidence without user action.

## Permissions

LockWitness requests permissions only for user-visible features:

- **Device Admin:** used to receive failed unlock attempt callbacks after the user explicitly enables Device Admin access through the system settings prompt.
- **Camera:** used for user-enabled photo and video evidence capture.
- **Location:** used for user-enabled location snapshot metadata.
- **Notifications (Android 13+):** used to display the foreground service notification required by Android when capturing evidence at the time of a failed unlock event.
- **Local storage/export access:** used to create local export packages and attach files to user-initiated chooser flows.

## Sharing And Export

Exports and share handoffs are user-initiated. LockWitness creates a local export package and opens an Android chooser only when the user selects the related action. LockWitness does not send evidence to a preset destination.

## Subscriptions And In-App Purchases

LockWitness offers optional paid upgrades through Google Play:

- **Pro Monthly** — auto-renewing monthly subscription at $2.99/month (price may vary by region).
- **Pro Annual** — auto-renewing annual subscription at $19.99/year (price may vary by region).
- **Lifetime Pro** — one-time purchase at $19.99 (introductory founder price; standard price is $39.99).

Subscriptions automatically renew unless cancelled at least 24 hours before the end of the current billing period. You can manage or cancel subscriptions at any time through your Google Play account settings. Cancellation takes effect at the end of the current paid period — you retain Pro access until then. No refunds are provided for partial billing periods except as required by applicable law.

Payment for subscriptions is charged to your Google Play account upon purchase confirmation and at the start of each renewal period. Google Play handles all payment processing; LockWitness does not access or store payment information.

The Free tier remains fully functional if you do not purchase a Pro plan. Core failed unlock monitoring and photo capture are available at no cost.

## Advertising

The Free tier displays a banner advertisement. Advertisements are served through Google AdMob. Pro users do not see advertisements. LockWitness does not use personalized ad targeting beyond what Google AdMob applies based on its own policies.

## Data Retention And Deletion

Incident records remain on the device until the user deletes a single incident or clears history. Local export packages remain on the device until the user deletes them through normal device file management or app data cleanup. Uninstalling the app removes all locally stored app data.

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

LockWitness is intended for device owners and is not designed for or directed at children under 13. The app is not a children's app.

## Changes To This Policy

If this policy changes materially, the updated policy will be posted at the policy URL with a revised "Last updated" date. Continued use of the app after changes constitutes acceptance of the updated policy.

## Contact

Developer contact: [Insert developer contact email]

Mailing address: [Insert developer mailing address if required by applicable law]

## Review Notes Before Publishing

- Replace `[Insert developer contact email]` and `[Insert developer mailing address]`.
- Host this policy at a stable public URL and enter that URL in Play Console.
- Verify pricing figures match the live Play Console product configuration before publishing.
- Complete the Play Console Data safety form against the exact release build.
- Confirm AdMob data handling disclosures match Google AdMob's current partner policy requirements.
- Confirm no network behavior was added between this draft and the release build.
