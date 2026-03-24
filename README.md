# Shade

Per-app grayscale for Android. Toggle colour correction on or off for each app — Shade switches the setting automatically when you switch apps.

Built for the Light Phone 3, but works on any Android 10+ device.

[![Claude](https://img.shields.io/badge/Claude-D97757?logo=claude&logoColor=fff)](#)

## How it works

Shade runs as an [Accessibility Service](https://developer.android.com/guide/topics/ui/accessibility/service), watching for app switches. When you open an app, Shade looks up your saved preference and applies (or removes) Android's system grayscale via `Settings.Secure`.

Writing to `Settings.Secure` requires `WRITE_SECURE_SETTINGS`, a permission not grantable through normal app install — it must be granted once via ADB.

## Setup

### 1. Install the APK

Download `app-release.apk` from the [latest release](https://github.com/joshkarlin/shade/releases/latest) and install it on your device.

### 2. Run the grant script

With the device connected via USB and USB debugging enabled:

```bash
./grant.sh
```

This does three things:
- Lifts Android 13+'s sideload restriction so the accessibility service can be enabled
- Grants `WRITE_SECURE_SETTINGS` via ADB
- Enables the Shade accessibility service

### 3. Done

Open Shade and configure per-app grayscale.

## Using Shade

The app list is split into two tabs — **MY APPS** (apps you installed) and **BUILT-IN** (pre-installed and system apps). Tap any row to toggle grayscale for that app.

App names appear in soft rainbow when in colour mode, white when shaded. Tap the **SHADE** title to toggle grayscale for Shade itself — the whole UI switches mode instantly.

**FULL SHADE** / **NO SHADE** applies to all apps in the current tab at once.

Shade itself is excluded from the grayscale filter when you're using it, so the UI always remains readable.

## Building from source

```bash
./setup.sh
```

Requires the Android SDK at `~/Library/Android/sdk` and the JDK at `/Applications/Android Studio.app/Contents/jbr/Contents/Home`. Adjust `JAVA_HOME` and `ANDROID_HOME` in `setup.sh` if yours differ.

## Permissions

| Permission | Why | How granted |
|---|---|---|
| `WRITE_SECURE_SETTINGS` | Write grayscale settings | ADB via `grant.sh` |
| `QUERY_ALL_PACKAGES` | List installed apps | Normal install |
| Accessibility Service | Detect app switches | ADB via `grant.sh` |

## Tech

Kotlin · Jetpack Compose · DataStore · AccessibilityService

No third-party dependencies beyond AndroidX.

## License

MIT
