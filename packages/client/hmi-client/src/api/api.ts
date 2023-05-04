import axios from 'axios';
import { logger } from '@/utils/logger';
import { ToastSummaries } from '@/services/toast';

const API = axios.create({
	baseURL: '/api'
});

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

export enum PollerState {
	Done = 'done',
	Failed = 'failed',
	ExceedThreshold = 'ExceedThreshold',
	Cancelled = 'Cancelled'
}

interface PollResponse<T> {
	error: any;
	progress?: any;
	data: T | null;
}

type PollerCallback<T> = (...args: any[]) => Promise<PollResponse<T>>;
type ProgressCallback = (progressData: any, current: number, max: number) => void;

interface PollerResult<T> {
	state: PollerState;
	data: T | null;
}

export class Poller<T> {
	pollingInterval = 2000;

	pollingThreshold = 10;

	keepGoing = false;

	poll: PollerCallback<T> | null;

	progressAction: ProgressCallback | null;

	numPolls = 0;

	constructor() {
		this.poll = null;
		this.progressAction = null;
	}

	setInterval(v: number) {
		this.pollingInterval = v;
		return this;
	}

	setThreshold(v: number) {
		this.pollingThreshold = v;
		return this;
	}

	setPollAction(f: PollerCallback<T>) {
		this.poll = f;
		return this;
	}

	setProgressAction(f: ProgressCallback) {
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

		let response: PollResponse<T> | null = null;

		if (!this.poll) throw new Error('Poll callback undefined');

		while (this.numPolls < this.pollingThreshold) {
			this.numPolls++;
			if (!this.keepGoing) {
				return {
					state: PollerState.Cancelled,
					data: null
				};
			}

			try {
				// eslint-disable-next-line no-await-in-loop
				response = (await this.poll()) as PollResponse<T>;
				const { error, progress, data } = response;

				if (error) {
					return {
						state: PollerState.Failed,
						data: null
					};
				}

				if (data) {
					return {
						state: PollerState.Done,
						data
					};
				}

				// We are still in progress
				if (this.progressAction) {
					this.progressAction(progress, this.numPolls, this.pollingThreshold);
				}
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
