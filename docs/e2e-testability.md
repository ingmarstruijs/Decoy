# End-to-end testability

Prove Decoy without talking to real surveillance backends.

## Goals

- Every v1 use case has a deterministic fixture path
- Privacy properties are asserted in automation, not only documented
- Plugins are testable behind fakes/stubs

## Strategy

### Plugin contract + fakes

- Each capability plugin defines a narrow port (menu, session, unlock, portal)
- Tests inject fake backends with fixed JSON/fixture payloads
- No live campsite / parking / lottery servers in CI

### Deterministic fixtures

Examples:

| Fixture | Covers |
|---------|--------|
| Zone `4021` + tariff table | Parking One-shot |
| Table QR → menu JSON | Terrace order |
| Campsite order form schema | Hero field policy (omit address, alias email, block IBAN) |
| Captive portal HTML markers | Portal detect without loading trackers |
| Sample `.pkpass` / PDF / QR PNG | Ticket wallet import |
| Discount code list | Present loyalty codes |

### Privacy assertions

Automated checks must fail the build if violated:

| Assertion | Layer |
|-----------|-------|
| No WebView load on QR happy path for known plugins | Instrumented / e2e |
| No cookies persisted across One-shot sessions | Unit + e2e |
| No GPS in cleartext request URLs/bodies | Network interceptor tests |
| No `ANDROID_ID` / GAID / install-UUID in outbound traffic | Interceptor + CI API grep |
| One-shot wipe leaves no PII in local DB/prefs | Instrumented |
| Multi-day TTL expiry deletes records | Unit + instrumented |
| Share-import does not require vendor SDK | Compile / module graph |

### Suggested layers

1. **Unit** — field classifier, QR parse, retention timers, alias generation
2. **Instrumented UI** — Compose flows with fake plugins
3. **E2E plugin scenarios** — full discover → UI → complete → dispose with fixtures

## Acceptance checklist (per use case)

A use case is “done” for testability when:

- [ ] Fixture exists and is documented
- [ ] Fake/stub backend covers success and hard-require-surplus-fields failure
- [ ] Retention class asserted (One-shot wipe or Multi-day TTL)
- [ ] PII policy asserted (keep / alias / omit / pay-elsewhere)
- [ ] No WebView / no stable device IDs on the happy path
- [ ] Linked from [use-cases.md](use-cases.md)

## Out of scope for CI

- Real payment settlement against production PSPs (use payment fakes; optional manual staging)
- Physical NFC/UWB hardware farms (use injected input events)

## Related docs

- [Use cases](use-cases.md)
- [Plugins architecture](plugins-architecture.md)
- [Threat model](threat-model.md)
