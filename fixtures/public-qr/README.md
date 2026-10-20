# Public QR samples

Print these PNGs or open them on a second screen, then use **Scan** in Decoy.
On emulator without a camera, paste the payload via **Zone or link**.

| File | Payload | Expected |
|------|---------|----------|
| `decoy-campsite-order.png` | `decoy://campsite/order?fixture=1` | Campsite order session |
| `decoy-parking-4021.png` | `decoy://parking/zone=4021?fixture=1` | Parking timed session |
| `decoy-butlaroo-order.png` | `decoy://butlaroo/order?table=12&fixture=1` | Butlaroo UI-overwrite fixture (Compose order) |
| `public-example-com.png` | `https://example.com/` | Unresolved (nothing sent) |
| `public-wikipedia-qr.png` | `https://en.wikipedia.org/wiki/QR_code` | Unresolved (nothing sent) |

Paste `https://order.butlaroo.app/venue/demo?table=12` → Butlaroo plugin **Error** (capture needed), not Unresolved.
Public `https://` samples without a plugin stay decode/UX only — no vendor API call.
See [docs/public-qr-samples.md](../../docs/public-qr-samples.md) and [docs/butlaroo-adapter-spike.md](../../docs/butlaroo-adapter-spike.md).
