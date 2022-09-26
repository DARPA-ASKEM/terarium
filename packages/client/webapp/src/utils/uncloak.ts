let accessToken: string | null;

// export const getAccessToken = () => {
// 	const client = new XMLHttpRequest();
// 	client.open('GET', '/silent-check-sso.html', true);
// 	client.send();

// 	client.onreadystatechange = () => {
// 		if (client.readyState === client.HEADERS_RECEIVED) {
// 			accessToken = client.getResponseHeader('OIDC_access_token');
// 		}
// 	};
// };
export const getAccessToken = async () => {
	const response = await fetch('/silent-check-sso.html');

	if (response.ok) {
		accessToken = response.headers.get('OIDC_access_token');
	}
};

// export const callAPI = (url: string, id: string) => {
// 	console.log('... fetching ...');

// 	const client = new XMLHttpRequest();
// 	client.open('GET', url, true);
// 	client.setRequestHeader('Authorization', `Bearer ${accessToken}`);
// 	client.send();

// 	client.onreadystatechange = () => {
// 		if (client.readyState === client.DONE) {
// 			console.log(`STATUS:${client.status} Response: ${client.responseText}`);
// 		}
// 	};
// };

export const uncloak = async (url: string) => {
	console.log('... uncloaking ...');

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
