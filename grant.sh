#!/bin/bash
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"

echo "==> Lifting sideload restriction..."
adb shell cmd appops set pro.shade ACCESS_RESTRICTED_SETTINGS allow

echo "==> Granting WRITE_SECURE_SETTINGS..."
adb shell pm grant pro.shade android.permission.WRITE_SECURE_SETTINGS

echo "==> Enabling accessibility service..."
COMPONENT="pro.shade/pro.shade.AppWatcherService"
CURRENT=$(adb shell settings get secure enabled_accessibility_services | tr -d '\r\n')

if [ "$CURRENT" = "null" ] || [ -z "$CURRENT" ]; then
    NEW="$COMPONENT"
elif [[ "$CURRENT" == *"$COMPONENT"* ]]; then
    NEW="$CURRENT"
else
    NEW="${CURRENT}:${COMPONENT}"
fi

adb shell settings put secure enabled_accessibility_services "$NEW"
adb shell settings put secure accessibility_enabled 1

echo "Done."
