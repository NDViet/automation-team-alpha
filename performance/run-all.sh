#!/usr/bin/env bash
# Run all K6 performance tests, stream metrics to InfluxDB (Grafana), and
# publish summary results to the platform ingestion endpoint.
#
# Prerequisites:
#   - k6 installed (https://grafana.com/docs/k6/latest/set-up/install-k6/)
#   - Node.js >= 18
#   - InfluxDB + Grafana (optional, for real-time dashboards):
#       cd /path/to/test-automation-platform && docker compose up -d
#       Grafana: http://localhost:3000  (admin/admin)
#       InfluxDB: http://localhost:8086
#
# Usage:
#   ./performance/run-all.sh
#
# Environment overrides:
#   BASE_URL            AUT base URL      (default: https://the-internet.herokuapp.com)
#   K6                  k6 binary         (default: k6)
#   INFLUXDB_URL        InfluxDB base URL (default: http://localhost:8086)
#                       Points to the platform stack (docker compose up in
#                       test-automation-platform/) by default.
#                       Set to empty string to skip InfluxDB streaming.
#   K6_TEAM             Team tag stored in InfluxDB (default: automation-team-alpha)
#   K6_PROJECT          Project tag stored in InfluxDB (default: the-internet)
#   K6_ENV              Environment tag stored in InfluxDB (default: local)
#   PLATFORM_URL        Platform URL      (default: http://localhost:8081)
#   PLATFORM_API_KEY    API key           (default: local-dev)
#   TEST_ENV            Env label         (default: local)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="${SCRIPT_DIR}/results"
K6="${K6:-k6}"
BASE_URL="${BASE_URL:-https://the-internet.herokuapp.com}"
INFLUXDB_URL="${INFLUXDB_URL:-http://localhost:8086}"
K6_TEAM="${K6_TEAM:-automation-team-alpha}"
K6_PROJECT="${K6_PROJECT:-the-internet}"
K6_ENV="${K6_ENV:-local}"

mkdir -p "$RESULTS_DIR"

# ── InfluxDB availability check ───────────────────────────────────────────────
# Streaming is optional — tests still run and publish to the platform if
# InfluxDB is not available.

INFLUXDB_OUT=""
if [ -n "$INFLUXDB_URL" ]; then
  if curl -sf "${INFLUXDB_URL}/ping" > /dev/null 2>&1; then
    INFLUXDB_OUT="--out influxdb=${INFLUXDB_URL}/k6"
    echo "InfluxDB detected at ${INFLUXDB_URL} — real-time metrics will stream to Grafana (http://localhost:3001)"
  else
    echo "InfluxDB not reachable at ${INFLUXDB_URL} — skipping real-time streaming (summary-only mode)"
  fi
fi

# ── Helpers ───────────────────────────────────────────────────────────────────

run_k6() {
  local name="$1"
  local test_file="$2"
  local result_file="$3"
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

run_k6 "Homepage Load"       "${SCRIPT_DIR}/k6/homepage-load.js"     "${RESULTS_DIR}/homepage-results.json"
run_k6 "Login Flow"          "${SCRIPT_DIR}/k6/login-flow.js"        "${RESULTS_DIR}/login-results.json"
run_k6 "Form Interactions"   "${SCRIPT_DIR}/k6/form-interactions.js" "${RESULTS_DIR}/form-results.json"

# ── Publish results ───────────────────────────────────────────────────────────

echo ""
echo "=================================================="
echo " Publishing results to platform"
echo "=================================================="

node "${SCRIPT_DIR}/platform-publisher.mjs" \
  "${RESULTS_DIR}/homepage-results.json" \
  "${RESULTS_DIR}/login-results.json" \
  "${RESULTS_DIR}/form-results.json"
