<template>
	<main @scroll="updateScrollPosition">
		<slot name="nav" />
		<header v-if="shrinkHeader" class="shrinked">
			<h4 v-html="name" />
			<aside>
				<slot v-if="isEditable" name="edit-buttons" />
				<Button
					v-else
					icon="pi pi-times"
					class="close p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
					@click="emit('close-preview')"
				/>
			</aside>
		</header>
		<header id="asset-top" ref="headerRef">
			<template v-if="!hideHeader">
				<section>
					<span v-if="overline" class="overline">{{ overline }}</span>
					<!--For naming asset such as model or code file-->
					<slot v-if="isCreatingAsset" name="name-input" />
					<h4 v-else v-html="name" />
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
				</section>
				<aside>
					<slot v-if="isEditable" name="edit-buttons" />
					<Button
						v-else
						icon="pi pi-times"
						class="close p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
						@click="emit('close-preview')"
					/>
				</aside>
			</template>
		</header>
		<section :style="stretchContentStyle">
			<slot name="default" />
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	name: string;
	overline?: string;
	isEditable: boolean;
	isCreatingAsset?: boolean;
	authors?: string;
	doi?: string;
	publisher?: string;
	hideHeader?: boolean;
	stretchContent?: boolean;
}>();

const emit = defineEmits(['close-preview']);

const headerRef = ref();
const scrollPosition = ref(0);

const shrinkHeader = computed(() => {
	const headerHeight = headerRef.value?.clientHeight ? headerRef.value.clientHeight - 50 : 1;
	return (
		scrollPosition.value > headerHeight && // Appear if (original header - 50px) is scrolled past
		scrollPosition.value !== 0 && // Handles case where original header is shorter than shrunk header (happens in PDF view)
		!props.isCreatingAsset // Don't appear while creating an asset eg. a model
	);
});

// Scroll margin for anchors are adjusted depending on the header (inserted in css)
const scrollMarginTopStyle = computed(() => (shrinkHeader.value ? '3.5rem' : '0.5rem'));
const stretchContentStyle = computed(() =>
	props.stretchContent ? { gridColumn: '1 / span 2' } : {}
);

function updateScrollPosition(event) {
	scrollPosition.value = event?.currentTarget.scrollTop;
}

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
	height: fit-content;
	grid-column-start: 2;
	color: var(--text-color-subdued);
	padding: 1rem;
	padding-bottom: 0;
	transition: 0.2s;
	display: flex;
	gap: 0.5rem;
	justify-content: space-between;
}

header.shrinked {
	height: 3rem;
	position: sticky;
	top: -1px;
	z-index: 1;
	isolation: isolate;
	background-color: rgba(255, 255, 255, 0.85);
	backdrop-filter: blur(6px);
	padding: 0.5rem 1rem;
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
	gap: 0.5rem;
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
	color: var(--primary-color-dark);
}

.authors i {
	color: var(--text-color-subdued);
	margin-right: 0.5rem;
}

.header-buttons,
header aside {
	display: flex;
	flex-direction: row;
	gap: 0.5rem;
}

/* Affects child components put in the slot*/
main:deep(.p-accordion) {
	margin: 0.5rem;
}

/*  Gives some top padding when you auto-scroll to an anchor */
main:deep(.p-accordion-header > a > header) {
	scroll-margin-top: v-bind('scrollMarginTopStyle');
}

main:deep(.p-accordion-content > p),
main:deep(.p-accordion-content > ul),
main:deep(.data-row) {
	max-width: var(--constrain-width);
}

main:deep(.p-accordion-content ul) {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
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
</style>
