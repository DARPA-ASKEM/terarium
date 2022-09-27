let accessToken: string | null;

export const getAccessToken = async () => {
	const response = await fetch('/silent-check-sso.html');

	if (response.ok) {
		accessToken = response.headers.get('OIDC_access_token');
	}
};

export const uncloak = async (url: string) => {
	const response = await fetch(url, {
		headers: {
			Authorization: `Bearer ${accessToken}`
		}
	});

	if (response.ok) {
		const data = await response.json();
		console.log(`STATUS: ${response.status} Response: ${JSON.stringify(data)}`);
	}
};

export const isAuthorized = () => accessToken != null;
