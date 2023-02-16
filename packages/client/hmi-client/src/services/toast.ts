import ToastEventBus from 'primevue/toasteventbus';

const DEFAULT_DURATION = 3000; // 3 seconds

// TODO: Define more.
export enum ToastSummaries {
	NETWORK_ERROR = 'Network Error',
	UNKNOWN_ERROR = 'Unknown Error',
	SERVICE_UNAVAILABLE = 'Service Unavailable',
	ATTENTION = 'Attention',
	INFO = 'Info',
	WARNING = 'Warning',
	ERROR = 'Error'
}

export enum ToastSeverity {
	info = 'info',
	warn = 'warn',
	success = 'success',
	error = 'error'
}

export const useToastService = () => {
	const showToast = (
		severity: ToastSeverity,
		summary: string,
		detail: string,
		life: number = 4000
	) => {
		ToastEventBus.emit('add', {
			severity: severity,
			summary: summary,
			detail: detail,
			life: life
		});
	};

	const warn = (
		summary: string | undefined,
		detail: string,
		duration: number = DEFAULT_DURATION
	) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.warn,
			summary: summary || ToastSummaries.WARNING,
			detail: detail,
			life: duration,
			group: ToastSeverity.warn
		});
	};

	const error = (
		summary: string | undefined,
		detail: string,
		duration: number = DEFAULT_DURATION
	) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.error,
			summary: summary || ToastSummaries.ERROR,
			detail: detail,
			life: duration,
			group: ToastSeverity.error
		});
	};

	const success = (
		summary: string | undefined,
		detail: string,
		duration: number = DEFAULT_DURATION
	) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.success,
			summary: summary || ToastSummaries.INFO,
			detail: detail,
			life: duration,
			group: ToastSeverity.success
		});
	};

	const info = (
		summary: string | undefined,
		detail: string,
		duration: number = DEFAULT_DURATION
	) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.info,
			summary: summary || ToastSummaries.INFO,
			detail: detail,
			life: duration,
			group: ToastSeverity.info
		});
	};

	return { showToast, info, warn, success, error };
};
