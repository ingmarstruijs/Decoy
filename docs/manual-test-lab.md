# Manual test lab (emulator)

The debug home screen is a **test lab**, not the final product UI. It fakes scans so you can exercise flows without a camera or NFC tag.

## Mental model

```text
Pick a scenario → Decoy opens a fake session → follow the blue “Next” hint → finish or Walk away
```

After you finish, you return to the lab. A **Last result** card shows payment status, which fields were kept/aliased/blocked/omitted, or vault status for stays.

## Recommended walkthroughs

### A. Hero order (campsite)

1. Tap **Start campsite order**
2. Read the field list: pitch kept, email aliased, IBAN blocked, home omitted
3. Select **Sandwich** → **Pay & order**
4. Back on home: Last result should show `Payment: Success` and `session wiped`

### B. Terrace

1. Tap **Start terrace order**
2. Order a beer → pay
3. Confirm wipe + field policy in Last result

### C. Multi-day stay

1. Tap **Start campsite stay**
2. Tap **Confirm stay (save vault)**
3. Last result / vault line shows encrypted stay with pitch

### D. Zone keypad

1. Tap chip **CAMP42** (or type it)
2. Tap **Resolve zone code**
3. Same as campsite order flow

### E. Walk away

1. Start any order scenario
2. Tap **Walk away — wipe without ordering**
3. Session gone; no successful payment in Last result

## adb (optional)

Same scenarios, no UI taps — see [dev-setup.md](dev-setup.md).
