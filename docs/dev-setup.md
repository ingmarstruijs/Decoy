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
```

Or open the app and tap **Inject campsite order QR** / **Inject terrace table QR**.

3. Pick a menu item → **Pay & order** (fake payment) → session wiped. Confirm IBAN/home are shown as blocked/omitted, not collected.

## Modules

| Module | Role |
|--------|------|
| `:app` | Compose shell + debug inject entry |
| `:core` | ContextResolver + retention |
| `:privacy-net` | OkHttp privacy defaults + ID bans |
| `:plugins-api` | Plugin contract |
| `:fixtures` | Hello fixture plugin + catalog |
