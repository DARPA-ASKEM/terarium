// import API from '@/api/api';
import { useToastService } from '@/services/toast';
import axios from 'axios';
import { isEmpty } from 'lodash';

const LOGS = axios.create({
	baseURL: '/api'
});

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

	isEmpty = (): boolean => isEmpty(this.logs.length);

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
				toast.error('Error Sending Logs', error);
			}
		}
	};
}
