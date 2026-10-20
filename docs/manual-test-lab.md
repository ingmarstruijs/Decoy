# Manual testing (product home + Demo)

The app opens on the **product home** (ready to scan), not a scenario dashboard.

## Mental model

```text
Scan or paste → session UI → finish / Walk away → home
```

Debug builds also expose **Demo fixtures** (overflow on home) to inject discovery without camera or radio.

## Product paths

### Scan

1. Tap **Scan**
2. Allow camera if prompted (on-device decode only)
3. Point at a printed sample from [`fixtures/public-qr/`](../fixtures/public-qr/)
4. Known `decoy://` → session; public `https://` → **No Decoy capability yet**

### Zone or link

1. Tap **Zone or link**
2. Enter `CAMP42` / `4021` or paste a URL / `decoy://…` payload
3. Continue

### Multi-day

1. Demo → Campsite stay / Cabin / Festival, or paste stay QR
2. Confirm into vault
3. Home shows **Active stays**
4. Tap **End** → stay leaves Active stays and appears under **Recent**

### Timed (parking / EV)

1. Start via Demo or scan/paste
2. Pay & start → Stop when done (session wipes)
3. Home **Recent** shows the parking breadcrumb (Clear wipes the list)

## Demo fixtures (debug only)

Open **Demo fixtures** on home. Grouped One-shot / Multi-day / Ambient inject buttons, plus payment backend toggle (fake vs staging open-banking relay).

Release builds omit Demo.

## Walk away

From any session, **Walk away** wipes the One-shot UI session without completing pay/confirm.

## adb

See [dev-setup.md](dev-setup.md). Public samples: [public-qr-samples.md](public-qr-samples.md).
