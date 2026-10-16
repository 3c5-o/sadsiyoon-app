#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$DIR/android/gradlew" ]; then
    chmod +x "$DIR/android/gradlew"
    exec "$DIR/android/gradlew" -p "$DIR/android" "$@"
else
    echo "Error: android/gradlew not found" >&2
    exit 1
fi
