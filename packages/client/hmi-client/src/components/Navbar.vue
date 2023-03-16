<template>
	<header>
		<section class="header-left">
			<router-link :to="RoutePath.Home">
				<img
					v-if="currentProjectId"
					src="@assets/svg/terarium-icon.svg"
					height="30"
					alt="Terarium icon"
				/>
				<img v-else src="@assets/svg/terarium-logo.svg" height="30" alt="Terarium logo" />
			</router-link>
			<h1 v-if="currentProjectId" @click="showNavigationMenu">
				{{ currentProjectName }}
				<i class="pi pi-angle-down" />
			</h1>
			<Menu ref="navigationMenu" :model="navMenuItems" :popup="true" class="navigation-menu" />
		</section>
		<SearchBar
			v-if="active"
			class="search-bar"
			ref="searchBarRef"
			:show-suggestions="showSuggestions"
			@query-changed="updateRelatedTerms"
			@toggle-search-by-example="searchByExampleModalToggled"
		/>
		<section v-if="active" class="header-right">
			<Avatar :label="userInitials" class="avatar m-2" shape="circle" @click="showUserMenu" />
			<Menu ref="userMenu" :model="userMenuItems" :popup="true" />
			<Dialog header="Logout" v-model:visible="isLogoutDialog">
				<p>You will be returned to the login screen.</p>
				<template #footer>
					<Button label="Cancel" class="p-button-secondary" @click="closeLogoutDialog" />
					<Button label="Ok" @click="auth.logout" />
				</template>
			</Dialog>
		</section>
		<aside class="suggested-terms" v-if="!isEmpty(terms)">
			Suggested terms:
			<Chip v-for="term in terms" :key="term" removable remove-icon="pi pi-times">
				<span @click="searchBarRef?.addToQuery(term)">{{ term }}</span>
			</Chip>
		</aside>
	</header>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { isEmpty } from 'lodash';
import Avatar from 'primevue/avatar';
import Button from 'primevue/button';
import Chip from 'primevue/chip';
import Dialog from 'primevue/dialog';
import Menu from 'primevue/menu';
import SearchBar from '@/page/data-explorer/components/search-bar.vue';
import { RoutePath } from '@/router/index';
import { RouteMetadata, RouteName } from '@/router/routes';
import { getRelatedTerms } from '@/services/data';
import useAuthStore from '@/stores/auth';
import { IProject } from '@/types/Project';
import { MenuItem } from 'primevue/menuitem';

const props = defineProps<{
	active: boolean;
	currentProjectId: IProject['id'] | null;
	projects: IProject[] | null;
	showSuggestions: boolean;
}>();

/*
 * Navigation Menu
 */
const router = useRouter();
const navigationMenu = ref();
const homeItem: MenuItem = {
	label: RouteMetadata[RouteName.HomeRoute].displayName,
	icon: RouteMetadata[RouteName.HomeRoute].icon,
	command: () => router.push(RoutePath.Home)
};
const navMenuItems = ref<MenuItem[]>([homeItem]);
const showNavigationMenu = (event) => {
	navigationMenu.value.toggle(event);
};

/*
 * User Menu
 */
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

function closeLogoutDialog() {
	isLogoutDialog.value = false;
}

/*
 * Search
 */
const searchBarRef = ref();
const terms = ref<string[]>([]);

async function updateRelatedTerms(q?: string) {
	terms.value = await getRelatedTerms(q);
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

/*
 * Reactive
 */
const currentProjectName = computed(
	() => props.projects?.find((project) => project.id === props.currentProjectId?.toString())?.name
);

watch(
	() => props.projects,
	() => {
		if (props.projects) {
			const items: MenuItem[] = [];
			props.projects?.forEach((project) => {
				items.push({
					label: project.name,
					icon: 'pi pi-folder',
					command: () =>
						router.push({ name: RouteName.ProjectRoute, params: { projectId: project.id } })
				});
			});
			navMenuItems.value = [homeItem, { label: 'Projects', items }];
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
header {
	background-color: var(--surface-section);
	border-bottom: 1px solid var(--surface-border-light);
	padding: 0.5rem 1rem;
	display: grid;
	column-gap: 0.5rem;
	grid-template-areas:
		'header-left search-bar header-right'
		'suggested-terms suggested-terms suggested-terms';
	grid-template-columns: minMax(max-content, 25%) auto minMax(min-content, 25%);
}

h1 {
	border-color: transparent;
	border-style: solid;
	border-radius: var(--border-radius);
	border-width: 1px;
	cursor: pointer;
	font-size: var(--font-body-large);
	font-weight: var(--font-weight-semibold);
	padding: 0.5rem;
}

h1:hover,
h1:focus {
	border-color: var(--primary-color);
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
	margin-left: auto;
}

.avatar {
	color: var(--text-color-subdued);
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

.header-left:deep(.p-dropdown-label.p-inputtext) {
	padding-right: 0;
}

.p-dropdown {
	border: 0;
}

.p-dropdown-label {
	color: var(--text-color-secondary);
}

i {
	margin-left: 0.5rem;
}

/* Suggested terms */
.suggested-terms {
	align-items: center;
	font-weight: 400;
	color: var(--text-color-subdued);
	display: flex;
	column-gap: 0.5rem;
	font-size: var(--font-caption);
	grid-area: suggested-terms;
	justify-content: center;
	margin-top: 0.5rem;
	white-space: nowrap;
}

.clear-search-terms:enabled {
	color: var(--text-color-secondary);
	background-color: transparent;
}

.clear-search-terms:enabled:hover {
	background-color: var(--surface-hover);
	color: var(--text-color-secondary);
}
</style>
<style>
/*
 * On it's own style, because the pop-up happend outside of this component.
 * To left align the content with the h1.
 */
.navigation-menu {
	margin-left: -0.5rem;
}
</style>
