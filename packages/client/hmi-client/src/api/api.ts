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
	 */
	onmessage: (message: any) => void;
	/**
	 * Handler for SSE connection open event.
	 * @param {Response} response The response object.
	 */
	onopen?: (response: Response) => void;
	/**
	 * Handler for SSE connection error.
	 * @param {Error} error The error object.
	 */
	onerror?: (error: Error) => void;
	/**
	 * Handler for SSE connection close event.
	 */
	onclose?: () => void;
}

/**
 * Enum for SSE connection status values.
 * @readonly
 * @enum {string}
 */
export enum SSEStatus {
	INITIALIZED = 'initialized',
	DONE = 'done',
	ERROR = 'error'
}

/**
 * Class representing a Server-Sent Events (SSE) handler.
 */
export class SSEHandler {
	private url: string;

	private handlers: SSEHandlers;

	private options?: FetchEventSourceInit;

	private controller: AbortController;

	private status: SSEStatus;

	/**
	 * Create a SSEHandler.
	 * @param {string} url - The URL to connect to.
	 * @param {SSEHandlers} handlers - The handlers for the SSE events.
	 * @param {FetchEventSourceInit} options - The options for the fetchEventSource function.
	 */
	constructor(url: string, handlers: SSEHandlers, options?: FetchEventSourceInit) {
		this.url = url;
		this.handlers = handlers;
		this.options = options;
		this.controller = new AbortController();
		this.status = SSEStatus.INITIALIZED;
	}

	/**
	 * Close the SSE connection.
	 */
	public closeConnection() {
		this.controller.abort();
		if (this.handlers.onclose) this.handlers.onclose();
		logger.info('Connection closed', { showToast: false });
	}

	/**
	 * Get the status of the SSE connection.
	 * @return {SSEStatus} The status of the SSE connection.
	 */
	getStatus() {
		return this.status;
	}

	/**
	 * Set the status of the SSE connection.
	 * @param {SSEStatus} status - The new status of the SSE connection.
	 */
	setStatus(status: SSEStatus) {
		this.status = status;
	}

	// FIXME: We can probably add the message to the response
	/**
	 * Connect to the SSE server.
	 * @return {Promise<SSEStatus>} The status of the SSE connection after attempting to connect.
	 */
	async connect(): Promise<SSEStatus> {
		const handlers = this.handlers;
		const authStore = useAuthStore();
		const fetchEventSourceOptions: FetchEventSourceInit = {
			...this.options,
			headers: {
				...this.options?.headers,
				Authorization: `Bearer ${authStore.token}`
			},
			onmessage: (message: EventSourceMessage) => {
				const data = message?.data;
				this.handlers.onmessage(data);
			},
			onerror: (error: Error) => {
				if (this.handlers.onerror) this.handlers.onerror(error);
				throw error;
			},
			async onopen(response: Response) {
				logger.info('Connection opened', { showToast: false });
				if (handlers.onopen) handlers.onopen(response);
			},
			signal: this.controller.signal,
			openWhenHidden: true
		};

		try {
			await fetchEventSource(this.url, fetchEventSourceOptions);
		} catch (error: unknown) {
			logger.error(error, { showToast: false });
			this.setStatus(SSEStatus.ERROR);
		} finally {
			this.closeConnection();
		}
		return this.getStatus();
	}
}

export default API;
