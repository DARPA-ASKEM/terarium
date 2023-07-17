// import API from '@/api/api';
import { useToastService } from '@/services/toast';
import axios from 'axios';
import { isEmpty } from 'lodash';
import useAuthStore from '../stores/auth';

const isProduction = process.env.NODE_ENV === 'production';

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
		console.error(error);
	}
);

const toast = useToastService();

// LogDetailsType Interface
interface LogDetailsType {
	level: string;
	message: string;
}

export class LogBuffer {
	logs: LogDetailsType[];

	constructor() {
		this.logs = [];
	}

	clearLogs = () => {
		this.logs = [];
	};

	add = (log: LogDetailsType) => {
		this.logs.push(log);
	};

	isEmpty = (): boolean => isEmpty(this.logs);

	getLogBuffer = (): LogDetailsType[] => this.logs;

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
				console.error(error);
				if (!isProduction) {
					toast.error('Error Sending Logs', error);
				}
			}
		}
	};
}
