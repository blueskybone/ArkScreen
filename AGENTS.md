# APK packaging

- For APKs delivered to the user, run `python3 scripts/build-apk.py --offline` (defaults to the R8 probe variant).
- The script increments `versionCode` and the `2.4.0-dev.N` development suffix before each build. The old `2.4.0-dev1` spelling is normalized on the next increment.
- Use `--variant debug` or `--variant release` only when that variant is requested. Keep the existing application ID and signing configuration so builds remain eligible for in-place updates.
- Compilation and test-only commands do not need a version increment. A failed packaging attempt retains its increment.
