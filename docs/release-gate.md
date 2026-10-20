# Release gate checklist

Sign off before tagging a public release. **F-Droid publishing is optional/deferred** — the gate can pass with a local F-Droid-ready artifact only.

## Privacy & FOSS

- [ ] `./gradlew test` green
- [ ] `./gradlew :app:assembleDebug` green
- [ ] `./gradlew :app:assembleRelease` green (unsigned OK)
- [ ] `powershell -File ./scripts/check-banned-apis.ps1` green
- [ ] No Play Services / Firebase / GAID / `ANDROID_ID` product usage
- [ ] IBAN/card never in vendor outbound (Phase 4 tests)
- [ ] One-shot wipe + Multi-day TTL covered by tests
- [ ] BLE/UWB only via injection in CI (no cloud geolocation)

## Docs

- [ ] [release-notes.md](release-notes.md) updated for the version
- [ ] [development-phases.md](development-phases.md) exit criteria reflect shipped phases
- [ ] Manifesto / [product-spec.md](product-spec.md) still accurate

## F-Droid (optional)

- [ ] [`metadata/nl.decoy.app.yml`](../metadata/nl.decoy.app.yml) commit SHA filled
- [ ] Local unsigned release APK built
- [ ] **Publish / fdroiddata PR** — deferred unless explicitly chosen

## Sign-off

| Role | Name | Date | Notes |
|------|------|------|-------|
| Maintainer | | | Phase 5+6 prep; publish deferred |
