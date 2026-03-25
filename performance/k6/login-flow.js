/**
 * Login Flow Performance Test
 *
 * Tests the authentication endpoint under load with both valid and invalid
 * credentials to ensure the server handles both paths within acceptable latency.
 *
 * Run locally:
 *   k6 run performance/k6/login-flow.js
 */
import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

const BASE_URL    = __ENV.BASE_URL    || 'https://the-internet.herokuapp.com';
const RESULT_FILE = __ENV.RESULT_FILE || 'performance/results/login-results.json';

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
        { duration: '10s', target: 5 },
        { duration: '30s', target: 5 },
        { duration: '10s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.05'],
    checks: ['rate>0.95'],
  },
};

export default function () {
  const params = { redirects: 5 };

  group('Valid Login', () => {
    const res = http.post(
      `${BASE_URL}/authenticate`,
      { username: 'tomsmith', password: 'SuperSecretPassword!' },
      params,
    );
    check(res, {
      'status is 200': (r) => r.status === 200,
      'redirected to secure area': (r) => r.url.includes('/secure'),
      'shows success message': (r) => r.body.includes('secure area'),
      'response time under 2s': (r) => r.timings.duration < 2000,
    });
  });

  sleep(0.5);

  group('Invalid Login', () => {
    const res = http.post(
      `${BASE_URL}/authenticate`,
      { username: 'tomsmith', password: 'wrongpassword' },
      params,
    );
    check(res, {
      'status is 200': (r) => r.status === 200,
      'stays on login page': (r) => r.url.includes('/login'),
      'shows error message': (r) => r.body.includes('invalid'),
      'response time under 2s': (r) => r.timings.duration < 2000,
    });
  });

  sleep(1);
}

export function handleSummary(data) {
  return {
    [RESULT_FILE]: JSON.stringify(data),
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
  };
}
