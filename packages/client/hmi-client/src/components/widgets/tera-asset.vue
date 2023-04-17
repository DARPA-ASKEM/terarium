<template>
	<main id="asset-toc-top">
		<header>
			<section>
				<span v-if="overline" class="overline">{{ overline }}</span>
				<!--For naming asset such as model or code file-->
				<slot v-if="isCreatingAsset" name="name-input" />
				<h4 v-else v-html="name" />
				<div class="authors" v-if="authors">
					<i :class="authors.includes(',') ? 'pi pi-users' : 'pi pi-user'" />
					<div v-html="authors" />
					<!--contributor-->
				</div>
				<div v-if="doi">
					DOI: <a :href="`https://doi.org/${doi}`" rel="noreferrer noopener" v-html="doi" />
				</div>
				<div v-if="publisher" v-html="publisher" />
				<!--created on: date-->
				<div class="header-buttons">
					<slot name="bottom-header-buttons" />
				</div>
				<p v-if="description">
					{{ description }}
				</p>
			</section>
			<aside v-if="isEditable">
				<slot name="edit-buttons" />
			</aside>
		</header>
		<slot name="default" />
	</main>
</template>

<script setup lang="ts">
defineProps<{
	name: string;
	overline?: string;
	isEditable: boolean;
	isCreatingAsset?: boolean;
	description?: string;
	authors?: string;
	doi?: string;
	publisher?: string;
}>();
</script>

<style scoped>
main {
	display: flex;
	flex: 1;
	height: fit-content;
	flex-direction: column;
	background-color: var(--surface-section);
	scroll-margin-top: 1rem;
	overflow: auto;
}

header {
	padding: 1rem;
	padding-bottom: 0.5rem;
	color: var(--text-color-subdued);
	display: flex;
	gap: 0.5rem;
	justify-content: space-between;
	position: sticky;
	top: 0;
	background-color: var(--surface-section);
	z-index: 1;
	isolation: isolate;
	border-bottom: 1px solid var(--surface-border-light);
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

header aside:deep(.p-inputtext.p-inputtext-sm) {
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

.authors {
	display: flex;
	gap: 0.5rem;
}

.authors i {
	color: var(--text-color-subdued);
}

.header-buttons,
header aside {
	display: flex;
	flex-direction: row;
	gap: 0.5rem;
}

/* Affects child components put in the slot*/
main:deep(.p-accordion) {
	padding: 0.5rem;
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
header:deep(input) {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: 0.75rem;
}

main:deep(.p-button.p-button-outlined) {
	color: var(--text-color-primary);
	box-shadow: var(--text-color-disabled) inset 0 0 0 1px;
}
</style>
