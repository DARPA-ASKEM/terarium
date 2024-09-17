import axios, { AxiosHeaders } from 'axios';
import { logger } from '@/utils/logger';
import { ToastSummaries } from '@/services/toast';
import useAuthStore from '../stores/auth';

const CONFIGURATION = axios.create({
	baseURL: '/configuration',
	headers: new AxiosHeaders()
});

CONFIGURATION.interceptors.response.use(
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
			auth.login(window.location.href);
		}
		return null;
	}
);

export default CONFIGURATION;
