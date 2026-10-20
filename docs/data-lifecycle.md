# Data lifecycle

Decoy never keeps interaction data permanently in a Decoy cloud. Retention is local and classified as **One-shot** or **Multi-day** (plus a Present-wallet variant for reusable codes).

## One-shot

Instant actions that should leave no lasting digital dossier on the device.

**Examples:** terrace drink order, bike unlock, parking session, captive-portal login, vending pay.

| Rule | Detail |
|------|--------|
| Storage | Volatile / short-lived local state for the active context only |
| Encryption | Prefer in-memory; if briefly persisted, encrypted and deleted on wipe |
| Wipe triggers | Transaction complete, user dismisses, or user walks away from context |
| Cloud | None |
| Session ID | Ephemeral nonce; destroyed on wipe |

## Multi-day

Transient relationships that need local memory for a bounded window.

**Examples:** campsite stay with pitch + recurring orders, short-stay apartment access, festival pass across event days.

| Rule | Detail |
|------|--------|
| Storage | On-device encrypted store (hardware-backed keys when available) |
| Encryption | At rest; keys not exported |
| Wipe triggers | Pre-defined TTL (days), user delete, or stay/event end + grace |
| Cloud | None by default |
| Local IDs | Random row keys; never uploaded as a Decoy user id |

## Present wallet

Reverse-direction items the user chooses to keep for showing at checkout or gates.

| Kind | Retention |
|------|-----------|
| Discount / loyalty codes | Multi-day or **keep until I delete** (reusable) |
| Tickets | Until event end + short grace, then wipe |

Tokens are presentable payloads only; no Decoy-cloud sync by default.

## Mapping (summary)

| Use case class | Retention |
|----------------|-----------|
| Bike / terrace / parking / captive Wi-Fi / EV / vending | One-shot |
| Campsite stay / apartment / festival pass | Multi-day |
| Loyalty codes | Keep until delete |
| Tickets | Event-bounded Multi-day |

Full scenarios: [use-cases.md](use-cases.md).

## Defaults

- Nothing permanent in a Decoy-operated cloud
- Prefer wipe-early over retain-just-in-case
- Multi-day TTLs are visible to the user and force-clearable
