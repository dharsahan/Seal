#!/bin/bash
set -e

# Define variables
ANDROID_SDK_ROOT="$HOME/android-sdk"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
PLATFORM_VERSION="android-35"
BUILD_TOOLS_VERSION="34.0.0"

# Install necessary system packages
echo "Installing necessary system packages..."
sudo apt-get update
sudo apt-get install -y wget unzip openjdk-21-jdk

# Setup Android SDK
if [ ! -d "$ANDROID_SDK_ROOT" ]; then
    echo "Setting up Android SDK..."
    mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
    wget -q "$CMDLINE_TOOLS_URL" -O cmdline-tools.zip
    unzip -q cmdline-tools.zip -d "$ANDROID_SDK_ROOT/cmdline-tools"
    mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    rm cmdline-tools.zip
else
    echo "Android SDK directory already exists."
fi

export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

# Accept licenses and install SDK packages
echo "Accepting licenses and installing SDK packages..."
yes | sdkmanager --licenses
sdkmanager "platforms;$PLATFORM_VERSION" "build-tools;$BUILD_TOOLS_VERSION"

# Build APK
echo "Building APK..."
./gradlew assembleDebug

echo "Build complete. APKs are located in app/build/outputs/apk/"
