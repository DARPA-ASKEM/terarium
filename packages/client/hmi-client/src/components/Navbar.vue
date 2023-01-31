<script setup lang="ts">
import { computed, ref, watch, shallowRef } from 'vue';
import { useRouter, RouteParamsRaw } from 'vue-router';
import Button from 'primevue/button';
import Avatar from 'primevue/avatar';
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
const isLogoutDialog = ref(false);
const userMenuItems = ref([
	{
		label: 'Logout',
		command: () => {
			isLogoutDialog.value = true;
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

function updateProjectNavItem(id, name) {
	const projectNavKey = `/projects/${id}`;
	const projectNavItem = {
		[projectNavKey]: {
			name,
			icon: 'pi pi-clone',
			routeName: RouteName.ProjectRoute
		}
	};
	navItems.value = { ...initialNavItems, ...projectNavItem };
	selectedPage.value = navItems.value[currentRoute.value.path];
}

function closeLogoutDialog() {
	isLogoutDialog.value = false;
}

watch(activeProjectId, (newProjectId) => {
	updateProjectNavItem(newProjectId, activeProjectName.value);
});

watch(activeProjectName, (newProjectName) => {
	updateProjectNavItem(activeProjectId.value, newProjectName);
});

watch(currentRoute, (newRoute) => {
	selectedPage.value = navItems.value[newRoute.path] || emptyNavItem;
});
</script>

<template>
	<header>
		<section class="header-left">
			<img src="@assets/svg/terarium-logo.svg" height="36" alt="TERArium logo" />
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
			<Avatar :label="userInitials" class="avatar m-2" shape="circle" @click="showUserMenu" />
		</section>
		<Menu ref="userMenu" :model="userMenuItems" :popup="true" />
		<Dialog header="Logout" v-model:visible="isLogoutDialog">
			<p>You will be returned to the login screen.</p>
			<template #footer>
				<Button label="Cancel" class="p-button-secondary" @click="closeLogoutDialog" />
				<Button label="Ok" @click="auth.logout" />
			</template>
		</Dialog>
	</header>
</template>

<style scoped>
header {
	align-items: center;
	background-color: var(--surface-section);
	display: flex;
	border-bottom: 1px solid var(--surface-border);
	padding: 0.5rem 1rem;
	justify-content: space-between;
}

.searchbar {
	flex-grow: 3;
}

.avatar {
	color: var(--text-subdued);
	background-color: var(--surface-ground);
	cursor: pointer;
}

.avatar:hover {
	color: var(--text-color);
	background-color: var(--surface-hover);
}

.header-left {
	align-items: center;
	display: flex;
	height: 100%;
	gap: 1rem;
}

.header-left >>> .p-dropdown-label.p-inputtext {
	padding-right: 0;
}

.p-dropdown {
	border: 0;
}

.p-dropdown-label {
	color: var(--text-color-secondary);
}

i {
	margin-right: 0.5rem;
}
</style>
