# Shade

Per-app grayscale for Android. Toggle colour correction on or off for each app — Shade switches the setting automatically when you switch apps.

Built for the Light Phone 3, but works on any Android 10+ device.

[![Claude](https://img.shields.io/badge/Claude-D97757?logo=claude&logoColor=fff)](#)

## How it works

Shade runs as an [Accessibility Service](https://developer.android.com/guide/topics/ui/accessibility/service), watching for app switches. When you open an app, Shade looks up your saved preference and applies (or removes) Android's system grayscale via `Settings.Secure`.

Writing to `Settings.Secure` requires `WRITE_SECURE_SETTINGS`, a permission not grantable through normal app install — it must be granted once via ADB.

## Setup

### 1. Build and install

```bash
./setup.sh
```

Requires:
- JDK at `/Applications/Android Studio.app/Contents/jbr/Contents/Home` (adjust `JAVA_HOME` in `setup.sh` if yours differs)
- Android SDK platform-tools at `~/Library/Android/sdk/platform-tools`
- Device connected with USB debugging enabled

The script downloads the Gradle wrapper on first run, builds the APK, installs it, and grants the permission.

### 2. Enable the Accessibility Service

On the device: **Settings → Accessibility → Shade → enable**

That's it. Open Shade, toggle the apps you want in grayscale.

## Permissions

| Permission | Why | How granted |
|---|---|---|
| `WRITE_SECURE_SETTINGS` | Write grayscale settings | ADB (done by `setup.sh`) |
| `QUERY_ALL_PACKAGES` | List installed apps | Normal install |
| Accessibility Service | Detect app switches | User enables in Settings |

## Tech

Kotlin · Jetpack Compose · DataStore · AccessibilityService

No third-party dependencies beyond AndroidX.

## License

MIT
