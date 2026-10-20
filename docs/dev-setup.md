# Dev setup (Phase 0)

Do not commit on `main`. Use a feature branch and PR — see [branching.md](branching.md).

## Requirements

- JDK 17+
- Android SDK (emulator recommended)
- Gradle Wrapper (`./gradlew`)

## Build & unit tests

```bash
./gradlew test
./gradlew :app:assembleDebug
```

Banned API gate (PowerShell):

```powershell
./scripts/check-banned-apis.ps1
```

## Emulator smoke (no camera)

1. Start an AVD and install the debug build:

```bash
./gradlew :app:installDebug
```

2. Launch and inject the hello fixture QR via `adb` (no camera hardware):

```bash
adb shell am start -n nl.decoy.app.debug/nl.decoy.app.MainActivity -a nl.decoy.app.INJECT_QR --es nl.decoy.app.extra.QR_PAYLOAD "decoy://hello?fixture=1"
```

Or open the app and tap **Inject hello fixture QR**.

3. Confirm Compose shows **Hello Decoy**, then **Complete session** (session wiped).

## Modules

| Module | Role |
|--------|------|
| `:app` | Compose shell + debug inject entry |
| `:core` | ContextResolver + retention |
| `:privacy-net` | OkHttp privacy defaults + ID bans |
| `:plugins-api` | Plugin contract |
| `:fixtures` | Hello fixture plugin + catalog |
