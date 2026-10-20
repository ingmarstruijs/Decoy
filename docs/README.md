# Decoy documentation

Specs and architecture for Decoy. The manifesto lives in the root [README](../README.md).

## Reading order

1. [Product spec](product-spec.md) — what we build and why
2. [Non-goals](non-goals.md) — what we deliberately do not build
3. [Development phases](development-phases.md) — build order, live API ladder, milestones
4. [Use cases](use-cases.md) — concrete scenarios (One-shot, Multi-day)
5. [Data lifecycle](data-lifecycle.md) — One-shot vs Multi-day rules
6. [Threat model](threat-model.md) — privacy guarantees and residuals
7. [Plugins architecture](plugins-architecture.md) — core vs capability plugins
8. [End-to-end testability](e2e-testability.md) — emulator/CI vs manual scan
9. [Payments](payments.md) — Fake vs staging relay; Phase 4b live rail
10. [Dev setup](dev-setup.md) — build, install, adb inject
11. [Manual testing](manual-test-lab.md) — product home, Scan, Demo fixtures
12. [Public QR samples](public-qr-samples.md) — print/scan/paste corpus
13. [F-Droid posture](fdroid.md) — metadata + reproducible build (publish deferred)
14. [Release gate](release-gate.md) — pre-tag checklist
15. [Release notes](release-notes.md)
16. [Branching](branching.md) — protected `main`, feature branches, PRs

## Conventions

- Language: English
- Retention terms: **One-shot** and **Multi-day** (not “Do & Forget / Keep for Days”)
- Specs first — implementation details live in code; phases track ship order
