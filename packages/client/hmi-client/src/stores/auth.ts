import { defineStore } from 'pinia';
import { User } from '@/types/Types';
import Keycloak from 'keycloak-js';
import axios, { AxiosHeaders } from 'axios';
import { computed, ref } from 'vue';

/**
 * Main store used for authentication
 */
const useAuthStore = defineStore('auth', () => {
	// keycloak
	const keycloak = ref<Keycloak | null>(null);
	const setKeycloak = (newKeycloak: Keycloak) => {
		keycloak.value = newKeycloak;
	};
	const logout = async (options?: Keycloak.KeycloakLogoutOptions) => {
		await keycloak.value?.logout(options);
	};

	const token = computed(() => keycloak.value?.token);

	// user
	const user = ref<User | null>(null);
	const updateUser = async (updatedUser: User) => {
		const response = await axios.put('/api/user/me', updatedUser, {
			headers: new AxiosHeaders().setAuthorization(`Bearer ${token.value}`)
		});
		user.value = response.data;
	};

	const loadUserModel = async () => {
		const response = await axios.get('/api/user/me', {
			headers: new AxiosHeaders().setAuthorization(`Bearer ${token.value}`)
		});
		user.value = response.data;
	};
	const userInitials = computed(
		() => `${user.value?.givenName?.charAt(0)}${user.value?.familyName?.charAt(0)}`
	);

	const init = async () => {
		await loadUserModel();
	};

	return {
		keycloak,
		setKeycloak,
		logout,
		token,
		user,
		updateUser,
		loadUserModel,
		userInitials,
		init
	};
});

export default useAuthStore;
