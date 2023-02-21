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
			severity,
			summary,
			detail,
			life
		});
	};

	const warn = (summary: string | undefined, detail: string, life: number = DEFAULT_DURATION) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.warn,
			summary: summary || ToastSummaries.WARNING,
			group: ToastSeverity.warn,
			detail,
			life
		});
	};

	const error = (summary: string | undefined, detail: string, life: number = DEFAULT_DURATION) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.error,
			summary: summary || ToastSummaries.ERROR,
			group: ToastSeverity.error,
			detail,
			life
		});
	};

	const success = (
		summary: string | undefined,
		detail: string,
		life: number = DEFAULT_DURATION
	) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.success,
			summary: summary || ToastSummaries.INFO,
			group: ToastSeverity.success,
			detail,
			life
		});
	};

	const info = (summary: string | undefined, detail: string, life: number = DEFAULT_DURATION) => {
		ToastEventBus.emit('add', {
			severity: ToastSeverity.info,
			summary: summary || ToastSummaries.INFO,
			group: ToastSeverity.info,
			detail,
			life
		});
	};

	return { showToast, info, warn, success, error };
};
