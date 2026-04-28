# LockWitness Monetization Production Checklist

Production monetization is not complete. This checklist must be finished before Play Store production release if ads or Pro billing are enabled.

## Current State

- Free/Pro model exists locally.
- Pro gate logic exists.
- Billing fallback keeps the app usable in Free mode.
- Banner ad placeholder exists.
- Existing ad ID is the Google test banner ID only.
- Production AdMob setup is not complete.
- Production Play Billing setup is not complete.

## AdMob Production Checklist

- Create or confirm the production AdMob app entry.
- Create production banner ad unit IDs.
- Replace test-only IDs only after policy review.
- Confirm no interstitial, rewarded, or app-open ads are introduced without a separate authorized phase.
- Confirm ads do not appear for Pro users.
- Confirm ad load failure does not crash the app.
- Confirm ad placement does not obscure monitoring, diagnostics, settings, history, or permission disclosures.
- Update privacy policy and Data safety answers for production ad SDK behavior.

## Play Billing Production Checklist

- Define production product IDs for Pro.
- Configure products in Play Console.
- Implement or verify Play Billing integration in an authorized phase.
- Verify purchase flow.
- Verify restore entitlement flow.
- Verify billing unavailable fallback.
- Verify refund/cancellation behavior.
- Verify Pro gates update after entitlement changes.
- Confirm core free behavior remains usable.

## Release Evidence Required

- Test purchase evidence in an approved Play billing test environment.
- Ad test evidence with policy-compliant placement.
- Build/test evidence after production monetization configuration.
- Runtime evidence that billing/ad failures do not crash or block incident logging.

## Do Not Release Until

- Production AdMob setup is complete or ads are disabled for release.
- Production Play Billing setup is complete or Pro purchase UI is disabled for release.
- Privacy policy and Play Console disclosures reflect the exact monetization behavior.
