# F-Droid posture (publish deferred)

Decoy targets F-Droid distribution. **Phase 6 prepares** metadata and reproducible build steps; **actual submission/publishing is deferred** until a tagged release the maintainers choose to push.

## In-repo metadata

Draft recipe: [`metadata/nl.decoy.app.yml`](../metadata/nl.decoy.app.yml).

Before any submission:

1. Set `Builds[].commit` to the release tag SHA
2. Confirm `License` matches the repo `LICENSE`
3. Build unsigned release locally and verify no proprietary deps

## Reproducible local release build

```bash
./gradlew :app:assembleRelease
```

Artifact (unsigned): `app/build/outputs/apk/release/app-release-unsigned.apk`

CI keeps `assembleDebug` + unit tests + banned-API / dependency audit as the merge gate. Release assemble is part of the [release gate](release-gate.md), not required on every PR.

## Dependency rules

- No Google Play Services / Firebase / advertising ID clients
- Audit script: `scripts/check-banned-apis.ps1` (also run in CI)
- See [non-goals.md](non-goals.md)

## Not done in this phase

- Upload to f-droid.org / inclusion in fdroiddata
- Signed Play Store listing (explicit non-goal as primary distribution)
