# Branching & pull requests

`main` is protected. Do not push commits directly to `main`.

## Rules (GitHub)

- Pull request required before merge
- Force-push to `main` blocked
- Branch deletion of `main` blocked
- Conversation resolution required before merge
- Zero required approvals (solo-friendly); still always use a PR

## Workflow

1. Update local `main` (never commit on it):

```bash
git checkout main
git pull origin main
```

2. Create a feature branch:

```bash
git checkout -b feature/short-description
```

Naming: `feature/…`, `fix/…`, `docs/…`, `chore/…`.

3. Commit on the feature branch, then push:

```bash
git push -u origin HEAD
```

4. Open a PR into `main`:

```bash
gh pr create --base main --title "…" --body "…"
```

5. Merge via GitHub (squash preferred for small focused PRs), then delete the remote branch.

## Current work

Phase 0 foundation work lives on `feature/phase-0-foundation` until merged via PR.

## Related

- [Dev setup](dev-setup.md)
- [Development phases](development-phases.md)
