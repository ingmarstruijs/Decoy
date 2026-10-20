# Product spec

## Problem

Interacting with the physical world increasingly requires a unique vendor app, an account, cookies, trackers, and surplus personal data—for actions as trivial as ordering a sandwich on a campsite or starting a parking session.

## Audience

- People who want frictionless local services without surrendering a civil profile
- FOSS / privacy-minded Android users (F-Droid)
- Contributors building capability plugins for venues and protocols

## Success criteria

- A context appears when needed and vanishes when the user walks away
- Vendor WebViews are not the UI; Jetpack Compose is
- Irrelevant PII is omitted; contact uses aliases; payment secrets never enter vendor HTML forms
- No Decoy cloud account; no GPS upload to Decoy or adtech
- No stable device IDs sent to counterparties
- Distributed via F-Droid with zero Play Services / Firebase dependency
- Every v1 flow is fully testable on an Android emulator with mocks/stubs (no production vendor hosts or physical NFC/UWB required) — see [e2e-testability.md](e2e-testability.md)

## Platform

| Constraint | Requirement |
|------------|-------------|
| OS | Android |
| UI | Jetpack Compose |
| Distribution | F-Droid |
| Forbidden stack | Google Play Services, Firebase, proprietary analytics |
| Default posture | Local-first, no Decoy backend required for core flows |

## Principles → requirements

| Principle | Product requirement |
|-----------|---------------------|
| Contextual transformation | Resolve input → plugin → native UI in milliseconds; dispose on completion or walk-away |
| Universal input | Tiered inputs (below); keypad fallback when no tag |
| Masked identity | Field policy + aliases (below); cooperative proofs later |
| One-shot / Multi-day | See [data-lifecycle.md](data-lifecycle.md) |
| FOSS & anonymous payments | No trackers; W3C Web Payments / open-banking relays; no IBAN in vendor forms |

## Inputs (tiered)

### Core (MVP)

- QR codes
- NFC tags
- URL / deep link (most QR and email entry points resolve here)
- Manual zone / short-code keypad

### Ambient context (local-only)

- BLE beacons / proximity
- Captive portal / local HTTP (e.g. campsite Wi-Fi)

Ambient signals never leave the device as location telemetry to a Decoy cloud or adtech.

### When available

- UWB — same UX contract as BLE proximity; hardware-limited

### Later / optional

- 1D barcodes
- On-device OCR of zone signs
- Matter / Thread for thermostat-class devices

### Explicit non-inputs

- Cloud geolocation APIs
- Play-only Nearby APIs

## Identity — form fatigue / PII theater

**Hero problem:** buy sandwiches at a campsite; the vendor form demands home address, phone, IBAN, and pitch. The user should not pay with a full civil profile for a local One-shot order.

### Field policy

| Class | Policy |
|-------|--------|
| Contextual / needed | Keep (pitch, table, order notes) |
| Contact channel | Single-use alias email; optional masked phone relay |
| Payment secrets | Never type IBAN/card into vendor HTML; pay out-of-band |
| Irrelevant PII | Omit or refuse (home address for on-site pickup, marketing, forced account) |
| Regulated IDs | Explicit consent only; real value; local wipe — never fabricated |

### Technical approach (MVP)

- Native Compose UI instead of vendor WebView
- Field classifier: needed / alias / pay-elsewhere / omit
- Alias generator
- Encrypted local vault for Multi-day stays

### Later

Cooperative venue protocol: vendors request capability claims (`paid`, `pitch 42`) instead of full forms. Cryptographic proofs only when the verifier accepts them.

### Out of scope

Fabricating government IDs, license plates, or banking credentials. See [non-goals.md](non-goals.md).

## Payments

- Prefer open W3C Web Payments and open-banking relays
- Counterparty must not receive name or bank account number via Decoy-filled forms
- Residual linkability via PSP/bank is documented in [threat-model.md](threat-model.md)

## Retention

Interactions are **One-shot** or **Multi-day**. Definitions and wipe rules: [data-lifecycle.md](data-lifecycle.md).

## Related docs

- [Use cases](use-cases.md)
- [Threat model](threat-model.md)
- [Plugins architecture](plugins-architecture.md)
