import { LogBuffer } from '@/utils/log-buffer';
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
	callerInfoEnabled: boolean;
	showToast: boolean;
	hooks?: LoggerHooksOptionsType;
}

const defaultOptions: LoggerOptionsType = {
	consoleEnabled: !isProduction,
	callerInfoEnabled: false,
	showToast: false
};

class Logger {
	private options: LoggerOptionsType;

	private callerInfo: string;

	private logBuffer: LogBuffer;

	constructor(options: LoggerOptionsType = defaultOptions) {
		this.options = { ...defaultOptions, ...options };
		this.callerInfo = '';
		this.logBuffer = new LogBuffer();
		this.logBuffer.startService();
	}

	private getCallerInfo(): string {
		if (!this.options.callerInfoEnabled) {
			return '';
		}

		try {
			throw new Error();
		} catch (error: any) {
			return error.stack.split('\n')[3];
		}
	}

	log(
		level: string,
		message: string,
		messageOptions?: LoggerMessageOptionsType,
		...optionalParams: any[]
	): void {
		if (this.options.hooks?.before) {
			this.options.hooks.before(level, message.toString(), this.callerInfo);
		}

		if (this.options.consoleEnabled) {
			console[level](`[${level}] ${message} ${this.callerInfo}`, ...optionalParams);
		}

		if (this.options.showToast && messageOptions?.showToast) {
			this.displayToast(level, message.toString(), messageOptions);
		} else if (this.options.showToast && messageOptions?.showToast !== false) {
			this.displayToast(level, message.toString(), messageOptions);
		}
		if (this.options.hooks?.after && messageOptions?.silent !== true) {
			this.options.hooks.after(level, message.toString(), this.callerInfo);
		}

		this.queueLogs(level, message);
	}

	queueLogs(level: string, message: any) {
		this.logBuffer.add({ level, message });
	}

	info(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]): void {
		this.callerInfo = this.getCallerInfo();
		this.log(LogLevels.INFO, message.toString(), messageOptions, optionalParams);
	}

	warn(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]) {
		this.callerInfo = this.getCallerInfo();
		this.log(LogLevels.WARN, message.toString(), messageOptions, optionalParams);
	}

	success(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]) {
		this.callerInfo = this.getCallerInfo();
		this.log(LogLevels.SUCCESS, message.toString(), messageOptions, optionalParams);
	}

	error(message: any, messageOptions?: LoggerMessageOptionsType, ...optionalParams: any[]) {
		this.callerInfo = this.getCallerInfo();
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
	callerInfoEnabled: true,
	showToast: true
});
