import axios, { AxiosResponse } from 'axios';
import { logger } from '@/utils/logger';
import { ToastSummaries } from '@/services/toast';
import useAuthStore from '../stores/auth';

const API = axios.create({
	baseURL: '/api'
});

// Hook in bearer tokens
// See
// - https://medium.com/swlh/handling-access-and-refresh-tokens-using-axios-interceptors-3970b601a5da
API.interceptors.request.use(
	(config) => {
		const auth = useAuthStore();

		if (config.headers) {
			config.headers.Authorization = `Bearer ${auth.token}`;
		} else {
			config.headers = { Authorization: `Bearer ${auth.token}` };
		}
		return config;
	},
	(error) => {
		logger.error(error, { showToast: false, toastTitle: `Authentication Error` });
	}
);

API.interceptors.response.use(
	(response) => response,
	(error) => {
		const msg = error.response.data;
		const status = error.response.status;
		switch (status) {
			case 500:
				logger.error(msg, {
					showToast: false,
					toastTitle: `${ToastSummaries.SERVICE_UNAVAILABLE} (${status})`
				});
				break;
			default:
				logger.error(msg, {
					showToast: false,
					toastTitle: `${ToastSummaries.NETWORK_ERROR} (${status})`
				});
		}
		return null; // return null
	}
);

// eslint-disable-next-line no-promise-executor-return
const timeout = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));
const NOOP = () => {};

export enum PollerState {
	Done = 'done',
	Failed = 'failed',
	ExceedThreshold = 'ExceedThreshold'
}

interface PollerResult<T> {
	state: PollerState;
	data: T | null;
}
export class Poller<T> {
	pollingId = 0;

	pollingInterval = 2000;

	pollingThreshold = 10;

	keepGoing = false;

	poll: Function = NOOP;

	successCheck: Function = NOOP;

	progressAction: Function = NOOP;

	numPolls = 0;

	setInterval(v: number) {
		this.pollingInterval = v;
		return this;
	}

	setThreshold(v: number) {
		this.pollingThreshold = v;
		return this;
	}

	setSuccessCheck(f: Function) {
		this.successCheck = f;
		return this;
	}

	setProgressAction(f: Function) {
		this.progressAction = f;
		return this;
	}

	// Start polling, there are 4 foreseeable exit conditions
	// 1. Done, we have the result
	// 2. Failed, request returned with 4xx or 5xx status
	// 3. Failed, any unexpected errors
	// 4. ExceedThreshold, took longer than allotted time
	async start(): Promise<PollerResult<T>> {
		this.keepGoing = true;
		this.numPolls = 0;

		let response: AxiosResponse<T, any>;

		while (this.numPolls < this.pollingThreshold) {
			this.numPolls++;
			if (!this.keepGoing) {
				break;
			}

			try {
				// eslint-disable-next-line no-await-in-loop
				response = (await this.poll()) as AxiosResponse<T, any>;
				const { status, data } = response;
				console.log(status, data);

				if (status >= 400) {
					return {
						state: PollerState.Failed,
						data: null
					};
				}

				if (this.successCheck(data)) {
					return {
						state: PollerState.Done,
						data
					};
				}

				// We are still in progress
				this.progressAction(data, this.numPolls, this.pollingThreshold);
			} catch (error) {
				return {
					state: PollerState.Failed,
					data: null
				};
			}

			// eslint-disable-next-line no-await-in-loop
			await timeout(this.pollingInterval);
		}

		return {
			state: PollerState.ExceedThreshold,
			data: null
		};
	}

	// Not really a fluent API, but convienent
	stop() {
		this.keepGoing = false;
	}
}

export default API;
