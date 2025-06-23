#!/bin/bash

# CMake Setup Script for Android Development
# This script helps set up CMake for the Android project with native libraries

set -e

echo "🔧 Setting up CMake for Android development..."

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "❌ ANDROID_HOME is not set. Please set it to your Android SDK location."
    echo "   Example: export ANDROID_HOME=/Users/\$USER/Library/Android/sdk"
    exit 1
fi

echo "✅ Android SDK found at: $ANDROID_HOME"

# Install CMake through Android SDK Manager
echo "📦 Installing CMake 3.22.1 via Android SDK Manager..."
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "cmake;3.22.1"

# Install NDK if not present
echo "📦 Installing Android NDK 21.1.6352462..."
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "ndk;21.1.6352462"

# Set environment variables
echo "🔧 Setting up environment variables..."
export CMAKE_HOME="$ANDROID_HOME/cmake/3.22.1"
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/21.1.6352462"
export PATH="$CMAKE_HOME/bin:$PATH"

# Add to shell profile for persistence
SHELL_PROFILE=""
if [ -f "$HOME/.zshrc" ]; then
    SHELL_PROFILE="$HOME/.zshrc"
elif [ -f "$HOME/.bashrc" ]; then
    SHELL_PROFILE="$HOME/.bashrc"
elif [ -f "$HOME/.bash_profile" ]; then
    SHELL_PROFILE="$HOME/.bash_profile"
fi

if [ -n "$SHELL_PROFILE" ]; then
    echo "📝 Adding environment variables to $SHELL_PROFILE..."
    
    # Remove existing entries if any
    grep -v "CMAKE_HOME.*cmake" "$SHELL_PROFILE" > temp_profile || true
    grep -v "ANDROID_NDK_HOME.*ndk" temp_profile > temp_profile2 || true
    mv temp_profile2 "$SHELL_PROFILE" || true
    rm -f temp_profile temp_profile2 || true
    
    # Add new entries
    echo "" >> "$SHELL_PROFILE"
    echo "# Android Development - CMake and NDK" >> "$SHELL_PROFILE"
    echo "export CMAKE_HOME=\"\$ANDROID_HOME/cmake/3.22.1\"" >> "$SHELL_PROFILE"
    echo "export ANDROID_NDK_HOME=\"\$ANDROID_HOME/ndk/21.1.6352462\"" >> "$SHELL_PROFILE"
    echo "export PATH=\"\$CMAKE_HOME/bin:\$PATH\"" >> "$SHELL_PROFILE"
fi

# Verify installation
echo "🔍 Verifying CMake installation..."
if [ -f "$CMAKE_HOME/bin/cmake" ]; then
    echo "✅ CMake installed successfully at: $CMAKE_HOME/bin/cmake"
    "$CMAKE_HOME/bin/cmake" --version
else
    echo "❌ CMake installation failed or not found"
    exit 1
fi

echo "🔍 Verifying NDK installation..."
if [ -d "$ANDROID_NDK_HOME" ]; then
    echo "✅ NDK installed successfully at: $ANDROID_NDK_HOME"
    ls -la "$ANDROID_NDK_HOME"
else
    echo "❌ NDK installation failed or not found"
    exit 1
fi

echo ""
echo "🎉 Setup completed successfully!"
echo ""
echo "📋 Summary:"
echo "   - CMake 3.22.1 installed"
echo "   - Android NDK 21.1.6352462 installed"
echo "   - Environment variables configured"
echo ""
echo "🔄 Please restart your terminal or run: source $SHELL_PROFILE"
echo "🚀 You can now build the project with: ./gradlew assembleDebug"
