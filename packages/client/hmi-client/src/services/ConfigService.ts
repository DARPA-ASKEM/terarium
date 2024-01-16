import type { ClientConfig } from '@/types/Types';
import axios, { AxiosHeaders } from 'axios';
import useAuthStore from '@/stores/auth';

let configuration: ClientConfig | undefined;
async function getConfiguration() {
	if (!configuration) {
		const response = await axios.get('/api/config', {
			headers: new AxiosHeaders().setAuthorization(`Bearer ${useAuthStore().token}`)
		});
		configuration = response.data;
	}
	return configuration;
}

export default getConfiguration;
