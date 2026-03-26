/**
 * Homepage Load Test
 *
 * Verifies that the main landing page and key navigation pages are responsive
 * under moderate concurrent load.
 *
 * Run locally:
 *   k6 run performance/k6/homepage-load.js
 *
 * Override target URL:
 *   k6 run --env BASE_URL=https://staging.example.com performance/k6/homepage-load.js
 */
import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import { publishToPlatform } from '@ndviet/adapter-k6';

const BASE_URL    = __ENV.BASE_URL    || 'https://the-internet.herokuapp.com';
const RESULT_FILE = __ENV.RESULT_FILE || '';

// Tags are stored as InfluxDB tag keys — used by the platform Grafana dashboard
// to filter metrics by team / project / environment.
// Override via K6_TEAM, K6_PROJECT, K6_ENV env vars (or set in run-all.sh).
export const options = {
  tags: {
    team:        __ENV.K6_TEAM    || 'automation-team-alpha',
    project:     __ENV.K6_PROJECT || 'the-internet',
    environment: __ENV.K6_ENV     || 'local',
  },
  scenarios: {
    load: {
      executor: 'ramping-vus',
      stages: [
        { duration: '15s', target: 10 }, // ramp up
        { duration: '30s', target: 10 }, // hold
        { duration: '15s', target: 0 },  // ramp down
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.05'],
    checks: ['rate>0.95'],
  },
};

export default function () {
  group('Homepage', () => {
    const res = http.get(`${BASE_URL}/`);
    check(res, {
      'status is 200': (r) => r.status === 200,
      'contains welcome text': (r) => r.body.includes('Welcome to the-internet'),
      'response time under 3s': (r) => r.timings.duration < 3000,
    });
  });

  sleep(0.5);

  group('Login Page', () => {
    const res = http.get(`${BASE_URL}/login`);
    check(res, {
      'status is 200': (r) => r.status === 200,
      'has username field': (r) => r.body.includes('Username'),
      'has password field': (r) => r.body.includes('Password'),
      'response time under 3s': (r) => r.timings.duration < 3000,
    });
  });

  sleep(0.5);

  group('Checkboxes Page', () => {
    const res = http.get(`${BASE_URL}/checkboxes`);
    check(res, {
      'status is 200': (r) => r.status === 200,
      'has checkboxes': (r) => r.body.includes('checkbox'),
      'response time under 3s': (r) => r.timings.duration < 3000,
    });
  });

  sleep(1);
}

export function handleSummary(data) {
  publishToPlatform(data);
  const out = { stdout: textSummary(data, { indent: ' ', enableColors: true }) };
  if (RESULT_FILE) out[RESULT_FILE] = JSON.stringify(data);
  return out;
}
