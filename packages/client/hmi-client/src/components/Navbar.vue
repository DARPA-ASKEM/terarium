<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { useCurrentRoute } from '@/router/index';
import { Project } from '@/types/Project';
import useAuthStore from '@/stores/auth';
import Dialog from 'primevue/dialog';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import { RouteMetadata, RouteName } from '@/router/routes';

const currentRoute = useCurrentRoute();
const pageName = computed(() => {
	if (currentRoute.value.name) {
		const { displayName } = RouteMetadata[currentRoute.value.name];
		return displayName;
	}
	return '';
});
const router = useRouter();
const auth = useAuthStore();

const userMenu = ref();
const isLogoutConfirmationVisible = ref(false);
const userMenuItems = ref([
	{
		label: 'Logout',
		command: () => {
			isLogoutConfirmationVisible.value = !isLogoutConfirmationVisible.value;
		}
	}
]);

defineProps<{
	projectName?: Project['name'];
}>();

const showUserMenu = (event) => {
	userMenu.value.toggle(event);
};

const userInitials = computed(() =>
	auth.name
		?.split(' ')
		.reduce((accumulator, currentValue) => accumulator.concat(currentValue.substring(0, 1)), '')
);

function searchTextChanged(value) {
	router.push({ name: RouteName.DataExplorerRoute, query: { q: value } });
}
</script>

<template>
	<header>
		<img src="@assets/images/logo.png" height="32" width="128" alt="logo" />
		<section class="page-name">
			<p>
				{{ pageName }}
			</p>
		</section>
		<SearchBar class="searchbar" @search-text-changed="searchTextChanged" />
		<section>
			<Button
				class="p-button p-button-icon-only p-button-rounded p-button-sm user-button"
				@click="showUserMenu"
			>
				{{ userInitials }}
			</Button>
		</section>
		<Menu ref="userMenu" :model="userMenuItems" :popup="true"> </Menu>
		<Dialog header="Logout" v-model:visible="isLogoutConfirmationVisible">
			<span>You will be returned to the login screen.</span>
			<template #footer>
				<Button label="Ok" class="p-button-text" @click="auth.logout"></Button>
				<Button
					label="Cancel"
					class="p-button-text"
					@click="isLogoutConfirmationVisible = false"
				></Button>
			</template>
		</Dialog>
	</header>
</template>

<style scoped>
header {
	align-items: center;
	background-color: var(--un-color-body-surface-primary);
	box-shadow: var(--un-box-shadow-small);
	display: flex;
	justify-content: left;
	gap: 2rem;
	min-height: var(--header-height);
	padding: 0.5rem 1rem;
}

p {
	align-items: center;
	display: flex;
	font-size: var(--un-font-xlarge);
}

p > * + *::before {
	content: '>';
	margin: 0 1rem;
}

p a {
	text-decoration: underline;
}

p a:hover,
p a:focus {
	color: var(--un-color-accent-dark);
}

section {
	display: flex;
	margin-left: auto;
	gap: 1rem;
}

.user-button {
	color: var(--un-color-body-text-secondary);
	background-color: var(--un-color-body-surface-background);
}

.user-button:enabled:hover {
	color: var(--un-color-body-text-secondary);
	background-color: var(--un-color-body-surface-secondary);
}

.page-name {
	width: 5%;
	justify-content: center;
}
</style>
