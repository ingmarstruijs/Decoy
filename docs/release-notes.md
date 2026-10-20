# Release notes

## 0.1.0 (Phase 0–6 prep)

First milestone build of Decoy: FOSS Android privacy layer for physical-world services, fixture-first and F-Droid-oriented.

### Included

- Plugin shell: QR, URL, NFC, zone keypad, **BLE / UWB injection** (local-only ambient)
- One-shot fixtures: campsite/terrace order, parking, captive portal, EV, vending
- Multi-day fixtures: campsite stay, short-stay cabin, festival/venue pass (encrypted vault + TTL)
- Field policy: Needed / ContactAlias / PayElsewhere / Omit / ConsentRequired
- Payments: Fake (CI default) + staging open-banking relay mock
- Privacy HTTP defaults + banned identity interceptor
- Debug test lab scenarios; docs under `docs/`

### Explicitly not in this tag

- Live venue / operator APIs
- Production PSP settlement
- Physical BLE/NFC hardware requirement for CI
- **F-Droid.org publication** (metadata + reproducible posture only — see [fdroid.md](fdroid.md))

### Privacy residuals

PSP/bank and IP linkability remain — [threat-model.md](threat-model.md).
