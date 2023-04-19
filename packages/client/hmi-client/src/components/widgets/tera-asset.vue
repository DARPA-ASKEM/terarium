<template>
	<main @scroll="updateScrollPosition">
		<slot name="nav" />
		<header id="asset-header" ref="header" :class="shrinkHeader && !isCreatingAsset && 'shrinked'">
			<section v-if="!shrinkHeader">
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
			<h4 v-else class="inline" v-html="name" />
			<aside v-if="isEditable">
				<slot name="edit-buttons" />
			</aside>
		</header>
		<section>
			<slot name="default" />
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

defineProps<{
	name: string;
	overline?: string;
	isEditable: boolean;
	isCreatingAsset?: boolean;
	authors?: string;
	doi?: string;
	publisher?: string;
}>();

const header = ref();
const scrollPosition = ref(0);

const shrinkHeader = computed(() => {
	const headerHeight = header.value.clientHeight ? header.value.clientHeight / 2 : 1;
	return scrollPosition.value > headerHeight;
});

function updateScrollPosition(event) {
	scrollPosition.value = event?.currentTarget.scrollTop;
	console.log(scrollPosition.value, header.value?.clientHeight);
}
</script>

<style scoped>
main {
	display: grid;
	/* minmax prevents grid blowout caused by datatable */
	grid-template-columns: auto minmax(0, 1fr);
	grid-template-rows: auto 1fr;
	height: 100%;
	background-color: var(--surface-section);
	scroll-margin-top: 1rem;
	overflow-y: auto;
	overflow-x: hidden;
}

main:deep(> nav) {
	height: fit-content;
	grid-row: 1 / span 2;
}

main > section {
	grid-column-start: 2;
}

header {
	height: fit-content;
	grid-column-start: 2;
	padding: 1rem;
	padding-bottom: 0;
	display: flex;
	gap: 0.5rem;
	justify-content: space-between;
	color: var(--text-color-subdued);
	background-color: var(--surface-section);
	transition: 0.2s;
	overflow-anchor: none;
}

header.shrinked {
	position: sticky;
	top: -1px;
	z-index: 1;
	isolation: isolate;
	padding: 0.5rem 1rem;
	border-bottom: 1px solid var(--surface-border-light);
}

h4.inline {
	display: flex;
	align-self: center;
	overflow: hidden;
	text-align: left;
	text-overflow: ellipsis;
	white-space: nowrap;
	max-width: var(--constrain-width);
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
	scroll-margin-top: 1rem;
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
