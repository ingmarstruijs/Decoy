# Release notes

## 0.1.0 (Phase 0–6b prep)

First milestone build of Decoy: FOSS Android privacy layer for physical-world services, fixture-first and F-Droid-oriented.

### Included

- **Product shell:** ready-to-scan home, Zone or link, FOSS camera QR (CameraX + ZXing)
- Unresolved screen for unknown public QR payloads (nothing sent to vendor)
- Public sample PNGs under `fixtures/public-qr/`
- Demo fixtures sheet (debug only) for inject + payment backend toggle
- QR, URL, NFC, zone, **BLE / UWB injection** (local-only ambient)
- One-shot + Multi-day fixture plugins; field policy; Fake + staging payment port
- Privacy HTTP defaults + banned identity interceptor; F-Droid metadata draft

### Explicitly not in this tag

- Live venue / operator APIs (Phase 7)
- Production PSP settlement (Phase 4b)
- Physical BLE/NFC hardware requirement for CI
- **F-Droid.org publication** (metadata + reproducible posture only — see [fdroid.md](fdroid.md))

### Privacy residuals

PSP/bank and IP linkability remain — [threat-model.md](threat-model.md).
