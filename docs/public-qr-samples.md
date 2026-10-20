# Public QR samples

Decoy can **scan or paste** real QR payloads. Known `decoy://` fixtures open a session.
Unknown public URLs show **No Decoy capability yet** — decode stays on-device; nothing is sent to the vendor.

## In-repo samples

PNG files: [`fixtures/public-qr/`](../fixtures/public-qr/).

| Sample | Expected |
|--------|----------|
| Decoy campsite / parking / Butlaroo PNGs | Plugin session (Butlaroo = UI overwrite fixture) |
| `example.com` / Wikipedia QR wiki | Unresolved screen |
| Paste `https://order.butlaroo.app/…` | Butlaroo plugin Error (capture needed), not Unresolved |

## How to try

1. Product home → **Scan** (device camera), or **Zone or link** → paste payload
2. Emulator: paste from the table in `fixtures/public-qr/README.md`
3. Debug builds: **Demo fixtures** still inject without scanning

## Real Butlaroo venue QR

No public table QR is indexed online. Scan a real venue code when you have one — Decoy matches allowlisted hosts and shows capture-needed until live menu HTTP is filled in. See [butlaroo-adapter-spike.md](butlaroo-adapter-spike.md).

## Out of scope

- Live vendor checkout from arbitrary public QR codes ([development-phases.md](development-phases.md) live API ladder)
- Loading vendor HTML in a WebView
