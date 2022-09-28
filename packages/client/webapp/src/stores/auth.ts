import { defineStore } from 'pinia';

let timer: NodeJS.Timeout;
/**
 * Main store used for authentication
 */
// eslint-disable-next-line import/prefer-default-export
export const useAuthStore = defineStore('auth', {
	state: () => ({
		userId: null,
		userToken: null as string | null,
		expires: null as number | null,
		name: null,
		email: null
	}),
	getters: {
		token: (state) => state.userToken,
		isAuthenticated: (state) => !!state.userToken
	},
	actions: {
		/**
		 * Retrieve access tokens from Keycloak
		 */
		async fetchSSO() {
			const response = await fetch('/silent-check-sso.html');

			if (!response.ok) {
				const error = new Error('Authentication Failed');
				throw error;
			}

			const accessToken = response.headers.get('OIDC_access_token');
			const expirationTimestamp =
				+(response?.headers?.get('OIDC_access_token_expires') ?? 0) * 1000;
			const expiresIn = expirationTimestamp - new Date().getTime();

			this.userToken = accessToken;

			// TODO: seems the expiration is not correct upon renewal
			timer = setTimeout(() => {
				this.autoRenew();
			}, expiresIn);
		},
		logout() {
			this.userId = null;
			this.userToken = null;
		},
		autoRenew() {
			console.log('RENEW SSO');
			clearTimeout(timer);
			// TODO: reenable when fixed
			// this.fetchSSO();
		}
	}
});
