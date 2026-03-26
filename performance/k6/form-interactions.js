/**
 * Form Interactions Performance Test
 *
 * Covers the dropdown, dynamic controls, and dynamic loading pages.
 * Validates that UI-heavy pages still serve their initial HTML promptly
 * even under concurrent request load.
 *
 * Run locally:
 *   k6 run performance/k6/form-interactions.js
 */
import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import { publishToPlatform } from '@ndviet/adapter-k6';

const BASE_URL    = __ENV.BASE_URL    || 'https://the-internet.herokuapp.com';
const RESULT_FILE = __ENV.RESULT_FILE || '';

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
        { duration: '10s', target: 8 },
        { duration: '30s', target: 8 },
        { duration: '10s', target: 0 },
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
  group('Dropdown', () => {
    const res = http.get(`${BASE_URL}/dropdown`);
    check(res, {
      'status is 200': (r) => r.status === 200,
      'has Option 1': (r) => r.body.includes('Option 1'),
      'has Option 2': (r) => r.body.includes('Option 2'),
      'response time under 3s': (r) => r.timings.duration < 3000,
    });
  });

  sleep(0.5);

  group('Dynamic Controls', () => {
    const res = http.get(`${BASE_URL}/dynamic_controls`);
    check(res, {
      'status is 200': (r) => r.status === 200,
      'has checkbox': (r) => r.body.includes('checkbox'),
      'has input field': (r) => r.body.includes('input'),
      'response time under 3s': (r) => r.timings.duration < 3000,
    });
  });

  sleep(0.5);

  group('Dynamic Loading - Example 1', () => {
    const res = http.get(`${BASE_URL}/dynamic_loading/1`);
    check(res, {
      'status is 200': (r) => r.status === 200,
      'has start button': (r) => r.body.includes('Start'),
      'response time under 3s': (r) => r.timings.duration < 3000,
    });
  });

  sleep(0.5);

  group('Dynamic Loading - Example 2', () => {
    const res = http.get(`${BASE_URL}/dynamic_loading/2`);
    check(res, {
      'status is 200': (r) => r.status === 200,
      'has start button': (r) => r.body.includes('Start'),
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
