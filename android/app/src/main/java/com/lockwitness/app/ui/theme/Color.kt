package com.lockwitness.app.ui.theme

import androidx.compose.ui.graphics.Color

// LockWitness Forensic Evidence Console — Spec v2.0
// Design tokens from Lock_Witness_Full_UI_Spec_Sheet

// ── Spec tokens ──────────────────────────────────────────────────────────────
val GraphiteBg     = Color(0xFF080A0D)  // root screen background
val SurfaceRaised  = Color(0xFF11151B)  // top bar / bottom nav / sheets
val CardSurface    = Color(0xFF161B22)  // cards and panels
val StrokeSubtle   = Color(0xFF29313A)  // borders / dividers
val TextPrimary    = Color(0xFFF3F4F6)  // primary text
val TextSecondary  = Color(0xFF9CA3AF)  // secondary / metadata text
val MutedChip      = Color(0xFF202733)  // neutral chips / toggle-off track
val HashText       = Color(0xFFCBD5E1)  // monospace hash text
val VerifiedGreen  = Color(0xFF4C7559)  // armed / pass / verified
val CautionAmber   = Color(0xFFFFC100)  // caution / pending / unavailable
val ProOrange      = Color(0xFFFF8200)  // pro / action emphasis
val DestructiveRed = Color(0xFFD64A4A)  // delete / fail only

// ── Legacy aliases (used throughout existing screens) ─────────────────────────
val LockWitnessBackground   = GraphiteBg
val LockWitnessBackgroundAlt = GraphiteBg
val LockWitnessSurface       = CardSurface
val LockWitnessSurfaceRaised = SurfaceRaised
val LockWitnessSurfaceVariant = MutedChip
val LockWitnessPrimary       = VerifiedGreen
val LockWitnessPrimaryBright = VerifiedGreen
val LockWitnessPrimaryDark   = Color(0xFF3A5C44)
val LockWitnessSecondary     = TextSecondary
val LockWitnessAccentMuted   = TextSecondary
val LockWitnessDanger        = DestructiveRed
val LockWitnessWarning       = CautionAmber
val LockWitnessSuccess       = VerifiedGreen
val LockWitnessTextPrimary   = TextPrimary
val LockWitnessTextSecondary = TextSecondary
val LockWitnessTextMuted     = TextSecondary
val LockWitnessBorder        = StrokeSubtle
val LockWitnessDivider       = StrokeSubtle
val LockWitnessOnPrimary     = TextPrimary
val LockWitnessOnDark        = TextPrimary

// ── Screen-specific aliases ───────────────────────────────────────────────────
val LWSuccessGreen   = VerifiedGreen
val LWAccentRed      = DestructiveRed
val LWChrome         = HashText
val LWTextPrimary    = TextPrimary
val LWTextSecondary  = TextSecondary
val LWBackground     = GraphiteBg
val LWPanel          = CardSurface

val LWToggleOn        = VerifiedGreen
val LWToggleOff       = MutedChip
val LWToggleThumbOff  = TextSecondary
val LWSectionBlue     = VerifiedGreen   // section headers now use green

val LWActionOrange  = ProOrange
val LWWarningYellow = CautionAmber

val LWDiagDivider      = StrokeSubtle
val LWDiagDisabledBtn  = MutedChip
val LWDiagDisabledText = TextSecondary

val LWNavActive    = VerifiedGreen
val LWNavInactive  = TextSecondary
val LWNavIndicator = MutedChip
