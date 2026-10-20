# Manual test lab (emulator)

The debug home screen is a **test lab**, not the final product UI. It fakes scans so you can exercise flows without a camera or NFC tag.

## Mental model

```text
Pick a scenario → Decoy opens a fake session → follow the blue “Next” hint → finish or Walk away
```

After you finish, you return to the lab. A **Last result** card shows payment status, which fields were kept/aliased/blocked/omitted, or vault status for stays.

## Payment backend

CI always uses **fake**. In the lab, tap **Switch payment backend** to use the **staging open-banking relay** (in-process mock, no production PSP). Last result shows `via open-banking-relay` and a relay reference. Details: [payments.md](payments.md).

## Recommended walkthroughs

### A. Hero order (campsite)

1. Tap **Start campsite order**
2. Read the field list: pitch kept, email aliased, IBAN blocked, home omitted
3. Select **Sandwich** → **Pay & order**
4. Back on home: Last result should show `Payment: Success` and `session wiped`

### A2. Hero order via staging relay

1. Tap **Switch payment backend** until label says staging open-banking relay
2. Run campsite order as in A
3. Last result: `via open-banking-relay`, reference `obr-staging-…`, IBAN still blocked

### B. Terrace

1. Tap **Start terrace order**
2. Order a beer → pay
3. Confirm wipe + field policy in Last result

### C. Multi-day stay

1. Tap **Start campsite stay**
2. Tap **Confirm stay (save vault)**
3. Last result / vault line shows encrypted stay with pitch

### D. Parking zone 4021

1. Tap **Start parking session** (or keypad chip **4021** → Resolve)
2. Optionally consent + enter a plate (without consent, plate is omitted)
3. **Pay & start parking** — session stays open
4. **Stop parking & wipe** — Last result shows wipe; plate only in Kept if you consented

### E. Captive portal Wi-Fi

1. Tap **Start captive portal** (or chip **WIFI**)
2. **Accept & connect**
3. Last result: email aliased, marketing/account omitted, session wiped

### F. EV charging

1. Tap **Start EV charging** (or chip **EV1**)
2. **Pay & start charging** → **Stop charging & wipe**

### G. Vending / locker

1. Tap **Start vending pay** (or chip **VEND1**)
2. Pick an item → **Pay & dispense** → wipe

### H. Zone keypad

1. Tap chip **CAMP42** (or type it)
2. Tap **Resolve zone code**
3. Same as campsite order flow

### I. Walk away

1. Start any order or timed scenario (before stop)
2. Tap **Walk away**
3. Session gone; no successful completed wipe from pay path

## adb (optional)

Same scenarios, no UI taps — see [dev-setup.md](dev-setup.md).
