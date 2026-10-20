# Dev setup

## Requirements

- JDK 17+
- Android SDK / emulator
- Gradle Wrapper

## Build & test

```bash
./gradlew test
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

Banned APIs (PowerShell):

```powershell
./scripts/check-banned-apis.ps1
```

## Run on emulator / device

```bash
./gradlew :app:installDebug
```

Product home: **Scan**, **Zone or link**, or (debug) **Demo fixtures**.

### Inject without camera

```bash
# Campsite order
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.QR_PAYLOAD "decoy://campsite/order?fixture=1"

# Zone
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.ZONE_CODE "4021"

# Staging payment backend + order
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.PAYMENT_BACKEND staging --es nl.decoy.app.extra.QR_PAYLOAD "decoy://campsite/order?fixture=1"

# Ambient BLE / UWB
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.BLE_AD "decoy|ble|campsite.order|fixture=1"
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.UWB_AD "decoy|uwb|campsite.order|fixture=1"

# Paste-equivalent public URL (Unresolved)
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity --es nl.decoy.app.extra.QR_PAYLOAD "https://example.com/"
```

Walkthroughs: [manual-test-lab.md](manual-test-lab.md). Public QR PNGs: [public-qr-samples.md](public-qr-samples.md).

## Modules

| Module | Role |
|--------|------|
| `:app` | Product shell (Compose), FOSS QR scan, Demo (debug) |
| `:core` | ContextResolver, retention, payments, Multi-day vault |
| `:privacy-net` | OkHttp privacy defaults + ID bans |
| `:plugins-api` | Plugin contract + discovery inputs |
| `:fixtures` | Fixture plugins, ambient ads, public-qr samples |

## Related

- [Development phases](development-phases.md)
- [Branching](branching.md)
- [F-Droid posture](fdroid.md)
