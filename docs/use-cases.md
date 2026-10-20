# Use cases

Each v1 case lists actor, trigger, flow, retention, PII policy, and privacy outcome.

Retention terms: **One-shot** or **Multi-day** ([data-lifecycle.md](data-lifecycle.md)).

---

## One-shot (v1)

### Shared bike / scooter unlock

| | |
|--|--|
| **Actor** | Rider |
| **Trigger** | QR, NFC, or BLE near lock |
| **Flow** | Resolve vehicle → Compose unlock UI → pay/auth out-of-band → unlock → **keep session for the ride** → return/lock → wipe |
| **Retention** | One-shot (session = whole rental, not unlock alone); default auto-destroy TTL if abandoned |
| **PII policy** | No account if protocol allows; alias if contact required; no stable device ID |
| **Privacy outcome** | No vendor app install; session wiped after return (or safety TTL), not at first payment |

### Terrace order (table QR)

| | |
|--|--|
| **Actor** | Guest |
| **Trigger** | Table QR / URL |
| **Flow** | Parse locally → menu via minimal fetch or known plugin → Compose order UI → pay out-of-band → wipe |
| **Retention** | One-shot |
| **PII policy** | Keep table id; omit home address/account; alias contact if needed |
| **Privacy outcome** | No WebView trackers; no leftover order PII |

### Campsite food order (hero identity)

| | |
|--|--|
| **Actor** | Camper |
| **Trigger** | QR, URL, captive portal, or manual venue code |
| **Flow** | Native order UI → field policy → pay out-of-band → deliver to pitch |
| **Retention** | One-shot (single order); pitch may be Multi-day if part of a stay context |
| **PII policy** | **Keep** pitch. **Alias** email/phone. **Never** IBAN in form. **Omit** home address, marketing, forced signup |
| **Privacy outcome** | Sandwich without a civil profile |

### Parking session

| | |
|--|--|
| **Actor** | Driver |
| **Trigger** | Zone code, QR, or NFC |
| **Flow** | Keypad or scan → session UI → pay → optional plate with consent → **active until stop** → wipe |
| **Retention** | One-shot (session-scoped); default auto-destroy TTL if abandoned |
| **PII policy** | Real plate only with explicit consent when required; never a fake plate; local wipe |
| **Privacy outcome** | Minimal digital trail; cooperative hash/ZKP is future protocol |

### Captive portal Wi-Fi

| | |
|--|--|
| **Actor** | Guest (hotel / campsite / train) |
| **Trigger** | Captive portal / local HTTP |
| **Flow** | Detect portal → Compose accept/connect UI → no marketing dossier → wipe |
| **Retention** | One-shot |
| **PII policy** | Alias if email required; omit surplus fields |
| **Privacy outcome** | Connectivity without permanent Wi-Fi account |

### EV charging start/stop

| | |
|--|--|
| **Actor** | Driver |
| **Trigger** | QR / NFC at charger |
| **Flow** | Session UI → pay → start/stop → wipe |
| **Retention** | One-shot |
| **PII policy** | No vendor app account if avoidable; alias if required |
| **Privacy outcome** | Charge without installing operator app |

### Vending / locker / laundry QR pay

| | |
|--|--|
| **Actor** | Customer |
| **Trigger** | QR / NFC |
| **Flow** | Select → pay out-of-band → confirm → wipe |
| **Retention** | One-shot |
| **PII policy** | No account; no home address |
| **Privacy outcome** | Machine use without loyalty lock-in |

### Manual keypad (trigger variant)

When no tag is present, the user enters a zone or short code. Same retention and PII rules as the resolved use case (parking, venue menu, etc.). Processing stays local until a capability fetch is required.

---

## Multi-day (v1)

### Campsite stay

| | |
|--|--|
| **Actor** | Camper |
| **Trigger** | Check-in QR/code; pitch assigned |
| **Flow** | Store encrypted pitch + stay window; recurring food orders reuse context; TTL wipe |
| **Retention** | Multi-day |
| **PII policy** | Pitch kept locally; aliases for contact; no home address for on-site services |
| **Privacy outcome** | Stay works without a permanent campsite account |

### Short-stay apartment / cabin

| | |
|--|--|
| **Actor** | Guest |
| **Trigger** | Booking code, NFC, or URL |
| **Flow** | Local access context for door/thermostat across days → wipe after stay |
| **Retention** | Multi-day |
| **PII policy** | Access tokens local; no Decoy cloud |
| **Privacy outcome** | Access without installing host’s surveillance app |

### Festival / venue pass

| | |
|--|--|
| **Actor** | Attendee |
| **Trigger** | NFC / QR pass; top-ups |
| **Flow** | Local pass context for event window; re-entry / top-up without re-account |
| **Retention** | Multi-day (event-bounded) |
| **PII policy** | Pass credential local only; strip vendor app bloat |
| **Privacy outcome** | Event access without permanent festival account |

Generic ticket or loyalty **import-and-show** wallets are out of scope — see [non-goals.md](non-goals.md).

---

## Backlog (named only)

- Beach chair / umbrella rental
- Museum or attraction on-site ticket purchase
- Parcel locker pickup
- Coworking room booking
- Fuel pump app replacement
- Rich Matter/Thread home control
- Toll / congestion (regulated — extreme caution)

---

## Related docs

- [Product spec](product-spec.md)
- [Data lifecycle](data-lifecycle.md)
- [Threat model](threat-model.md)
