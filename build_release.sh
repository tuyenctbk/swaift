#!/usr/bin/env bash
set -e

export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$JAVA_HOME/bin:$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:/opt/homebrew/bin"

mkdir -p gradle/wrapper

if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    if [ -f "../DoseFlow/gradle/wrapper/gradle-wrapper.jar" ]; then
        echo "Copying gradle-wrapper.jar..."
        cp ../DoseFlow/gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
    elif command -v gradle &> /dev/null; then
        echo "Generating Gradle wrapper..."
        gradle wrapper
    fi
fi

chmod +x gradlew

echo "==============================================="
echo "📦 Building swaift Release APK & AAB"
echo "==============================================="

./gradlew assembleRelease bundleRelease

echo ""
echo "==============================================="
echo "🎉 RELEASE BUILD SUCCESSFUL!"
echo "📍 APK Output: app/build/outputs/apk/release/app-release.apk"
echo "📍 AAB Output: app/build/outputs/bundle/release/app-release.aab"
echo "==============================================="
