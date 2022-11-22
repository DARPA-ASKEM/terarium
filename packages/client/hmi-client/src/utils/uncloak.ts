import useAuthStore from '../stores/auth';

export const uncloak = async (url: string, method = 'GET', body = '') => {
	const auth = useAuthStore();
	const options = {
		method,
		body: method !== 'GET' ? body : null, // GET method cannot have a body
		headers: {
			Authorization: `Bearer ${auth.token}`
		}
	};

	const response = await fetch(url, options);

	if (!response.ok) {
		return null;
	}

	const data = await response.json();
	return data;
};
