# Payments

Decoy pays **out-of-band**. Vendor HTML/forms never receive IBAN, card PAN, or bank account numbers.

## Adapters

| Backend | Id | When |
|---------|-----|------|
| `FakePaymentAdapter` | `fake` | **CI and app default** |
| `OpenBankingRelayAdapter` + staging client | `open-banking-relay` | Debug Demo / manual staging verification |

Both implement `PaymentPort`. The Compose shell and order/timed flows call the same port; only the backend switches.

### Staging open-banking relay

Phase 4 ships an **in-process** staging relay that models authorize → settle (Web Payments / open-banking shape). It does **not** talk to a production PSP.

**Phase 4b** adds a real HTTP relay / open-banking path behind the same port; CI stays Fake-default. See [development-phases.md](development-phases.md).

Debug Demo: **Switch payment backend**.  
adb: `--es nl.decoy.app.extra.PAYMENT_BACKEND staging`

## Invariants

1. `FieldKind.PayElsewhere` fields (e.g. IBAN) are never collected into outbound vendor payloads
2. Order flow strips sneaked `iban` / `card` contextual keys before classify
3. `FieldClassifier.assertNoSecrets` fails the build if secrets appear in outbound
4. Residual PSP/bank linkability is **not** removed — see [threat-model.md](threat-model.md)

## Related

- [Product spec — Payments](product-spec.md#payments)
- [Threat model](threat-model.md)
- [Manual testing](manual-test-lab.md)
