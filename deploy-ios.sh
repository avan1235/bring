#!/bin/bash

set -eu

if [ $# -lt 1 ]; then
  echo "Usage: $0 <device-id-1> [device-id-2] [device-id-3] ..."
  echo "Example: $0 ABC123DEF456 GHI789JKL012"
  exit 1
fi

# Store all device IDs in an array
DEVICE_IDS=("$@")

echo "🚀 Starting deployment to ${#DEVICE_IDS[@]} device(s)..."
echo "Device IDs: ${DEVICE_IDS[*]}"
echo ""

# Function to deploy to a single device
deploy_to_device() {
  local DEVICE_ID="$1"
  local DEVICE_NUM="$2"
  local TOTAL_DEVICES="$3"

  echo "========================================"
  echo "📱 Processing Device $DEVICE_NUM/$TOTAL_DEVICES: $DEVICE_ID"
  echo "========================================"

  # === BUILD ===
  echo "🔨 Building app with xcodebuild for device $DEVICE_ID..."

  CMD='xcodebuild \
    -project iosApp/iosApp.xcodeproj \
    -scheme "Release iosApp" \
    -configuration "Release" \
    -destination "id='$DEVICE_ID'" \
    clean build'

  BUILD_OUTPUT=$(eval "$CMD" 2>&1 | tee /dev/tty)
  BUILD_EXIT_CODE=${PIPESTATUS[0]}

  if [ $BUILD_EXIT_CODE -ne 0 ]; then
    echo "❌ Build failed for device $DEVICE_ID with exit code $BUILD_EXIT_CODE"
    return $BUILD_EXIT_CODE
  fi

  # === PARSE .app PATH ===
  BUILT_PRODUCTS_DIR=$(echo "$BUILD_OUTPUT" | grep -m 1 "BUILT_PRODUCTS_DIR" | sed 's/^[^=]*=//')
  FULL_PRODUCT_NAME=$(echo "$BUILD_OUTPUT" | grep -m 1 "FULL_PRODUCT_NAME" | sed 's/^[^=]*=//')

  if [ -z "$BUILT_PRODUCTS_DIR" ]; then
    echo "❌ Could not find BUILT_PRODUCTS_DIR path in build output for device $DEVICE_ID"
    return 1
  fi

  if [ ! -d "$BUILT_PRODUCTS_DIR" ]; then
    echo "❌ Directory $BUILT_PRODUCTS_DIR does not exist for device $DEVICE_ID"
    return 1
  fi

  APP_PATH="$BUILT_PRODUCTS_DIR/Bring!.app"

  if [ ! -d "$APP_PATH" ]; then
    echo "❌ Directory $APP_PATH does not exist for device $DEVICE_ID"
    return 1
  fi

  echo "📦 Found .app bundle at: $APP_PATH"

  # === DEPLOY ===
  echo "📲 Deploying to device $DEVICE_ID with ios-deploy..."
  if ios-deploy --bundle "$APP_PATH" --id "$DEVICE_ID"; then
    echo "✅ Deployment complete for device $DEVICE_ID"
    return 0
  else
    echo "❌ Deployment failed for device $DEVICE_ID"
    return 1
  fi
}

# Deploy to each device
SUCCESS_COUNT=0
FAILED_DEVICES=()

for i in "${!DEVICE_IDS[@]}"; do
  DEVICE_ID="${DEVICE_IDS[i]}"
  DEVICE_NUM=$((i + 1))

  if deploy_to_device "$DEVICE_ID" "$DEVICE_NUM" "${#DEVICE_IDS[@]}"; then
    ((SUCCESS_COUNT++))
  else
    FAILED_DEVICES+=("$DEVICE_ID")
  fi

  echo ""
done

# Summary
echo "========================================"
echo "📊 DEPLOYMENT SUMMARY"
echo "========================================"
echo "Total devices: ${#DEVICE_IDS[@]}"
echo "Successful deployments: $SUCCESS_COUNT"
echo "Failed deployments: $((${#DEVICE_IDS[@]} - SUCCESS_COUNT))"

if [ ${#FAILED_DEVICES[@]} -gt 0 ]; then
  echo "❌ Failed devices: ${FAILED_DEVICES[*]}"
  exit 1
else
  echo "🎉 All deployments completed successfully!"
  exit 0
fi