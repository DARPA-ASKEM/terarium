<template>
	<main v-if="!isLoading" @scroll="updateScrollPosition">
		<slot name="nav" />
		<header v-if="shrinkHeader || showStickyHeader" class="shrinked">
			<h4 v-html="name" />
			<aside class="spread-out">
				<slot name="edit-buttons" />
				<Button
					v-if="featureConfig.isPreview"
					icon="pi pi-times"
					class="close p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
					@click="emit('close-preview')"
				/>
			</aside>
		</header>
		<template v-if="!hideIntro">
			<header
				id="asset-top"
				:class="{
					'overview-banner': pageType === ProjectPages.OVERVIEW,
					'with-tabs': tabs.length > 1
				}"
				ref="headerRef"
			>
				<section>
					<!-- put the buttons above the title if there is an overline -->
					<div v-if="overline" class="vertically-center">
						<span class="overline">{{ overline }}</span>
						<slot name="edit-buttons" />
					</div>
					<slot name="info-bar" />

					<!--For naming asset such as model or code file-->
					<div class="vertically-center">
						<slot name="name-input" />
						<h4 v-if="!isNamingAsset" v-html="name" />

						<div v-if="!overline" class="vertically-center">
							<slot name="edit-buttons" />
						</div>
					</div>

					<!--put model contributors here too-->
					<span class="authors" v-if="authors">
						<i :class="authors.includes(',') ? 'pi pi-users' : 'pi pi-user'" />
						<span v-html="authors" />
					</span>
					<div v-if="doi">
						DOI: <a :href="`https://doi.org/${doi}`" rel="noreferrer noopener" v-html="doi" />
					</div>
					<div v-if="publisher" v-html="publisher" />
					<!--created on: date-->
					<div class="header-buttons">
						<slot name="bottom-header-buttons" />
					</div>
					<slot name="overview-summary" />
					<TabView
						v-if="tabs.length > 1"
						:active-index="selectedTabIndex"
						@tab-change="(e) => emit('tab-change', e)"
					>
						<TabPanel v-for="(tab, index) in tabs" :key="index" :header="tab.props?.tabName" />
					</TabView>
				</section>
				<aside v-if="pageType !== ProjectPages.OVERVIEW" class="spread-out">
					<Button
						v-if="featureConfig.isPreview"
						icon="pi pi-times"
						class="close p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
						@click="emit('close-preview')"
					/>
				</aside>
			</header>
		</template>
		<section :class="overflowHiddenClass" :style="stretchContentStyle">
			<template v-for="(tab, index) in tabs" :key="index">
				<component :is="tab" v-show="selectedTabIndex === index" />
			</template>
			<slot name="default" />
		</section>
	</main>
	<tera-progress-spinner v-else :font-size="2" is-centered />
</template>

<script setup lang="ts">
import { ref, computed, watch, PropType, useSlots } from 'vue';
import { useRoute } from 'vue-router';
import Button from 'primevue/button';
import { FeatureConfig } from '@/types/common';
import { ProjectPages } from '@/types/Project';
import { AssetType } from '@/types/Types';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import teraProgressSpinner from '../widgets/tera-progress-spinner.vue';

const props = defineProps({
	name: {
		type: String,
		default: ''
	},
	overline: {
		type: String,
		default: null
	},
	authors: {
		type: String,
		default: null
	},
	doi: {
		type: String,
		default: null
	},
	publisher: {
		type: String,
		default: null
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	},
	// Booleans default to false if not specified
	isNamingAsset: Boolean,
	hideIntro: Boolean,
	showStickyHeader: Boolean,
	stretchContent: Boolean,
	isLoading: Boolean,
	overflowHidden: Boolean,
	selectedTabIndex: {
		type: Number,
		default: 0
	}
});

const emit = defineEmits(['close-preview', 'tab-change']);

const slots = useSlots();
const headerRef = ref();
const scrollPosition = ref(0);

const shrinkHeader = computed(() => {
	const headerHeight = headerRef.value?.clientHeight ? headerRef.value.clientHeight - 50 : 1;
	return (
		scrollPosition.value > headerHeight && // Appear if (original header - 50px) is scrolled past
		scrollPosition.value !== 0 && // Handles case where original header is shorter than shrunk header (happens in PDF view)
		!props.isNamingAsset // Don't appear while creating an asset eg. a model
	);
});

const pageType = useRoute().params.pageType as ProjectPages | AssetType;

// Scroll margin for anchors are adjusted depending on the header (inserted in css)
const scrollMarginTopStyle = computed(() => (shrinkHeader.value ? '3.5rem' : '0.5rem'));
const stretchContentStyle = computed(() =>
	props.stretchContent ? { gridColumn: '1 / span 2' } : {}
);

const overflowHiddenClass = computed(() => (props.overflowHidden ? 'overflow-hidden' : ''));

function updateScrollPosition(event) {
	scrollPosition.value = event?.currentTarget.scrollTop;
}

const tabs = computed(() => {
	if (slots.tabs?.()) {
		if (slots.tabs().length === 1) {
			// if there is only 1 component we don't need to know the tab name and we can render it.
			return slots.tabs();
		}

		return slots.tabs().filter((vnode) => vnode.props?.tabName);
	}
	return [];
});

// Reset the scroll position to the top on asset change
watch(
	() => props.name,
	() => {
		document.getElementById('asset-top')?.scrollIntoView();
	}
);
</script>

<style scoped>
main {
	display: grid;
	/* minmax prevents grid blowout caused by datatable */
	grid-template-columns: auto minmax(0, 1fr);
	grid-template-rows: auto 1fr;
	height: 100%;
	background-color: var(--surface-section);
	/* accounts for sticky header height */
	scroll-margin-top: v-bind('scrollMarginTopStyle');
	overflow-y: auto;
	overflow-x: hidden;
}

main > section {
	grid-column-start: 2;
}

header {
	display: flex;
	flex-direction: row;
	height: fit-content;
	grid-column-start: 2;
	color: var(--text-color-subdued);
	padding: var(--gap-small) var(--gap);
	transition: 0.2s;
	display: flex;
	gap: var(--gap);
	align-items: center;
	border-bottom: 1px solid var(--surface-border-light);
	background: var(--surface-disabled);
}

header.shrinked {
	height: 3rem;
	position: sticky;
	top: -1px;
	z-index: 100;
	isolation: isolate;
	background-color: rgba(255, 255, 255, 0.85);
	backdrop-filter: blur(6px);
	padding: var(--gap-small) var(--gap);
	border-bottom: 1px solid var(--surface-border-light);
	box-shadow: 0px 4px 8px -7px #b8b8b8;
}

header.shrinked h4 {
	align-self: center;
	overflow: hidden;
	text-align: left;
	text-overflow: ellipsis;
	white-space: nowrap;
	max-width: var(--constrain-width);
	font-size: var(--font-body-small);
}

h4,
header section p {
	color: var(--text-color-primary);
}

header section,
header aside {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
	max-width: var(--constrain-width);
}

header aside {
	align-self: flex-start;
	/* Prevent button stretch */
	max-height: 2.5rem;
}

header.shrinked aside {
	align-self: center;
}
header.overview-banner section {
	width: 100%;
	max-width: 100%;
}

header.with-tabs {
	padding: var(--gap-small) var(--gap) 0;
}

.overview-banner {
	background:
		url('@/assets/svg/terarium-icon-transparent.svg') no-repeat right 20% center,
		linear-gradient(45deg, #8bd4af1a, #d5e8e5 100%) no-repeat;
	background-size: 25%, 100%;
}

.vertically-center {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: var(--gap);
}

main:deep(.p-inputtext.p-inputtext-sm) {
	padding: 0.65rem 0.65rem 0.65rem 3rem;
}

/* Input asset name */
header section:deep(> input) {
	width: var(--constrain-width);
	font-size: var(--font-body-medium);
}

.overline,
.authors {
	color: var(--text-color-primary);
}

.authors i {
	color: var(--text-color-primary);
	margin-right: var(--gap-small);
}

.header-buttons:empty {
	display: none;
}

.header-buttons,
header aside {
	display: flex;
	flex-direction: row;
	gap: var(--gap-small);
}

/* Affects child components put in the slot*/
main:deep(.p-accordion) {
	margin: var(--gap-small);
}

/*  Gives some top padding when you auto-scroll to an anchor */
main:deep(.p-accordion-header > a > header) {
	scroll-margin-top: v-bind('scrollMarginTopStyle');
}

main:deep(.p-accordion-content) {
	padding-bottom: var(--gap-small);
}

main:deep(.p-accordion-content > p),
main:deep(.p-accordion-content > ul),
main:deep(.data-row) {
	max-width: var(--constrain-width);
}

main:deep(.p-accordion-content ul) {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
	list-style: none;
}

main:deep(.p-accordion-content > textarea) {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: 5px;
	resize: none;
	overflow-y: hidden;
	width: 100%;
}

main:deep(.artifact-amount) {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}

/* These styles should probably be moved to the general theme in some form */
main:deep(input) {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: 0.75rem;
}

main:deep(.p-button.p-button-outlined) {
	color: var(--text-color-primary);
	box-shadow: var(--text-color-disabled) inset 0 0 0 1px;
}

.overflow-hidden {
	overflow: hidden;
}

.spread-out {
	align-items: center;
	justify-content: space-between;
	flex-grow: 1;
}

:deep(.p-tabview .p-tabview-panels) {
	padding: 0;
}

:deep(.p-tabview-header:not(.p-highlight) .p-tabview-nav-link) {
	background: var(--tab-backgroundcolor-unselected);
}

:deep(.p-tabview .p-tabview-nav li .p-tabview-nav-link:focus) {
	background-color: var(--surface-section);
}
</style>
