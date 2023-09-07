import { defineStore } from 'pinia';
import { User } from '@/types/Types';
import Keycloak from 'keycloak-js';
import axios, { AxiosHeaders } from 'axios';

/**
 * Decode the OIDC token for additional information
 * @param token the OIDC token
 * @returns decoded JSON object representing the token
 * @throws an Error if token is not formatted as expected
 */
/**
 * Main store used for authentication
 */
const useAuthStore = defineStore('auth', {
	state: () => ({
		keycloak: null as Keycloak | null,
		user: null as User | null
	}),
	getters: {
		token: (state) => state?.keycloak?.token
	},
	actions: {
		async init() {
			const response = await axios.get('/api/user/me', {
				headers: new AxiosHeaders().setAuthorization(`Bearer ${this.keycloak?.token}`)
			});
			this.user = response.data;
		},
		setKeycloak(keycloak: Keycloak) {
			this.keycloak = keycloak;
		},
		async logout(options?: Keycloak.KeycloakLogoutOptions) {
			await this.keycloak?.logout(options);
		}
	}
});

export default useAuthStore;
