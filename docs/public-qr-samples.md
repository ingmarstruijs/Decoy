# Public QR samples

Decoy can **scan or paste** real QR payloads. Known `decoy://` fixtures open a session.
Unknown public URLs show **No Decoy capability yet** — decode stays on-device; nothing is sent to the vendor.

## In-repo samples

PNG files: [`fixtures/public-qr/`](../fixtures/public-qr/).

| Sample | Expected |
|--------|----------|
| Decoy campsite / parking PNGs | Plugin session |
| `example.com` / Wikipedia QR wiki | Unresolved screen |

## How to try

1. Product home → **Scan** (device camera), or **Zone or link** → paste payload
2. Emulator: paste from the table in `fixtures/public-qr/README.md`
3. Debug builds: **Demo fixtures** still inject without scanning

## Out of scope

- Live vendor checkout from arbitrary public QR codes ([development-phases.md](development-phases.md) live API ladder)
- Loading vendor HTML in a WebView
