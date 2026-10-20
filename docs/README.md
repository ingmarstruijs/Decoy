# Decoy documentation

Specs and architecture for Decoy. The manifesto lives in the root [README](../README.md).

## Reading order

1. [Product spec](product-spec.md) — what we build and why
2. [Non-goals](non-goals.md) — what we deliberately do not build
3. [Development phases](development-phases.md) — build order, milestones, exit criteria
4. [Use cases](use-cases.md) — concrete scenarios (One-shot, Multi-day)
5. [Data lifecycle](data-lifecycle.md) — One-shot vs Multi-day rules
6. [Threat model](threat-model.md) — privacy guarantees and residuals
7. [Plugins architecture](plugins-architecture.md) — core vs capability plugins
8. [End-to-end testability](e2e-testability.md) — how we prove it without vendor surveillance backends
9. [Payments](payments.md) — Fake vs staging open-banking relay; IBAN-out-of-band invariant
10. [Dev setup](dev-setup.md) — build, tests, emulator smoke
11. [Manual test lab](manual-test-lab.md) — how to use the debug scenario screen
12. [F-Droid posture](fdroid.md) — metadata + reproducible build (publish deferred)
13. [Release gate](release-gate.md) — pre-tag checklist
14. [Release notes](release-notes.md)
15. [Branching](branching.md) — protected `main`, feature branches, PRs

## Conventions

- Language: English
- Retention terms: **One-shot** and **Multi-day** (not “Do & Forget / Keep for Days”)
- Specs first — no implementation scaffolding in these docs
