import { createApp } from 'vue';
import router from './router';
import './style.css';
import App from './App.vue';
import { getAccessToken, isAuthorized } from './utils/uncloak';

// const initOptions: KeycloakConfig = {
// 	url: import.meta.env.VITE_KEYCLOAK_URL as string,
// 	realm: import.meta.env.VITE_KEYCLOAK_REALM as string,
// 	clientId: import.meta.env.VITE_KEYCLOAK_CLIENTID as string
// };

// console.info(initOptions);
// console.info(window.location.origin);

// const keycloak = new Keycloak(initOptions);

// keycloak
// 	.init({
// 		onLoad: 'check-sso',
// 		silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
// 		checkLoginIframe: true,
// 		enableLogging: true
// 	})
// 	.then(async (auth) => {
// 		if (!auth) {
// 			// window.location.reload();
// 			console.error('FAILED TO AUTHENTICAT');
// 		} else {
// 			console.info('Authenticated');

// 			await keycloak.loadUserInfo();
// 			console.info(keycloak.authenticated);

// 			// const app = createApp(App);
// 			// app.provide<KeycloakInstance>('keycloack', keycloak);
// 			// app.use(router);
// 			// app.mount('#app');
// 		}

// 		// Token Refresh
// 		// setInterval(() => {
// 		// 	keycloak
// 		// 		.updateToken(70)
// 		// 		.then((refreshed) => {
// 		// 			if (refreshed) {
// 		// 				console.info(`Token refreshed ${refreshed}`);
// 		// 			} else {
// 		// 				console.warn(
// 		// 					`Token not refreshed, valid for ${Math.round(
// 		// 						keycloak.tokenParsed!.exp! + keycloak.timeSkew! - new Date().getTime() / 1000
// 		// 					)} seconds`
// 		// 				);
// 		// 			}
// 		// 		})
// 		// 		.catch(() => {
// 		// 			console.error('Failed to refresh token');
// 		// 		});
// 		// }, 6000);
// 	})
// 	.catch((e) => {
// 		console.error('Authenticated Failed');
// 		console.error(e);
// 	});

await getAccessToken();
if (isAuthorized()) {
	const app = createApp(App);
	app.use(router);
	app.mount('#app');
} else {
	console.error('FAILED TO AUTHORIZE');
}

// function loadProfile() {
// 	keycloak
// 		.loadUserProfile()
// 		.success((profile) => {
// 			output(profile);
// 		})
// 		.error(() => {
// 			output('Failed to load profile');
// 		});
// }

// function updateProfile() {
// 	const url = keycloak.createAccountUrl().split('?')[0];
// 	const req = new XMLHttpRequest();
// 	req.open('POST', url, true);
// 	req.setRequestHeader('Accept', 'application/json');
// 	req.setRequestHeader('Content-Type', 'application/json');
// 	req.setRequestHeader('Authorization', `bearer ${keycloak.token}`);

// 	req.onreadystatechange = function () {
// 		if (req.readyState == 4) {
// 			if (req.status == 200) {
// 				output('Success');
// 			} else {
// 				output('Failed');
// 			}
// 		}
// 	};

// 	req.send('{"email":"myemail@foo.bar","firstName":"test","lastName":"bar"}');
// }

// function loadUserInfo() {
// 	keycloak
// 		.loadUserInfo()
// 		.success((userInfo) => {
// 			output(userInfo);
// 		})
// 		.error(() => {
// 			output('Failed to load user info');
// 		});
// }

// function refreshToken(minValidity) {
// 	keycloak
// 		.updateToken(minValidity)
// 		.then((refreshed) => {
// 			if (refreshed) {
// 				output(keycloak.tokenParsed);
// 			} else {
// 				output(
// 					`Token not refreshed, valid for ${Math.round(
// 						keycloak.tokenParsed.exp + keycloak.timeSkew - new Date().getTime() / 1000
// 					)} seconds`
// 				);
// 			}
// 		})
// 		.catch(() => {
// 			output('Failed to refresh token');
// 		});
// }

// function showExpires() {
// 	if (!keycloak.tokenParsed) {
// 		output('Not authenticated');
// 		return;
// 	}

// 	let o = `Token Expires:\t\t${new Date(
// 		(keycloak.tokenParsed.exp + keycloak.timeSkew) * 1000
// 	).toLocaleString()}\n`;
// 	o += `Token Expires in:\t${Math.round(
// 		keycloak.tokenParsed.exp + keycloak.timeSkew - new Date().getTime() / 1000
// 	)} seconds\n`;

// 	if (keycloak.refreshTokenParsed) {
// 		o += `Refresh Token Expires:\t${new Date(
// 			(keycloak.refreshTokenParsed.exp + keycloak.timeSkew) * 1000
// 		).toLocaleString()}\n`;
// 		o += `Refresh Expires in:\t${Math.round(
// 			keycloak.refreshTokenParsed.exp + keycloak.timeSkew - new Date().getTime() / 1000
// 		)} seconds`;
// 	}

// 	output(o);
// }

// function output(data) {
// 	if (typeof data === 'object') {
// 		data = JSON.stringify(data, null, '  ');
// 	}
// 	console.log(data);
// }

// function event(event) {
// 	const e = document.getElementById('events').innerHTML;
// 	document.getElementById('events').innerHTML = `${new Date().toLocaleString()}\t${event}\n${e}`;
// }

// var keycloak = new Keycloak(conf);

// keycloak.onAuthSuccess = function () {
// 	event('Auth Success');
// };

// keycloak.onAuthError = function (errorData) {
// 	event(`Auth Error: ${JSON.stringify(errorData)}`);
// };

// keycloak.onAuthRefreshSuccess = function () {
// 	event('Auth Refresh Success');
// };

// keycloak.onAuthRefreshError = function () {
// 	event('Auth Refresh Error');
// };

// keycloak.onAuthLogout = function () {
// 	event('Auth Logout');
// };

// keycloak.onTokenExpired = function () {
// 	event('Access token expired.');
// };

// keycloak.onActionUpdate = function (status) {
// 	switch (status) {
// 		case 'success':
// 			event('Action completed successfully');
// 			break;
// 		case 'cancelled':
// 			event('Action cancelled by user');
// 			break;
// 		case 'error':
// 			event('Action failed');
// 			break;
// 	}
// };

// // Flow can be changed to 'implicit' or 'hybrid', but then client must enable implicit flow in admin console too
// const configOptions = {
// 	responseMode: 'fragment',
// 	flow: 'standard'
// };

// keycloak
// 	.init({
// 		onLoad: 'check-sso',
// 		silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`
// 	})
// 	.then((authenticated) => {
// 		output(`Init Success (${authenticated ? 'Authenticated' : 'Not Authenticated'})`);
// 	})
// 	.catch(() => {
// 		output('Init Error');
// 	});
