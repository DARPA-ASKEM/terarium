import { ToastSummaries } from '@/services/toast';
import { logger } from '@/utils/logger';
import axios, { AxiosHeaders } from 'axios';
import {
	EventSourceMessage,
	FetchEventSourceInit,
	fetchEventSource
} from '@microsoft/fetch-event-source';
import useAuthStore from '../stores/auth';

const API = axios.create({
	baseURL: '/api',
	headers: new AxiosHeaders()
});

// Hook in bearer tokens
// See
// - https://medium.com/swlh/handling-access-and-refresh-tokens-using-axios-interceptors-3970b601a5da
API.interceptors.request.use(
	(config) => {
		const auth = useAuthStore();
		config.headers.setAuthorization(`Bearer ${auth.token}`);
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
				logger.error(msg.toString(), {
					showToast: false,
					toastTitle: `${ToastSummaries.SERVICE_UNAVAILABLE} (${status})`
				});
				break;
			default:
				logger.error(msg.toString(), {
					showToast: false,
					toastTitle: `${ToastSummaries.NETWORK_ERROR} (${status})`
				});
		}
		if (status === 401) {
			// redirect to login
			const auth = useAuthStore();
			auth.keycloak?.login({
				redirectUri: window.location.href
			});
		}
		return null;
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

export interface PollResponse<T> {
	error: any;
	progress?: any;
	data: T | null;
}

type PollerCallback<T> = (...args: any[]) => Promise<PollResponse<T>>;
type ProgressCallback = (progressData: any, current: number, max: number) => void;

export interface PollerResult<T> {
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

/**
 * Interface defining handlers for Server-Sent Events (SSE).
 */
export interface SSEHandlers {
	/**
	 * Handler for incoming SSE messages.
	 * @param {any} message The received message.
	 * @param {() => void} abort Function to abort the SSE connection.
	 */
	onMessage: (message: any, abort: () => void) => void;
	/**
	 * Handler for SSE connection open event.
	 * @param {Response} response The response object.
	 */
	onOpen?: (response: Response) => void;
	/**
	 * Handler for SSE connection error.
	 * @param {Error} error The error object.
	 */
	onError: (error: Error) => void;
	/**
	 * Handler for SSE connection close event.
	 */
	onClose?: () => void;
}

/**
 * Function to handle Server-Sent Events (SSE) connections.
 * @param {string} url The URL to connect to.
 * @param {SSEHandlers} handlers Object containing SSE event handlers.
 * @param {FetchEventSourceInit} options Additional options for the SSE connection.
 */
export async function handleSSE(
	url: string,
	handlers: SSEHandlers,
	options?: FetchEventSourceInit
) {
	const authStore = useAuthStore();
	const controller = new AbortController();
	const fetchEventSourceOptions: FetchEventSourceInit = {
		...options,
		headers: {
			...options?.headers,
			Authorization: `Bearer ${authStore.token}`
		},
		onmessage(message: EventSourceMessage) {
			const data = message?.data;
			const abort = () => controller.abort();
			handlers.onMessage(data, abort);
		},
		onerror(error: Error) {
			handlers.onError(error);
			controller.abort();
		},
		async onopen(response: Response) {
			logger.info('Connection opened', { showToast: false });
			if (handlers.onOpen) handlers.onOpen(response);
		},
		onclose() {
			if (handlers.onClose) handlers.onClose();
		},
		signal: controller.signal,
		openWhenHidden: true
	};

	try {
		await fetchEventSource(url, fetchEventSourceOptions);
	} catch (error: unknown) {
		if (!(error instanceof Error)) {
			throw error;
		}
		handlers.onError(error);
		controller.abort();
	} finally {
		if (handlers.onClose) handlers.onClose();
	}
}

export default API;
