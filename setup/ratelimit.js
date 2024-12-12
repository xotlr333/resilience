import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { getGatewayHost, createPointRequest } from './pointRequest.js';

export let options = {
    scenarios: {
        ratelimit_test: {
            executor: 'constant-arrival-rate',
            rate: 100,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 200,
            maxVUs: 1000
        }
    }
};

export default function() {
    const payload = JSON.stringify(createPointRequest());

    const params = {
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'aeg-event-type': 'CircuitBreakerOpened'
        },
    };
    let gatewayHost = getGatewayHost();
    let response = http.post(`http://${gatewayHost}/api/events/point`, payload, params);

    check(response, {
        'status is 200 or 429': (r) => [200, 429].includes(r.status),
        'rate limit check': (r) => r.status === 429 ? true : r.status === 200
    });
}

