// HMI-Client logging service
import API from '@/api/api';
import { createLogger, LogEvent, LoggerHook, LogLevel } from 'vue-logger-plugin';
// TODO: add logic for different modes
// const env = process.env.NODE_ENV;

// LogDetails Interface
interface LogDetails {
	level: LogLevel;
	message: string;
}

// Usage:
// import logger from '@/utils/logger';
class LogBuffer {
	logs: LogDetails[];

	constructor() {
		this.logs = [];
	}

	clearLogs = () => {
		this.logs = [];
	};

	add = (log: LogDetails) => {
		this.logs.push(log);
	};

	isEmpty = (): boolean => {
		if (this.logs.length > 0) {
			return false;
		}
		return true;
	};

	getLogBuffer = (): LogDetails[] => this.logs;

	startService = () => {
		setInterval(() => {
			this.sendLogsToServer();
		}, 5000);
	};

	sendLogsToServer = async () => {
		if (!this.isEmpty()) {
			await API.post(`/logs/`, {
				logs: this.getLogBuffer()
			})
				.then(() => {
					this.clearLogs();
				})
				.catch((error) => {
					console.error(error);
				});
		}
	};
}

export const logBuffer = new LogBuffer();

// after hook
const bufferLog: LoggerHook = {
	async run(event: LogEvent) {
		const thisLog = { level: event.level, message: event.argumentArray[0] };
		logBuffer.add(thisLog);
	}
};

// create logger with options
export const logger = createLogger({
	enabled: true,
	level: 'debug',
	afterHooks: [bufferLog]
});
