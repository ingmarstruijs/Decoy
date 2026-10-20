# Data lifecycle

Decoy never keeps interaction data permanently in a Decoy cloud. Retention is local and classified as **One-shot** or **Multi-day**.

**One-shot does not mean “wipe at payment.”** It means one active *session* that ends when the real-world activity ends—then data is destroyed. A bike rental stays alive until you return the bike; a parking session until you stop it; a sandwich order until the order completes.

## Default: auto-destroy

By default, every local context **auto-destroys**. The user may opt out (pin / extend) for a specific context; otherwise Decoy wipes on schedule.

| Default | Behavior |
|---------|----------|
| Auto-destroy **on** | Wipe when the session ends **and** when a safety TTL elapses (whichever comes first) |
| User opt-out | Explicit “keep longer” / pin for that context only; still never syncs to a Decoy cloud; still force-clearable |

Safety TTLs are visible in UI and testable with a fake clock ([e2e-testability.md](e2e-testability.md)).

## One-shot

One continuous local session for a single activity. Data may persist for minutes or hours while the activity is open; it must not become a lasting dossier afterward.

**Examples:** terrace drink order, bike rental (unlock → ride → return), parking session, captive-portal login, EV charge, vending pay.

| Rule | Detail |
|------|--------|
| Storage | Short-lived local state for the **active session** (may touch disk if the process dies mid-ride; encrypted) |
| Lifetime | From session start until **session end** (see below), not until first payment alone |
| Wipe triggers | Session end; user dismiss / force-clear; **safety TTL** if the session was abandoned; walk-away only when the activity is already complete |
| Cloud | None |
| Session ID | Ephemeral nonce; destroyed on wipe |
| Auto-destroy | **On by default**; user may pin a live session only while it is still active |

### Session end (examples)

| Activity | Session ends when |
|----------|-------------------|
| Bike / scooter | Ride ends / bike returned / lock closed — **not** at unlock payment alone |
| Parking | User stops the session (or operator end signal via plugin) |
| Terrace / campsite order | Order placed and confirmation done (or failed/cancelled) |
| Captive portal | Connect flow finished |
| EV charging | Charge stopped |
| Vending | Dispense confirmed or failed |

If the app is killed mid-bike-ride, Decoy may restore the **same** One-shot session from encrypted short-lived storage until return or safety TTL—not a new permanent account.

## Multi-day

Transient relationships that need local memory across days.

**Examples:** campsite stay with pitch + recurring orders, short-stay apartment access, festival pass across event days.

| Rule | Detail |
|------|--------|
| Storage | On-device encrypted store (hardware-backed keys when available) |
| Encryption | At rest; keys not exported |
| Wipe triggers | Stay/event end + grace; user delete; **default TTL auto-destroy** |
| Cloud | None by default |
| Local IDs | Random row keys; never uploaded as a Decoy user id |
| Auto-destroy | **On by default** (TTL); user may extend/pin until stay end, not forever |

## Mapping (summary)

| Use case class | Retention |
|----------------|-----------|
| Bike / terrace / parking / captive Wi-Fi / EV / vending | One-shot (session-scoped) |
| Campsite stay / apartment / festival pass | Multi-day |

Full scenarios: [use-cases.md](use-cases.md).

## Defaults (summary)

- Nothing permanent in a Decoy-operated cloud
- **Auto-destroy on by default** for both One-shot and Multi-day
- User can opt out per context (pin/extend); never silent infinite retention
- Prefer wipe-at-session-end over retain-just-in-case
- Safety TTLs always bound abandoned sessions
