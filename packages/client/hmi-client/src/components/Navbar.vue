<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { useCurrentRouter } from '@/router/index';
import { Project } from '@/types/Project';
import useResourcesStore from '@/stores/resources';
import useAuthStore from '@/stores/auth';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';

const emit = defineEmits(['show-data-explorer']);
const router = useRouter();
const { isCurrentRouteHome } = useCurrentRouter();
const auth = useAuthStore();
const isHome = computed(() => isCurrentRouteHome.value);
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
const resources = useResourcesStore();

const goToHomepage = () => {
	resources.setActiveProject(null);
	resources.activeProjectAssets = null;
	router.push('/');
};

defineProps<{
	projectName?: Project['name'];
}>();

const showUserMenu = (event) => {
	userMenu.value.toggle(event);
};
</script>

<template>
	<header>
		<img v-if="isHome" src="@assets/images/logo.png" height="32" width="128" alt="logo" />
		<img v-else src="@assets/images/icon.png" height="32" width="32" alt="TERArium icon" />
		<p v-if="!isHome">
			<a @click="goToHomepage">Projects</a>
			<span>{{ projectName }}</span>
		</p>
		<span class="p-input-icon-left">
			<i class="pi pi-search" />
			<InputText type="text" placeholder="Search" />
		</span>
		<aside>
			<Button
				class="p-button p-button-icon-only p-button-rounded"
				@click="emit('show-data-explorer')"
				aria-label="Data Explorer"
			>
				<i class="pi pi-search" />
			</Button>
			<Button
				class="p-button p-button-icon-only p-button-rounded p-button-sm"
				id="user-button"
				@click="showUserMenu"
			>
			</Button>
		</aside>
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
	justify-content: space-between;
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

aside {
	display: flex;
	/* Push it to the far side */
	gap: 1rem;
}

.p-button {
	background-color: var(--un-color-accent);
}

.p-button:enabled:hover,
.p-button:enabled:focus {
	background-color: var(--un-color-accent-light);
}

#user-button {
	color: var(--un-color-text-secondary);
	background-color: var(--un-color-body-surface-background);
}

#user-button:hover {
	background-color: var(--un-color-body-surface-secondary);
}

.p-input-icon-left {
	width: 50%;
}

.p-inputtext {
	height: 3rem;
	border-radius: 1.5rem;
}
</style>
