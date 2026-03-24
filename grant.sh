#!/bin/bash
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"
adb shell pm grant pro.shade android.permission.WRITE_SECURE_SETTINGS
echo "Done."
