# Public QR samples

Print these PNGs or open them on a second screen, then use **Scan** in Decoy.
On emulator without a camera, paste the payload via **Zone or link**.

| File | Payload | Expected |
|------|---------|----------|
| `decoy-campsite-order.png` | `decoy://campsite/order?fixture=1` | Campsite order session |
| `decoy-parking-4021.png` | `decoy://parking/zone=4021?fixture=1` | Parking timed session |
| `public-example-com.png` | `https://example.com/` | Unresolved (nothing sent) |
| `public-wikipedia-qr.png` | `https://en.wikipedia.org/wiki/QR_code` | Unresolved (nothing sent) |

Public `https://` samples are for decode/UX only — no vendor API call.
See [docs/public-qr-samples.md](../../docs/public-qr-samples.md).
