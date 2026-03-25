#!/usr/bin/env node
/**
 * Platform Publisher — K6 native summary JSON
 *
 * Reads one or more K6 summary JSON files produced by handleSummary()
 * and POSTs each to the platform ingestion endpoint as format=K6.
 *
 * The platform routes K6 format to K6JsonParser which reads:
 *   root_group.checks[]  and  root_group.groups[].checks[]
 * — exactly what JSON.stringify(data) in handleSummary() produces.
 *
 * Usage:
 *   node performance/platform-publisher.mjs <file1.json> [file2.json ...]
 *
 * Configuration (env vars — same keys as platform.properties):
 *   PLATFORM_URL          Base URL of the platform  (default: http://localhost:8081)
 *   PLATFORM_API_KEY      API key                   (default: local-dev)
 *   PLATFORM_TEAM_ID      Team identifier           (default: automation-team-alpha)
 *   PLATFORM_PROJECT_ID   Project identifier        (default: the-internet)
 *   TEST_ENV              Environment label         (default: local)
 */
import { readFileSync } from 'node:fs';
import { basename, extname } from 'node:path';

// Use localhost default only when the env var is completely absent (local dev).
// An explicitly empty string means "platform not configured" — skip publishing.
const PLATFORM_URL  = 'PLATFORM_URL'       in process.env ? process.env.PLATFORM_URL       : 'http://localhost:8081';
const API_KEY       = 'PLATFORM_API_KEY'   in process.env ? process.env.PLATFORM_API_KEY   : 'local-dev';
const TEAM_ID       = process.env.PLATFORM_TEAM_ID     || 'automation-team-alpha';
const PROJECT_ID    = process.env.PLATFORM_PROJECT_ID  || 'the-internet';
const ENVIRONMENT   = process.env.TEST_ENV             || 'local';

// ── Git / CI context ─────────────────────────────────────────────────────────
// Enables branch filtering, commit correlation, and CI run links in the
// platform's performance metrics history dashboard.
const BRANCH = process.env.PLATFORM_BRANCH
  || process.env.GITHUB_REF_NAME        // GitHub Actions
  || process.env.CI_COMMIT_BRANCH       // GitLab CI
  || process.env.GIT_BRANCH             // Jenkins
  || process.env.CIRCLE_BRANCH          // CircleCI
  || '';

const COMMIT_SHA = process.env.PLATFORM_COMMIT_SHA
  || process.env.GITHUB_SHA             // GitHub Actions
  || process.env.CI_COMMIT_SHA          // GitLab CI
  || process.env.GIT_COMMIT             // Jenkins
  || process.env.CIRCLE_SHA1            // CircleCI
  || '';

const CI_RUN_URL = (() => {
  // GitHub Actions
  if (process.env.GITHUB_SERVER_URL && process.env.GITHUB_REPOSITORY && process.env.GITHUB_RUN_ID) {
    return `${process.env.GITHUB_SERVER_URL}/${process.env.GITHUB_REPOSITORY}/actions/runs/${process.env.GITHUB_RUN_ID}`;
  }
  if (process.env.CI_JOB_URL)      return process.env.CI_JOB_URL;       // GitLab CI
  if (process.env.CIRCLE_BUILD_URL) return process.env.CIRCLE_BUILD_URL; // CircleCI
  if (process.env.BUILD_URL)        return process.env.BUILD_URL;        // Jenkins
  return '';
})();

const INGEST_URL = `${PLATFORM_URL}/api/v1/results/ingest`;
const TIMEOUT_MS = 30_000;

/**
 * Derives a human-readable suite name from the result filename.
 * e.g. "homepage-results.json" → "Homepage Load"
 *      "login-results.json"    → "Login Flow"
 *      "form-results.json"     → "Form Interactions"
 */
const SUITE_NAME_MAP = {
  'homepage-results': 'Homepage Load',
  'login-results':    'Login Flow',
  'form-results':     'Form Interactions',
};

function suiteName(file) {
  const stem = basename(file, extname(file));
  return SUITE_NAME_MAP[stem]
    ?? stem.replace(/-results$/, '').split('-').map(w => w[0].toUpperCase() + w.slice(1)).join(' ');
}

// ─────────────────────────────────────────────────────────────────────────────

if (!PLATFORM_URL) {
  console.log('[platform] PLATFORM_URL is empty — skipping ingestion (set PLATFORM_URL to enable).');
  process.exit(0);
}

const files = process.argv.slice(2);
if (files.length === 0) {
  console.error('Usage: node platform-publisher.mjs <result.json> [result2.json ...]');
  process.exit(1);
}

let anyFailed = false;

for (const file of files) {
  const label = basename(file);
  const suite = suiteName(file);
  try {
    const content = readFileSync(file);

    const form = new FormData();
    form.append('teamId',      TEAM_ID);
    form.append('projectId',   PROJECT_ID);
    form.append('format',      'K6');
    form.append('environment', ENVIRONMENT);
    form.append('suiteName',   suite);
    if (BRANCH)     form.append('branch',    BRANCH);
    if (COMMIT_SHA) form.append('commitSha', COMMIT_SHA);
    if (CI_RUN_URL) form.append('ciRunUrl',  CI_RUN_URL);
    form.append('files', new Blob([content], { type: 'application/json' }), label);

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), TIMEOUT_MS);

    const res = await fetch(INGEST_URL, {
      method: 'POST',
      headers: { 'X-API-Key': API_KEY },
      body: form,
      signal: controller.signal,
    }).finally(() => clearTimeout(timeout));

    if (res.status === 202) {
      const body = await res.json();
      console.log(`[platform] ✓ ${suite} (${label}) → runId=${body.runId}, accepted=${body.accepted}`);
    } else {
      const text = await res.text().catch(() => '');
      console.warn(`[platform] ✗ ${suite} (${label}) → HTTP ${res.status}: ${text}`);
      anyFailed = true;
    }
  } catch (err) {
    console.warn(`[platform] ✗ ${suite} (${label}) → ${err.message}`);
    anyFailed = true;
  }
}

process.exit(anyFailed ? 1 : 0);
