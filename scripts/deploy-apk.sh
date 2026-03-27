#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
CONFIG_FILE="${1:-$ROOT_DIR/deploy.s3.properties}"
APK_PATH="${2:-$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk}"
APP_VERSION_CODE="${3:-0}"
APP_VERSION_NAME="${4:-unknown}"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "Deploy config not found: $CONFIG_FILE" >&2
  exit 1
fi

if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not found: $APK_PATH" >&2
  exit 1
fi

if [[ "${APK_PATH##*.}" != "apk" ]]; then
  echo "Only APK artifacts can be deployed: $APK_PATH" >&2
  exit 1
fi

get_prop() {
  local key="$1"
  grep -E "^${key}=" "$CONFIG_FILE" | head -n1 | cut -d'=' -f2-
}

ENDPOINT="$(get_prop endpoint)"
BUCKET="$(get_prop bucket)"
REGION="$(get_prop region)"
ACCESS_KEY="$(get_prop accessKey)"
SECRET_KEY="$(get_prop secretKey)"

if [[ -z "$ENDPOINT" || -z "$BUCKET" || -z "$REGION" || -z "$ACCESS_KEY" || -z "$SECRET_KEY" ]]; then
  echo "Deploy config is incomplete: $CONFIG_FILE" >&2
  exit 1
fi

VERSION_NAME="${VERSION_NAME:-$(date -u +%Y%m%d-%H%M%S)}"
OBJECT_KEY="android/debug/custle-${VERSION_NAME}.apk"
LATEST_KEY="android/debug/latest.apk"
LATEST_META_KEY="android/debug/latest.json"
PUBLIC_BASE_URL="${ENDPOINT%/}/${BUCKET}"
VERSIONED_URL="${PUBLIC_BASE_URL}/${OBJECT_KEY}"
LATEST_URL="${PUBLIC_BASE_URL}/${LATEST_KEY}"
TMP_META="$(mktemp)"

export AWS_ACCESS_KEY_ID="$ACCESS_KEY"
export AWS_SECRET_ACCESS_KEY="$SECRET_KEY"
export AWS_DEFAULT_REGION="$REGION"

cat > "$TMP_META" <<EOF
{
  "versionCode": ${APP_VERSION_CODE},
  "versionName": "${APP_VERSION_NAME}",
  "apkUrl": "${LATEST_URL}",
  "publishedAt": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

# Remove old versioned APKs (keep only latest.apk and latest.json)
aws --endpoint-url "$ENDPOINT" s3 rm "s3://${BUCKET}/android/debug/" --recursive --exclude "latest.apk" --exclude "latest.json" --only-show-errors 2>/dev/null || true

aws --endpoint-url "$ENDPOINT" s3 cp "$APK_PATH" "s3://${BUCKET}/${LATEST_KEY}" --only-show-errors --acl public-read
aws --endpoint-url "$ENDPOINT" s3 cp "$TMP_META" "s3://${BUCKET}/${LATEST_META_KEY}" --only-show-errors --acl public-read --content-type application/json

rm -f "$TMP_META"

printf 'Uploaded:\n'
printf '  s3://%s/%s\n' "$BUCKET" "$LATEST_KEY"
printf '  s3://%s/%s\n' "$BUCKET" "$LATEST_META_KEY"
