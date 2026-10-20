# Non-goals

Decoy deliberately does **not** aim to be:

| Non-goal | Why |
|----------|-----|
| A VPN or Tor client | Network anonymizers are user choice; Decoy is a physical-world interface layer |
| A general-purpose ad blocker | We avoid vendor WebViews and strip trackers on our fetch path; we are not a browser extension |
| A vendor SaaS / hosted Decoy cloud | Local-first; no permanent cloud accounts for core product |
| Play Services / Firebase dependent | F-Droid and FOSS purity |
| A fake-ID toolkit | No fabricating plates, government IDs, or banking credentials to evade law or enforcement |
| Silent scrapers of other apps | Ticket/code import is user-initiated share / open-with only |
| Permanent cloud sync for wallets | Present-wallet items stay on-device by default |
| A replacement for every regulated identity flow | When the law requires a real identifier, Decoy asks consent and sends the real value |

If a feature needs one of the above to “work,” redesign the feature—or put it out of scope.
