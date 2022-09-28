import { useAuthStore } from '../stores/auth';

// eslint-disable-next-line import/prefer-default-export
export const uncloak = async (url: string) => {
	const auth = useAuthStore();
	const response = await fetch(url, {
		headers: {
			Authorization: `Bearer ${auth.token}`
		}
	});

	if (!response.ok) {
		console.log('Unauthorized Access');
		return;
	}

	const data = await response.json();
	console.log(`STATUS: ${response.status} Response: ${JSON.stringify(data)}`);
};
