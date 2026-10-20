# Use cases

Each v1 case lists actor, trigger, flow, retention, PII policy, and privacy outcome.

Retention terms: **One-shot**, **Multi-day**, or Present-wallet rules ([data-lifecycle.md](data-lifecycle.md)).

---

## One-shot (v1)

### Shared bike / scooter unlock

| | |
|--|--|
| **Actor** | Rider |
| **Trigger** | QR, NFC, or BLE near lock |
| **Flow** | Resolve vehicle → Compose unlock UI → pay/auth out-of-band → unlock → dispose |
| **Retention** | One-shot |
| **PII policy** | No account if protocol allows; alias if contact required; no stable device ID |
| **Privacy outcome** | No vendor app install; session wiped after ride ends |

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
| **Flow** | Keypad or scan → session UI → pay → optional plate with consent → wipe |
| **Retention** | One-shot |
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
| **Flow** | Local pass wallet for event window; re-entry without re-account |
| **Retention** | Multi-day (event-bounded) |
| **PII policy** | Present token only; strip vendor app bloat |
| **Privacy outcome** | Event access without permanent festival account |

---

## Present — reverse direction (v1)

Opposite of venue discovery: the user presents a token *to* the world.

### Discount / loyalty codes

| | |
|--|--|
| **Actor** | Shopper |
| **Trigger** | User opens Present wallet |
| **Flow** | Pick code → fullscreen QR/barcode/text → cashier scans |
| **Retention** | Keep until delete (reusable) |
| **PII policy** | Store presentable token only; no lottery/loyalty app; no cloud sync by default |
| **Privacy outcome** | Show VriendenLoterij-style or store codes without tracker-heavy apps |
| **Example** | Reusable discount codes shown often at checkout |

### Ticket wallet

| | |
|--|--|
| **Actor** | Traveler / attendee |
| **Trigger** | User opens ticket; or import then present at gate |
| **Flow** | Import → store locally → fullscreen present offline → wipe after event + grace |
| **Retention** | Event-bounded Multi-day |
| **PII policy** | Presentable ticket payload only |
| **Privacy outcome** | One wallet for train/event/cinema tickets without each vendor app |

#### Import from other apps (required)

User-initiated only. Decoy is a share target and file handler—never scrapes another app’s sandbox.

| Path | Examples |
|------|----------|
| Share sheet (`SEND` / `VIEW`) | PDF, QR screenshot, text code, link |
| Open-with | `.pkpass`, PDF, barcode images |
| SAF picker | Downloads, email attachments |
| Camera / gallery | Re-scan QR on screen or photo |
| Clipboard | Paste code or URL |

**Non-goal:** silent import from Google/Samsung Wallet; Play-only Wallet APIs; embedding each vendor SDK.

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
