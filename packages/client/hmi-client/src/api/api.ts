import axios, { AxiosRequestHeaders } from 'axios';
import { setupCache, buildWebStorage } from 'axios-cache-interceptor';
import { logger } from '@/utils/logger';
import { ToastSummaries } from '@/services/toast';
import useAuthStore from '../stores/auth';

const API = axios.create({
	baseURL: '/api'
});

// Setup Caching for API calls, leveraging localStorage
// By default the caching is 5 minutes
setupCache(API, {
	storage: buildWebStorage(localStorage, 'api-cache:')
});

// Hook in bearer tokens
// See
// - https://medium.com/swlh/handling-access-and-refresh-tokens-using-axios-interceptors-3970b601a5da
API.interceptors.request.use(
	(config) => {
		const auth = useAuthStore();

		if (config.headers) {
			config.headers.Authorization = `Bearer ${auth.token}`;
		} else {
			config.headers = { Authorization: `Bearer ${auth.token}` } as AxiosRequestHeaders;
		}
		return config;
	},
	(error) => {
		logger.error(error, { showToast: true, toastTitle: `Authentication Error` });
	}
);

API.interceptors.response.use(
	(response) => response,
	(error) => {
		const msg = error.response.data;
		const status = error.response.status;
		switch (status) {
			case 500:
				logger.error(msg, {
					showToast: true,
					toastTitle: `${ToastSummaries.SERVICE_UNAVAILABLE} (${status})`
				});
				break;
			default:
				logger.error(msg, {
					showToast: true,
					toastTitle: `${ToastSummaries.NETWORK_ERROR} (${status})`
				});
		}
		return null; // return null
	}
);

export default API;
