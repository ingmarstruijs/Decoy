# Development phases

Ordered build path from the specs to a first F-Droid release. Each phase has an outcome, work items, tests, non-goals, and exit criteria.

Aligned with [product-spec.md](product-spec.md), [use-cases.md](use-cases.md), [plugins-architecture.md](plugins-architecture.md), [e2e-testability.md](e2e-testability.md), and [threat-model.md](threat-model.md).

## Stack

| Choice | Value |
|--------|-------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Shape | Multi-module Android app |
| Distribution target | F-Droid |
| Forbidden | Google Play Services, Firebase, proprietary analytics |

## Phase overview

After **Phase 0 (Foundation)**, **Phase 1** proves the core with the campsite/terrace hero order flow on fixtures. Later phases harden inputs, expand One-shot fixtures, add real payments, Multi-day surfaces, then F-Droid release.

Generic ticket/loyalty import-and-show wallets are **out of scope** (see [non-goals.md](non-goals.md)).

```mermaid
flowchart TB
  P0[Phase0_Foundation]
  P0 --> P1[Phase1_HeroOrder]
  P1 --> P2[Phase2_Inputs_Identity_Retention]
  P2 --> P3[Phase3_OneShot_Catalog]
  P3 --> P4[Phase4_Payments]
  P4 --> P5[Phase5_Ambient_MultiDay]
  P5 --> P6[Phase6_FDroid_Hardening]
  P6 --> P7[Phase7_Cooperative_Protocols]
```

## Cross-cutting rules

Apply in every phase:

- **Emulator-first:** every feature must be fully exercisable on an AVD with mocks/stubs — see [e2e-testability.md](e2e-testability.md)
- Fixture/fake backends before any live vendor integration; CI never depends on production vendor hosts
- Debug/demo harness (or scripted `adb` intents) to start fixture flows with one action
- Injected inputs for QR, NFC, BLE (no physical hardware required to merge)
- No Play Services / Firebase / analytics SDKs in core or plugins
- Phase exit = features **plus** privacy tests on emulator — not a UI demo alone
- No stable device IDs; empty cookie jar; no vendor WebView on happy paths ([threat-model.md](threat-model.md))

---

## Phase 0 — Foundation

**Outcome:** product shell that enforces privacy defaults and can host plugins.

### Modules (initial sketch)

| Module | Role |
|--------|------|
| `:app` | Compose shell, navigation, entry points |
| `:core` | Context resolver, session lifecycle, retention orchestration |
| `:privacy-net` | HTTP client, interceptors, ID bans |
| `:plugins-api` | Plugin contract (discover → resolve → render → complete → dispose) |
| `:fixtures` | Deterministic payloads, stub backends, injected-input helpers for emulator CI |

Names may be refined at implement time; responsibilities must not blur.

### Work

- Compose app shell; **no** WebView dependency in core
- Privacy HTTP client: empty cookie jar, no stable device IDs, interceptor hooks
- Plugin API lifecycle as in [plugins-architecture.md](plugins-architecture.md)
- Retention primitives: One-shot session nonce + wipe; Multi-day store **interface** (stub implementation OK)
- **Mock harness:** dependency injection seams for network, payments, and input sources; debug build can bind all stubs
- CI: unit tests + instrumented emulator smoke + banned-API grep (`AdvertisingIdClient`, Play/Firebase imports)

### Tests

- App launches on emulator
- Hello fake plugin renders a Compose screen via stub binding
- Injected “QR” payload resolves without camera hardware
- CI fails on banned imports / ID APIs

### Non-goals

- Real venue integrations
- Ticket/loyalty code wallets
- Real payments

### Exit criteria

- [x] App launches on emulator (`:app:assembleDebug` builds; install via `installDebug`)
- [x] Fake plugin shows Compose UI via plugin API with stubs only (`HelloFixturePlugin`)
- [x] Privacy CI gates green (`./gradlew test` + `scripts/check-banned-apis.ps1`)
- [x] Documented how to run the smoke flow on an AVD ([dev-setup.md](dev-setup.md))

---

## Phase 1 — Hero order flow

**Outcome:** core architecture proven on campsite / terrace ordering with fixtures only.

### Work

- QR + URL parse → ContextResolver → fixture plugins (no live vendors)
- Compose order UI
- Field classifier: needed / alias / pay-elsewhere / omit
- **Campsite sandwich hero:** keep pitch; alias email; block IBAN in forms; omit home address
- **Terrace table-QR:** menu fixture → order → complete
- Payment **fake** adapter (success / fail only)
- One-shot wipe on complete or dismiss

### Tests

- Fixtures: campsite form schema, terrace menu JSON
- Privacy assertions: no WebView, no cookies across sessions, no GPS/stable IDs in requests
- Wipe leaves no PII in local DB/prefs
- [e2e-testability.md](e2e-testability.md) acceptance checklist for these use cases

### Non-goals

- Live campsite/restaurant backends
- Real PSP settlement
- Multi-day stay product (interface only if needed for pitch context)
- Import/show code or ticket wallet

### Exit criteria

- [x] Campsite and terrace fixture flows complete end-to-end **on emulator with stubs**
- [x] Field policy assertions pass (keep / alias / omit / pay-elsewhere)
- [x] One-shot wipe + privacy network assertions green
- [x] Both flows reachable from debug/demo harness (or `adb` intent)

---

## Phase 2 — Inputs, identity, retention hardening

**Outcome:** core MVP inputs and durable local Multi-day vault.

### Work

- NFC + manual zone / short-code keypad through ContextResolver
- Alias generator (local generation; configurable relay later)
- Multi-day encrypted vault (e.g. campsite stay pitch context)
- Walk-away / dismiss dispose behavior for active contexts

### Tests

- Keypad and NFC (injected events) trigger hero plugin
- Multi-day TTL unit + instrumented expiry/delete tests
- Alias used for contact fields; no global Decoy user id uploaded

### Non-goals

- BLE / UWB (Phase 5)
- Real alias email relay hosting (document config only if needed)

### Exit criteria

- [x] QR, URL, NFC, keypad all resolve into plugins
- [x] Multi-day vault encrypts and TTL-wipes
- [x] Dispose on walk-away/dismiss verified

---

## Phase 3 — One-shot catalog (fixture-first)

**Outcome:** remaining v1 One-shot use cases runnable on fakes.

### Work

Fake plugins + fixtures for:

- Parking (zone / QR)
- Captive portal Wi-Fi
- EV charging start/stop stub
- Vending / locker / laundry QR pay

Manual keypad remains a shared trigger variant. Each case meets the acceptance checklist in [e2e-testability.md](e2e-testability.md).

### Tests

- Per-use-case fixture + fake success path
- Hard-require-surplus-fields failure path where relevant
- Retention = One-shot wipe asserted

### Non-goals

- Live operator APIs
- Toll / congestion (backlog; regulated)

### Exit criteria

- [ ] All listed One-shot v1 cases have fixtures and green e2e checklists
- [ ] Linked from [use-cases.md](use-cases.md)

---

## Phase 4 — Payments

**Outcome:** one real open payment path behind the same port as the fake.

### Work

- Payment port already used by the fake; add Web Payments / open-banking relay adapter
- Never paste IBAN/card into vendor HTML forms (product + test invariant)
- Keep fakes as CI default; staging path for manual verification
- Residual PSP/bank linkability remains documented in [threat-model.md](threat-model.md)

### Tests

- Adapter contract tests (fake + staging mock)
- Assertion: order flow never places IBAN into vendor field payloads
- CI still uses fake payment

### Non-goals

- Supporting every PSP
- Claiming payment anonymity stronger than the threat model allows

### Exit criteria

- [ ] One staging payment path works for hero order
- [ ] CI remains fake-default and green
- [ ] IBAN-in-form ban enforced in tests

---

## Phase 5 — Ambient + Multi-day product surfaces

**Outcome:** ambient discovery and full Multi-day product flows.

### Work

- BLE proximity (local-only; never as cloud location telemetry)
- Captive portal polish
- Multi-day flows: campsite stay, short-stay apartment/cabin, festival/venue pass
- UWB when hardware allows — same UX contract as BLE

### Tests

- Multi-day stay + One-shot coexist without cross-leaking session/device IDs
- BLE resolution uses fixtures/injected advertisements in CI
- TTL wipe for stay/pass contexts

### Non-goals

- Play-only Nearby APIs
- Cloud geolocation
- Rich Matter/Thread home control (backlog)
- Generic ticket/loyalty import-and-show wallet

### Exit criteria

- [ ] BLE-triggered resolve works in instrumented tests
- [ ] Multi-day use cases from [use-cases.md](use-cases.md) meet e2e checklist
- [ ] No ID leakage across retention classes

---

## Phase 6 — F-Droid hardening and release

**Outcome:** first public FOSS release.

### Work

- F-Droid metadata / reproducible build posture
- Full proprietary-dependency audit
- Privacy regression suite as release gate
- Release notes pointing at manifesto + docs

### Tests

- Full privacy suite green on release tag
- Dependency audit clean (no Play/Firebase/analytics)

### Non-goals

- Play Store listing as primary distribution

### Exit criteria

- [ ] F-Droid-ready artifact published (or submitted)
- [ ] Release gate checklist signed off

---

## Phase 7 — Cooperative protocols (later)

**Outcome:** venues that speak Decoy-native capability claims.

### Work

- Venue requests claims such as `paid`, `pitch 42` instead of full PII forms
- Optional cryptographic / ZKP-style verification **only** where verifiers cooperate
- Document migration path from UI-overwrite plugins to open adapters

### Non-goals

- Required for first F-Droid release (M4)
- Fabricating regulated identifiers

### Exit criteria

- [ ] At least one cooperative fixture + one documented venue protocol sketch
- [ ] Threat model updated if guarantees change

---

## Milestones

| Milestone | Phases | Ship signal |
|-----------|--------|-------------|
| **M0** | 0 | Privacy shell + plugin hello |
| **M1** | 0 + 1 | Hero order on fixtures (campsite + terrace) |
| **M2** | 2 + 3 | Core inputs + several One-shot fakes |
| **M3** | 4 + 5 | Payments + Multi-day stays |
| **M4** | 6 | F-Droid release |
| **M5** | 7 | Cooperative protocols (post-MVP) |

## Related docs

- [Product spec](product-spec.md)
- [Use cases](use-cases.md)
- [Plugins architecture](plugins-architecture.md)
- [End-to-end testability](e2e-testability.md)
- [Threat model](threat-model.md)
- [Data lifecycle](data-lifecycle.md)
- [Non-goals](non-goals.md)
