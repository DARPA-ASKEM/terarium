import { useToastService } from '@/services/toast';

// TODO: add logic for different modes
const isProduction = process.env.NODE_ENV === 'production';
const toast = useToastService();

enum LogLevels {
	INFO = 'info',
	ERROR = 'error',
	WARN = 'warn',
	SUCCESS = 'success'
}

interface LoggerMessageOptionsType {
	showToast?: boolean;
	toastTitle?: string;
	silent?: boolean; // do not transmit to backend
}

interface LoggerHooksOptionsType {
	before?: (level: string, message: string, callerInfo?: string) => void;
	after?: (level: string, message: string, callerInfo?: string) => void;
}

interface LoggerOptionsType {
	consoleEnabled: boolean;
	showToast: boolean;
	hooks?: LoggerHooksOptionsType;
}

const defaultOptions: LoggerOptionsType = {
	consoleEnabled: !isProduction,
	showToast: false
};

class Logger {
	private options: LoggerOptionsType;

	constructor(options: LoggerOptionsType = defaultOptions) {
		this.options = { ...defaultOptions, ...options };
	}

	log(level: string, message: string, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]): void {
		if (this.options.hooks?.before) {
			this.options.hooks.before(level, message.toString());
		}

		if (this.options.consoleEnabled) {
			const consoleLevel = level === LogLevels.SUCCESS ? LogLevels.INFO : level;
			console[consoleLevel](`[${level}] ${message}`, ...optionalParams);
		}

		if (this.options.showToast && messageOptions?.showToast) {
			this.displayToast(level, message.toString(), messageOptions);
		} else if (this.options.showToast && messageOptions?.showToast !== false) {
			this.displayToast(level, message.toString(), messageOptions);
		}
		if (this.options.hooks?.after && messageOptions?.silent !== true) {
			this.options.hooks.after(level, message.toString());
		}
	}

	info(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]): void {
		this.log(LogLevels.INFO, message.toString(), messageOptions, optionalParams);
	}

	warn(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]) {
		this.log(LogLevels.WARN, message.toString(), messageOptions, optionalParams);
	}

	success(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]) {
		this.log(LogLevels.SUCCESS, message.toString(), messageOptions, optionalParams);
	}

	error(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]) {
		this.log(LogLevels.ERROR, message.toString(), messageOptions, optionalParams);
	}

	private displayToast(level: string, message: string, messageOptions?: LoggerMessageOptionsType) {
		switch (level) {
			case LogLevels.INFO:
				toast.info(messageOptions?.toastTitle, message);
				break;
			case LogLevels.ERROR:
				toast.error(messageOptions?.toastTitle, message);
				break;
			case LogLevels.WARN:
				toast.warn(messageOptions?.toastTitle, message);
				break;
			case LogLevels.SUCCESS:
				toast.success(messageOptions?.toastTitle, message);
				break;
			default:
				toast.info(messageOptions?.toastTitle, message);
		}
	}
}

export const logger = new Logger({
	consoleEnabled: !isProduction,
	showToast: true
});
