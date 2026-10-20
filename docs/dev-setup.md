# Dev setup (Phase 0)

Do not commit on `main`. Use a feature branch and PR — see [branching.md](branching.md).

## Requirements

- JDK 17+
- Android SDK (emulator recommended)
- Gradle Wrapper (`./gradlew`)

## Build & unit tests

Same commands as CI (`.github/workflows/ci.yml`):

```bash
./gradlew test
./gradlew :app:assembleDebug
```

Banned API gate (PowerShell / `pwsh`):

```powershell
./scripts/check-banned-apis.ps1
```

## Emulator smoke (no camera)

1. Start an AVD and install the debug build:

```bash
./gradlew :app:installDebug
```

2. Launch and inject a fixture QR via `adb` (no camera hardware):

```bash
# Phase 1 hero — campsite sandwich (field policy)
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity -a nl.decoy.app.INJECT_QR --es nl.decoy.app.extra.QR_PAYLOAD "decoy://campsite/order?fixture=1"

# Terrace table
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity -a nl.decoy.app.INJECT_QR --es nl.decoy.app.extra.QR_PAYLOAD "decoy://terrace/table=7?fixture=1"

# Phase 0 hello
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity -a nl.decoy.app.INJECT_QR --es nl.decoy.app.extra.QR_PAYLOAD "decoy://hello?fixture=1"

# Phase 2 — NFC / zone keypad (no hardware)
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.NFC_PAYLOAD "decoy://campsite/order?nfc=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.ZONE_CODE "STAY42"

# Phase 4 — staging open-banking relay (still no production PSP), then campsite order
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.PAYMENT_BACKEND staging --es nl.decoy.app.extra.QR_PAYLOAD "decoy://campsite/order?fixture=1"

# Phase 5 — ambient BLE / UWB inject (no radio hardware)
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.BLE_AD "decoy|ble|campsite.order|fixture=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.UWB_AD "decoy|uwb|campsite.order|fixture=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.QR_PAYLOAD "decoy://cabin/unit=3?fixture=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.ZONE_CODE "PASS1"

# Phase 3 — One-shot catalog
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.QR_PAYLOAD "decoy://parking/zone=4021?fixture=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.ZONE_CODE "4021"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.QR_PAYLOAD "decoy://portal/guest?fixture=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.QR_PAYLOAD "decoy://ev/charger=EV-1?fixture=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.QR_PAYLOAD "decoy://vending/locker=A3?fixture=1"
```

Or use the in-app **Debug test lab** (scenario cards). Step-by-step: [manual-test-lab.md](manual-test-lab.md).

3. Follow the on-screen **Next** hint. Orders: **Pay & order** → wipe. Timed (parking/EV): **Pay & start** → **Stop & wipe**. Stay: **Confirm stay** → vault. **Walk away** wipes without finishing.

## Modules

| Module | Role |
|--------|------|
| `:app` | Compose shell + debug inject entry |
| `:core` | ContextResolver + retention |
| `:privacy-net` | OkHttp privacy defaults + ID bans |
| `:plugins-api` | Plugin contract |
| `:fixtures` | Hello fixture plugin + catalog |
