<script setup lang="ts">
import { computed, ref, watch, shallowRef } from 'vue';
import { useRouter, RouteParamsRaw } from 'vue-router';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { Project } from '@/types/Project';
import useAuthStore from '@/stores/auth';
import Dialog from 'primevue/dialog';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import { RouteMetadata, RouteName } from '@/router/routes';
import Dropdown from 'primevue/dropdown';
import { useCurrentRoute, RoutePath } from '@/router/index';

const props = defineProps<{
	project: Project | null;
	searchBarText?: string;
	relatedSearchTerms?: string[];
}>();
interface NavItem {
	[key: string]: { name: string; icon: string; routeName: string };
}
const activeProjectName = computed(() => props.project?.name || '');
const activeProjectId = computed(() => props.project?.id);
const currentRoute = useCurrentRoute();
const router = useRouter();
const initialNavItems = {
	[RoutePath.Home]: {
		name: RouteMetadata[RouteName.HomeRoute].displayName,
		icon: RouteMetadata[RouteName.HomeRoute].icon,
		routeName: RouteName.HomeRoute
	},
	[RoutePath.DataExplorer]: {
		name: RouteMetadata[RouteName.DataExplorerRoute].displayName,
		icon: RouteMetadata[RouteName.DataExplorerRoute].icon,
		routeName: RouteName.DataExplorerRoute
	}
};
const emptyNavItem = {
	name: '',
	icon: '',
	routeName: '/'
};
const navItems = shallowRef<NavItem>(initialNavItems);

const selectedPage = ref(navItems.value[currentRoute.value.path] || emptyNavItem);
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

function searchByExampleModalToggled() {
	// TODO
	// toggle the search by example modal represented by the component search-by-example
	// which may be used as follows
	/*
	<search-by-example
		v-if="searchByExampleModal"
		:item="searchByExampleItem"
		@search="onSearchByExample"
		@hide="searchByExampleModal = false"
	/>
	*/
}

function goToPage(event) {
	const routeName = event.value.routeName;
	if (routeName === RouteName.ProjectRoute && activeProjectId.value) {
		const params: RouteParamsRaw = { projectId: activeProjectId.value };
		router.push({ name: routeName, params });
	} else {
		router.push({ name: routeName });
	}
}

watch(activeProjectId, (newProjectId) => {
	const projectNavKey = `/projects/${newProjectId}`;
	const projectNavItem = {
		[projectNavKey]: {
			name: activeProjectName.value,
			icon: 'pi pi-images',
			routeName: RouteName.ProjectRoute
		}
	};
	navItems.value = { ...initialNavItems, ...projectNavItem };
	selectedPage.value = navItems.value[currentRoute.value.path];
});

watch(currentRoute, (newRoute) => {
	selectedPage.value = navItems.value[newRoute.path] || emptyNavItem;
});
</script>

<template>
	<header>
		<section class="header-left">
			<img src="@assets/images/TERArium-logo.png" height="48" width="168" alt="logo" />
			<nav>
				<Dropdown
					class="dropdown"
					v-model="selectedPage"
					:options="Object.values(navItems)"
					optionLabel="name"
					panelClass="dropdown-panel"
					@change="goToPage"
				>
					<template #value="slotProps">
						<i :class="slotProps.value.icon" />
						<span>{{ slotProps.value.name }}</span>
					</template>
					<template #option="slotProps">
						<i :class="slotProps.option.icon" />
						<span>{{ slotProps.option.name }}</span>
					</template>
				</Dropdown>
			</nav>
		</section>
		<SearchBar
			class="searchbar"
			:text="searchBarText"
			:suggested-terms="relatedSearchTerms"
			@search-text-changed="searchTextChanged"
			@toggle-search-by-example="searchByExampleModalToggled"
		/>
		<section class="header-right">
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
	background-color: var(--surface-section);
	min-height: var(--header-height);
	display: flex;
	align-items: flex-start;
	padding: 8px 16px;
	gap: 8px;
	border-bottom: 1px solid var(--surface-border);
	flex: none;
}

section {
	display: flex;
	gap: 0.5rem;
}

.user-button {
	color: var(--text-color-secondary);
	background-color: var(--surface-ground);
}

.user-button:enabled:hover {
	color: var(--text-color-secondary);
	background-color: var(--surface-secondary);
}

nav {
	justify-content: space-between;
	flex: 0.5;
	margin-right: auto;
}

.header-left {
	flex: 1;
}

.header-right {
	flex: 1;
	justify-content: right;
}

.searchbar {
	flex: 2;
}

.p-dropdown {
	height: 3rem;
	width: 9rem;
	flex: 1;
	border: 0;
}

.p-dropdown:not(.p-disabled).p-focus {
	box-shadow: none;
}

.p-dropdown-label {
	color: var(--text-color-secondary);
}

i {
	margin-right: 0.5rem;
}

.logo {
	align-self: center;
}
</style>
