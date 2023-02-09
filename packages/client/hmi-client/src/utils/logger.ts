// HMI-Client logging service
import { createLogger, LogEvent, LoggerHook, LogLevel } from 'vue-logger-plugin';
import API from '@/api/api';

// TODO: add logic for different modes
const isProduction = process.env.NODE_ENV === 'production';

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
			try {
				const resp = await API.post(`/logs/`, {
					logs: this.getLogBuffer()
				});
				const { status } = resp;
				if (status !== 200) {
					console.warn('POST to /logs did not return a 200');
				} else {
					this.clearLogs();
				}
			} catch (error) {
				console.error(error);
			}
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
const logger = createLogger({
	enabled: true,
	consoleEnabled: isProduction,
	callerInfo: true,
	level: 'debug',
	afterHooks: [bufferLog]
});

export default logger;
