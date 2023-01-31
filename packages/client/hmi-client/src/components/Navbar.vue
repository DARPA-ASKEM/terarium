<script setup lang="ts">
import { computed, ref, watch, shallowRef } from 'vue';
import { useRouter, RouteParamsRaw } from 'vue-router';
import Button from 'primevue/button';
import Avatar from 'primevue/avatar';
import Menu from 'primevue/menu';
import { Project } from '@/types/Project';
import useAuthStore from '@/stores/auth';
import Chip from 'primevue/chip';
import Dialog from 'primevue/dialog';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import { RouteMetadata, RouteName } from '@/router/routes';
import Dropdown from 'primevue/dropdown';
import { useCurrentRoute, RoutePath } from '@/router/index';
import { isEmpty } from 'lodash';
import { getRelatedWords } from '@/services/data';

const props = defineProps<{
	project: Project | null;
	searchBarText?: string;
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

/*
 * Search text
 */

const query = ref<string>();

function searchTextChanged(value) {
	router.push({ name: RouteName.DataExplorerRoute, query: { q: value } });
}

/*
 * Suggested Terms
 */

const terms = ref<string[]>([]);

watch(
	() => props.searchBarText,
	async (newSearchTerm) => {
		if (newSearchTerm) {
			terms.value = await getRelatedWords(newSearchTerm);
		} else {
			terms.value = [];
		}
	},
	{ immediate: true }
);

function addSearchTerm(term) {
	query.value = query.value ? query.value.concat(' ').concat(term) : term;
}
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
			class="search-bar"
			:text="query"
			@search-text-changed="searchTextChanged"
			@toggle-search-by-example="searchByExampleModalToggled"
		/>
		<section class="header-right">
			<Avatar :label="userInitials" class="avatar m-2" shape="circle" @click="showUserMenu" />
		</section>
		<aside class="suggested-terms" v-if="!isEmpty(terms)">
			Suggested terms:
			<Chip v-for="term in terms" :key="term" removable remove-icon="pi pi-times">
				<span @click="addSearchTerm(term)">{{ term }}</span>
			</Chip>
		</aside>
	</header>
	<Menu ref="userMenu" :model="userMenuItems" :popup="true" />
	<Dialog header="Logout" v-model:visible="isLogoutDialog">
		<p>You will be returned to the login screen.</p>
		<template #footer>
			<Button label="Cancel" class="p-button-secondary" @click="closeLogoutDialog" />
			<Button label="Ok" @click="auth.logout" />
		</template>
	</Dialog>
</template>

<style scoped>
header {
	background-color: var(--surface-section);
	border-bottom: 1px solid var(--surface-border);
	padding: 0.5rem 1rem;

	display: grid;
	gap: 0.5rem;
	grid-template-areas:
		'header-left search-bar header-right'
		'. suggested-terms .';
	grid-template-columns: minMax(max-content, 25%) auto min-content;
	grid-template-rows: max-content max-content;
}

/* Search Bar */

.search-bar {
	grid-area: search-bar;
	margin-left: auto;
	margin-right: auto;
	min-width: 25vw;
	max-width: 60rem;
	width: 100%;
}

/* Header Right */

.header-right {
	grid-area: header-right;
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

/* Header Left */
.header-left {
	align-items: center;
	display: flex;
	gap: 1rem;
	grid-area: header-left;
	height: 100%;
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

/* Suggested terms */
.suggested-terms {
	align-items: center;
	display: flex;
	gap: 0.5rem;
	grid-area: suggested-terms;
	justify-content: center;
	overflow: hidden;
	white-space: nowrap;
}

.suggested-terms,
.p-chip {
	font-size: small;
	font-weight: bold;
	color: var(--text-color-subdued);
}

.p-chip {
	padding: 0 0.75rem;
	background-color: var(--surface-200);
}

.p-chip span {
	margin: 0.25rem 0;
	cursor: pointer;
}

.p-chip :deep(.p-chip-remove-icon) {
	font-size: 0.75rem;
}
</style>
