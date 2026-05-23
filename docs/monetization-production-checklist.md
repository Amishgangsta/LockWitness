# LockWitness Monetization Production Checklist

Production monetization is not complete. This checklist must be finished before Play Store production release.

---

## Pricing Model (Authorized 2026-05-23)

**7-day free trial at base level. No permanent free tier.**

| Plan | Price | Billing |
|---|---|---|
| Monthly | $2.99 | Auto-renewing subscription |
| Annual | $19.99 | Auto-renewing subscription |
| Lifetime | $39.99 | One-time purchase |

**Trial scope:** Failed-unlock monitoring, photo capture, last 10 incidents, basic diagnostics.  
**Trial is NOT Pro.** Video, GPS, unlimited history, ZIP export, and share are not available during trial.  
**After trial:** User must purchase a plan to continue. No ongoing free fallback.

---

## Current State

- Free/Pro gate logic exists (`ProFeatureGate`, `MonetizationRepository`, `MonetizationState`).
- 7-day trial timer: **not yet implemented** — requires a phase-authorized code change.
- Trial-level gates (base-only, not Pro): **not yet implemented** — gates currently model Free vs Pro, not trial vs Pro.
- Production Play Billing product IDs: **not configured**.
- Production AdMob setup: **not complete**.
- Banner ad placeholder uses Google test ID only.

---

## Trial Implementation Checklist (Phase Required)

- [ ] Define trial start timestamp (first install, DataStore persisted).
- [ ] Add `isInTrial: Boolean` and `trialExpired: Boolean` to `MonetizationState`.
- [ ] Gate Pro features off during trial (trial = base level only).
- [ ] Show trial countdown or expiry notice in Dashboard and Upgrade screen.
- [ ] On trial expiry: show upgrade prompt; monitoring continues but evidence modules pause until purchase.
- [ ] Confirm trial cannot be restarted after expiry on the same install.

---

## Play Billing Production Checklist

- [ ] Define product IDs: `pro_monthly`, `pro_annual`, `pro_lifetime` (or equivalent).
- [ ] Configure all three products in Play Console (one-time purchase for lifetime, subscriptions for monthly/annual).
- [ ] Implement purchase flow (authorized phase).
- [ ] Implement entitlement restore flow.
- [ ] Verify billing unavailable fallback does not crash the app.
- [ ] Verify Pro gates update immediately after successful purchase.
- [ ] Verify trial-expired state transitions to subscriber state correctly after purchase.
- [ ] Confirm core monitoring remains functional regardless of billing state.

---

## AdMob Production Checklist

- [ ] Create or confirm production AdMob app entry.
- [ ] Create production banner ad unit IDs.
- [ ] Replace test-only banner ID in source (authorized phase).
- [ ] Confirm ads do not appear for Pro subscribers.
- [ ] Confirm ads do not appear during the 7-day trial (trial is a premium experience).
- [ ] Confirm ad load failure does not crash the app or interrupt monitoring.
- [ ] Confirm ad placement does not obscure monitoring controls, diagnostics, or permission disclosures.
- [ ] Update privacy policy and Data safety form for production ad SDK behavior.

---

## Release Evidence Required

- Trial timer verified: start date persists across force-stop/relaunch; expiry gates features correctly.
- Purchase flow verified in an approved Play billing test environment.
- Restore flow verified.
- Ad placement verified with production IDs and policy-compliant placement.
- Build/test evidence after all production monetization configuration.
- Runtime evidence that billing and ad failures do not crash or block incident logging.

---

## Do Not Release Until

- Trial implementation is complete and verified.
- Production Play Billing product IDs are configured and verified.
- Production AdMob setup is complete, or ads are explicitly disabled for release.
- Privacy policy and Play Console disclosures reflect the exact trial and monetization behavior.
- Store listing pricing figures match live Play Console product configuration.
