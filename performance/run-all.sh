#!/usr/bin/env bash
# Run all K6 performance tests.
#
# Each test publishes its results directly to the platform via the
# @ndviet/adapter-k6 inside handleSummary — no separate publish step needed.
#
# Prerequisites:
#   - k6 >= 0.38.0 (https://grafana.com/docs/k6/latest/set-up/install-k6/)
#   - Node.js >= 18 + NODE_AUTH_TOKEN (PAT with read:packages) for first build:
#       export NODE_AUTH_TOKEN=<github-pat>  # only needed for npm install
#       # run-all.sh calls npm install + npm run build automatically if dist/ is absent
#   - InfluxDB + Grafana (optional, for real-time dashboards):
#       cd /path/to/test-automation-platform && docker compose up -d
#       Grafana: http://localhost:3000  (admin / admin)
#       InfluxDB: http://localhost:8086
#
# Usage:
#   ./performance/run-all.sh
#
# Environment overrides:
#   BASE_URL              AUT base URL        (default: https://the-internet.herokuapp.com)
#   K6                    k6 binary           (default: k6)
#   INFLUXDB_URL          InfluxDB base URL   (default: http://localhost:8086)
#                         Set to empty string to skip real-time streaming.
#   K6_TEAM               InfluxDB team tag   (default: automation-team-alpha)
#   K6_PROJECT            InfluxDB project tag (default: the-internet)
#   K6_ENV                InfluxDB env tag    (default: local)
#   PLATFORM_URL          Platform base URL   (default: http://localhost:8081)
#   PLATFORM_API_KEY      Platform API key    (default: local-dev)
#   PLATFORM_TEAM_ID      Platform team slug  (default: automation-team-alpha)
#   PLATFORM_PROJECT_ID   Platform project slug (default: the-internet)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="${SCRIPT_DIR}/results"
K6="${K6:-k6}"
BASE_URL="${BASE_URL:-https://the-internet.herokuapp.com}"
INFLUXDB_URL="${INFLUXDB_URL:-http://localhost:8086}"
K6_TEAM="${K6_TEAM:-automation-team-alpha}"
K6_PROJECT="${K6_PROJECT:-the-internet}"
K6_ENV="${K6_ENV:-local}"
PLATFORM_URL="${PLATFORM_URL:-http://localhost:8081}"
PLATFORM_API_KEY="${PLATFORM_API_KEY:-local-dev}"
PLATFORM_TEAM_ID="${PLATFORM_TEAM_ID:-automation-team-alpha}"
PLATFORM_PROJECT_ID="${PLATFORM_PROJECT_ID:-the-internet}"

mkdir -p "$RESULTS_DIR"

# ── Build bundled scripts if dist/ is missing ─────────────────────────────────
# esbuild inlines @ndviet/adapter-k6 from node_modules into each dist/ file so
# k6 does not need npm resolution at runtime.
# NODE_AUTH_TOKEN (PAT with read:packages) is required for npm install.

DIST_DIR="${SCRIPT_DIR}/dist"
if [ ! -f "${DIST_DIR}/homepage-load.js" ]; then
  echo "Building K6 scripts (npm install + esbuild)…"
  ( cd "$SCRIPT_DIR" && npm install --silent && npm run build --silent )
fi

# ── InfluxDB availability check ───────────────────────────────────────────────
# Streaming is optional — tests still run and publish to the platform if
# InfluxDB is not available.

INFLUXDB_OUT=""
if [ -n "$INFLUXDB_URL" ]; then
  if curl -sf "${INFLUXDB_URL}/ping" > /dev/null 2>&1; then
    INFLUXDB_OUT="--out influxdb=${INFLUXDB_URL}/k6"
    echo "InfluxDB detected at ${INFLUXDB_URL} — real-time metrics will stream to Grafana (http://localhost:3000)"
  else
    echo "InfluxDB not reachable at ${INFLUXDB_URL} — skipping real-time streaming (summary-only mode)"
  fi
fi

# ── Helpers ───────────────────────────────────────────────────────────────────

run_k6() {
  local name="$1"
  local test_file="$2"
  local result_file="$3"
  local suite_name="$4"
  echo ""
  echo "▶  $name"
  # shellword-split INFLUXDB_OUT intentionally; || true keeps going on threshold failures
  # shellcheck disable=SC2086
  "$K6" run \
    $INFLUXDB_OUT \
    --env BASE_URL="$BASE_URL" \
    --env RESULT_FILE="$result_file" \
    --env K6_TEAM="$K6_TEAM" \
    --env K6_PROJECT="$K6_PROJECT" \
    --env K6_ENV="$K6_ENV" \
    --env PLATFORM_URL="$PLATFORM_URL" \
    --env PLATFORM_API_KEY="$PLATFORM_API_KEY" \
    --env PLATFORM_TEAM_ID="$PLATFORM_TEAM_ID" \
    --env PLATFORM_PROJECT_ID="$PLATFORM_PROJECT_ID" \
    --env PLATFORM_ENVIRONMENT="$K6_ENV" \
    --env PLATFORM_SUITE_NAME="$suite_name" \
    "$test_file" || true
}

# ── Run tests ─────────────────────────────────────────────────────────────────

echo "=================================================="
echo " K6 Performance Tests  —  $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
echo "  AUT         : $BASE_URL"
echo "  Team        : $K6_TEAM"
echo "  Project     : $K6_PROJECT"
echo "  Environment : $K6_ENV"
echo "=================================================="

run_k6 "Homepage Load"      "${DIST_DIR}/homepage-load.js"     "${RESULTS_DIR}/homepage-results.json" "Homepage Load"
run_k6 "Login Flow"         "${DIST_DIR}/login-flow.js"        "${RESULTS_DIR}/login-results.json"    "Login Flow"
run_k6 "Form Interactions"  "${DIST_DIR}/form-interactions.js" "${RESULTS_DIR}/form-results.json"     "Form Interactions"

echo ""
echo "=================================================="
echo " Done"
echo "=================================================="
