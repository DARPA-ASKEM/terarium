// import API from '@/api/api';
import { useToastService } from '@/services/toast';
import axios from 'axios';
import useAuthStore from '../stores/auth';

const LOGS = axios.create({
	baseURL: '/api'
});

// Hook in bearer tokens
// See
// - https://medium.com/swlh/handling-access-and-refresh-tokens-using-axios-interceptors-3970b601a5da
LOGS.interceptors.request.use(
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
		console.warn(error);
	}
);

const toast = useToastService();

// LogDetails Interface
interface LogDetails {
	level: string;
	message: string;
}

export class LogBuffer {
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
				const resp = await LOGS.post(`/logs/`, {
					logs: this.getLogBuffer()
				});
				const { status } = resp;
				if (status === 200) {
					this.clearLogs();
				}
			} catch (error: any) {
				console.log(error);
				toast.error('Error Sending Logs', error);
			}
		}
	};
}
