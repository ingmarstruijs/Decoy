# End-to-end testability

Prove Decoy **fully in the Android emulator** with mocks and stubs. No real surveillance backends, no physical NFC/UWB hardware, and no production PSPs are required for CI or day-to-day development.

## Goals

- Every v1 use case has a deterministic **emulator** path
- All external I/O is behind ports that accept mocks/stubs
- Privacy properties are asserted in automation, not only documented
- A developer can exercise hero flows on an AVD without leaving the desk

## Emulator-first rule

| Rule | Meaning |
|------|---------|
| Default runtime in debug/CI | Mock/stub backends and injected inputs |
| Live network vendors | Opt-in only; never required to merge |
| Hardware (NFC, BLE, UWB, camera QR) | Emulated or injected; physical devices optional |
| Payments | Fake adapter in emulator; staging PSP optional later |

If a feature cannot be completed on an emulator with mocks, it is **not done**.

## What must be mockable

| Surface | Emulator approach |
|---------|-------------------|
| QR / camera | Inject decoded payload or fixture image URI (no real scan required) |
| URL / deep link | `adb shell am start` with intent extras / App Links test intents |
| NFC | Inject tag payload via test API or emulator NFC simulation |
| Manual zone keypad | UI Automator / Compose test input |
| BLE / UWB | Stub advertisement stream in `:fixtures` |
| Captive portal | Local mock HTTP server on emulator loopback (or instrumented fake) |
| Venue / menu / parking APIs | Plugin ports + stub implementations returning fixture JSON |
| Payments | `FakePaymentAdapter` (success/fail/cancel) |
| Time / TTL (One-shot safety wipe + Multi-day wipe) | Controllable clock in tests |
| Alias email | Deterministic generator (no real mailbox required in CI) |

## Strategy

### Plugin contract + fakes

- Each capability plugin defines a narrow port (menu, session, unlock, portal)
- Tests and debug builds inject fake backends with fixed JSON/fixture payloads
- No live campsite / parking / operator servers in CI
- `:fixtures` module owns shared payloads and stub servers

### Debug / demo harness

Ship a **debug-only** entry (hidden or flagged) that:

- Lists fixture scenarios (campsite order, terrace, parking zone `4021`, …)
- Starts a flow with one tap (injects input + binds stubs)
- Shows whether privacy interceptors would block a request

Release/F-Droid builds omit this harness or compile it out.

### Deterministic fixtures

| Fixture | Covers |
|---------|--------|
| Zone `4021` + tariff table | Parking One-shot |
| Table QR → menu JSON | Terrace order |
| Campsite order form schema | Hero field policy (omit address, alias email, block IBAN) |
| Captive portal HTML markers | Portal detect without loading trackers |
| Injected NFC UID + NDEF | NFC resolve without hardware |
| Stub BLE advertisement | Ambient resolve without radio |

### Privacy assertions

Automated checks must fail the build if violated:

| Assertion | Layer |
|-----------|-------|
| No WebView load on QR happy path for known plugins | Instrumented / e2e on emulator |
| No cookies persisted across One-shot sessions | Unit + e2e |
| No GPS in cleartext request URLs/bodies | Network interceptor tests |
| No `ANDROID_ID` / GAID / install-UUID in outbound traffic | Interceptor + CI API grep |
| One-shot wipe on session end leaves no PII in local DB/prefs | Instrumented on emulator |
| One-shot safety TTL wipes abandoned sessions (e.g. bike never returned in app) | Unit + instrumented (fake clock) |
| Multi-day TTL auto-destroy deletes records; pin/extend opt-out honored until stay end | Unit + instrumented (fake clock) |
| Flow completable with stubs only (no WAN) | Instrumented on emulator (airplane mode optional assert) |

### Suggested layers

1. **Unit** — field classifier, QR parse, retention timers, alias generation
2. **Instrumented UI (emulator)** — Compose flows with fake plugins
3. **E2E plugin scenarios (emulator)** — full discover → UI → complete → dispose with fixtures
4. **Privacy network suite** — interceptor + optional local mock server; block cleartext leaks

## Acceptance checklist (per use case)

A use case is “done” for testability when:

- [ ] Fixture exists and is documented in `:fixtures`
- [ ] Fake/stub backend covers success and hard-require-surplus-fields failure
- [ ] Full path runs on **emulator** with no physical hardware
- [ ] Full path runs with stubs only (no dependency on production vendor hosts)
- [ ] Retention class asserted (One-shot wipe or Multi-day TTL)
- [ ] PII policy asserted (keep / alias / omit / pay-elsewhere)
- [ ] No WebView / no stable device IDs on the happy path
- [ ] Linked from [use-cases.md](use-cases.md)
- [ ] Reachable from debug/demo harness (or equivalent scripted `adb` intent)

## Out of scope for CI

- Real payment settlement against production PSPs (fake required; staging optional manual)
- Physical NFC/UWB/BLE hardware farms (injection/stubs required instead)
- Real operator/venue production APIs

## Related docs

- [Development phases](development-phases.md)
- [Use cases](use-cases.md)
- [Plugins architecture](plugins-architecture.md)
- [Threat model](threat-model.md)
