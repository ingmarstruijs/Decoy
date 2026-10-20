# Non-goals

Decoy deliberately does **not** aim to be:

| Non-goal | Why |
|----------|-----|
| A VPN or Tor client | Network anonymizers are user choice; Decoy is a physical-world interface layer |
| A general-purpose ad blocker | We avoid vendor WebViews and strip trackers on our fetch path; we are not a browser extension |
| A vendor SaaS / hosted Decoy cloud | Local-first; no permanent cloud accounts for core product |
| Play Services / Firebase dependent | F-Droid and FOSS purity |
| A fake-ID toolkit | No fabricating plates, government IDs, or banking credentials to evade law or enforcement |
| A generic ticket / loyalty import-and-show wallet | Already well covered by existing apps; Decoy focuses on venue UI overwrite and PII field policy |
| A scraper of other apps | No reading other apps’ sandboxes or embedding their SDKs |
| A replacement for every regulated identity flow | When the law requires a real identifier, Decoy asks consent and sends the real value |

If a feature needs one of the above to “work,” redesign the feature—or put it out of scope.
