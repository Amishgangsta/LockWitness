# LockWitness Permission Disclosure Text

Use these disclosures in Play Console, onboarding copy, Settings explanations, and review notes where applicable.

Last updated: 2026-05-20

---

## Device Admin

LockWitness uses Android Device Admin only after the device owner enables it through the system settings prompt. This access allows the app to receive failed unlock attempt callbacks and create a local incident record. The user can disable Device Admin access at any time through Android system settings (Settings → Security → Device Admin Apps).

---

## Camera

LockWitness requests camera permission so the device owner can enable front-camera photo or short video evidence for failed unlock incidents and diagnostic tests. Video capture is designed without audio recording. Camera access is used only when the related feature toggle is enabled and a failed unlock event occurs, or when the user manually triggers a diagnostic test.

---

## Location

LockWitness requests location permission only when the device owner enables location snapshot capture in Settings. Location metadata may include latitude, longitude, accuracy, and provider information, and is stored with the local incident record. Location access is not used when the location capture toggle is disabled.

---

## Notifications (Android 13+)

LockWitness requests notification permission to display the foreground service notification required by Android when the evidence capture service runs in response to a failed unlock event. This notification informs the user that evidence capture is in progress and is required by the Android operating system for foreground services on Android 13 and later.

---

## Local Storage And Export

LockWitness stores incident metadata, media paths, hashes, and export packages locally on the device in app-private storage. Manual exports may include metadata JSON, CSV timeline data, SHA-256 hashes, and available media files, packaged as a local ZIP file.

---

## Share And Export Chooser

LockWitness uses Android chooser flows only when the user selects an export or share action. The user chooses the destination app. LockWitness does not send evidence to a preset destination and does not transmit evidence silently.

---

## Subscriptions And In-App Purchases

LockWitness offers optional Pro upgrades via Google Play Billing:

- **Pro Monthly** — $2.99/month, auto-renewing subscription
- **Pro Annual** — $19.99/year, auto-renewing subscription (saves ~44% vs monthly)
- **Lifetime Pro** — $19.99 one-time purchase (introductory founder price; standard price is $39.99)

Prices may vary by region and are set in Google Play. LockWitness does not process payment information — all billing is handled by Google Play.

Auto-renewing subscriptions renew at the end of each billing period unless cancelled at least 24 hours before renewal. Users can manage or cancel subscriptions in Google Play account settings. Cancellation takes effect at the end of the paid period; no partial refunds are given except as required by law.

The Free tier remains functional if no Pro plan is purchased. Core monitoring and photo capture are available at no cost.

---

## Advertising

The Free tier shows a banner advertisement served by Google AdMob. Pro users do not see advertisements. LockWitness does not perform its own ad targeting; targeting behavior follows Google AdMob's policies.

---

## Recommended Combined Disclosure (for Settings or onboarding)

LockWitness is local-first and owner-controlled. Device Admin, camera, location, and notification access are used only for user-enabled failed unlock evidence features. Export and share actions are user-initiated. Captured evidence is not silently transmitted. Optional Pro plans are available via Google Play; the Free plan remains fully functional without a purchase.
