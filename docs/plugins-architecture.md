# Plugins architecture

Decoy core resolves context and hosts UI. **Capability plugins** implement venue- or protocol-specific behavior without pulling proprietary SDKs into the core.

## Core vs plugins

| Layer | Responsibility |
|-------|----------------|
| Core | Inputs, context resolver, Compose shell, retention, identity field policy, payment orchestration, privacy HTTP defaults |
| Plugins | Know a venue/protocol: parse payloads, fetch capabilities, map fields, drive flow to completion |

Plugin kinds (logical):

- **Input helpers** — specialized parsers
- **Venue / protocol** — campsite menu, parking zone, bike lock API
- **Payment adapters** — Web Payments / open-banking relays
- **Identity helpers** — alias providers

## Lifecycle

1. **Discover** — QR / NFC / URL / zone / BLE
2. **Resolve** — Context resolver selects plugin
3. **Render** — Plugin supplies state; core renders Compose UI
4. **Complete** — Pay / unlock / order
5. **Dispose** — One-shot wipe or Multi-day persist with TTL

```mermaid
flowchart LR
  Input[QR_NFC_URL_Zone_BLE_UWB] --> Resolver[ContextResolver]
  Resolver --> Plugin[CapabilityPlugin]
  Plugin --> UI[ComposeUI]
  Plugin --> Identity[MaskedIdentity]
  Plugin --> Pay[AnonymousPayments]
  Plugin --> Retention[OneShot_or_MultiDay]
```

## Isolation rules

- No proprietary Big Tech SDKs in core (no Play Services / Firebase)
- Plugins declare required permissions and whether network is needed
- Default: no WebView; if a plugin proposes HTML, it must be exceptional and reviewed
- Plugins must not read or send stable device IDs ([threat-model.md](threat-model.md))
- Plugins must not embed vendor surveillance SDKs

## Open protocol vs UI overwrite

| Mode | When |
|------|------|
| Open protocol adapter | Venue speaks a documented or reverse-understood capability API |
| UI overwrite | Vendor only offers a broken WebView/app; Decoy ignores HTML UI and speaks the minimum backend needed—or a Decoy-native URI |

Long-term preference: cooperative open protocols so vendors request claims (`paid`, `pitch 42`) instead of full PII forms.

## Testing hooks

Every plugin **must** run against fake/stub backends and fixtures so the full flow works on an **emulator** without live surveillance vendors or physical tags. Input ports (QR, NFC, BLE) must accept injection. See [e2e-testability.md](e2e-testability.md).

Live vendor/capability HTTP adapters arrive in **Phase 7** (explicit plugin + privacy-net). Until then, unknown public QR codes stay on the Unresolved screen — see the live API ladder in [development-phases.md](development-phases.md).

## Related docs

- [Product spec](product-spec.md)
- [Threat model](threat-model.md)
