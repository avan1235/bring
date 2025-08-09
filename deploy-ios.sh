#!/bin/bash

set -eu

if [ $# -lt 1 ]; then
  echo "Usage: $0 <device-id>"
  exit 1
fi

DEVICE_ID="$1"

# === BUILD ===
echo "🔨 Building app with xcodebuild..."

CMD='xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "Release iosApp" \
  -configuration "Release" \
  -destination "id=$DEVICE_ID" \
  clean build'

BUILD_OUTPUT=$(eval "$CMD" 2>&1 | tee /dev/tty)
BUILD_EXIT_CODE=${PIPESTATUS[0]}

if [ $BUILD_EXIT_CODE -ne 0 ]; then
  echo "Build failed with exit code $BUILD_EXIT_CODE"
  exit $BUILD_EXIT_CODE
fi

# === PARSE .app PATH ===
BUILT_PRODUCTS_DIR=$(echo "$BUILD_OUTPUT" | grep -m 1 "BUILT_PRODUCTS_DIR" | sed 's/^[^=]*=//')
FULL_PRODUCT_NAME=$(echo "$BUILD_OUTPUT" | grep -m 1 "FULL_PRODUCT_NAME" | sed 's/^[^=]*=//')

if [ -z "$BUILT_PRODUCTS_DIR" ]; then
  echo "❌ Could not find BUILT_PRODUCTS_DIR path in build output."
  exit 1
fi

if [ ! -d "$BUILT_PRODUCTS_DIR" ]; then
  echo "❌ Directory $BUILT_PRODUCTS_DIR does not exist"
  exit 1
fi

APP_PATH="$BUILT_PRODUCTS_DIR/Bring!.app"

if [ ! -d "$APP_PATH" ]; then
  echo "❌ Directory $APP_PATH does not exist"
  exit 1
fi

echo "📦 Found .app bundle at: $APP_PATH"

# === DEPLOY ===
echo "📲 Deploying to device with ios-deploy..."
ios-deploy --bundle "$APP_PATH" --id "$DEVICE_ID"

echo "✅ Deployment complete."
