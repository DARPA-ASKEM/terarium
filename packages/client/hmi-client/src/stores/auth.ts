import { defineStore } from 'pinia';

/**
 * Main store used for authentication
 */
const useAuthStore = defineStore('auth', {
	state: () => ({
		name: null as string | null,
		email: null as string | null,
		initials: null as string | null,
		isAdmin: false as boolean,
		isUser: false as boolean
	}),
	getters: {
		isAuthenticated: (state) => !!state.isUser
	},
	actions: {
		/**
		 * Retrieve access tokens from Keycloak
		 */
		async fetchSSO() {
			// In development mode, bypass Keycloak setting the user to be anonymous
			if (import.meta.env.VITE_BYPASS_KEYCLOAK === 'true') {
				console.log(import.meta.env);
				this.name = 'Dev User';
				this.email = 'dev@terarium.ai';
				this.isUser = true;
			} else {
				// Fetch or refresh the access token
				const response = await fetch('/ua/user');

				const data = await response.json();
				if (response.ok) {
					this.name = data.userName;
					this.email = data.email;
					this.initials = data.initials;
					this.isAdmin = data.admin;
					this.isUser = data.user;
				} else {
					this.logout();
					const error = new Error('Authentication Failed');
					throw error;
				}
			}
		},
		logout() {
			this.name = null;
			this.email = null;
			this.initials = null;
			this.isAdmin = false;
			this.isUser = false;
			window.location.assign('/logout');
		}
	}
});

export default useAuthStore;
