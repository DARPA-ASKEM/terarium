<template>
	<main id="asset-toc-top">
		<header>
			<section>
				<span class="asset-form">{{ assetForm }}</span>
				<h4 v-html="name" />
				<div v-if="authors" class="authors" v-html="authors" />
				<div v-if="doi">
					DOI: <a :href="`https://doi.org/${doi}`" rel="noreferrer noopener" v-html="doi" />
				</div>
				<div v-if="publisher" v-html="publisher" />
				<!--Model header details that aren't in yet-->
				<!--contributor-->
				<!--created on: date-->
				<div class="header-buttons">
					<slot name="bottom-header-buttons" />
				</div>
				<p v-if="description">
					{{ description }}
				</p>
			</section>
			<section class="header-buttons">
				<slot v-if="isEditable" name="edit-buttons" />
			</section>
		</header>
		<slot name="default" />
	</main>
</template>

<script setup lang="ts">
defineProps<{
	name: string;
	assetForm: string;
	isEditable: boolean;
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
}

header {
	padding: 1rem;
	color: var(--text-color-subdued);
	display: flex;
	gap: 0.5rem;
	justify-content: space-between;
}

h4,
header section p {
	color: var(--text-color-primary);
}

header section {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	max-width: var(--constrain-width);
}

.asset-form,
.authors {
	color: var(--primary-color-dark);
}

.header-buttons {
	display: flex;
	align-self: flex-start;
	gap: 0.5rem;
}

/* Affects child components put in the slot*/
main:deep(p),
main:deep(ul),
main:deep(.data-row) {
	max-width: var(--constrain-width);
}

main:deep(.p-accordion) {
	padding: 0.5rem;
}

/*  Gives some top padding when you auto-scroll to the anchor */
main:deep(.p-accordion header) {
	scroll-margin-top: 1rem;
}

main:deep(.p-accordion ul) {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	list-style: none;
}

main:deep(.artifact-amount) {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}

/* This button style should probably be moved to the general theme in some form */
main:deep(.p-button.p-button-outlined) {
	color: var(--text-color-primary);
	box-shadow: var(--text-color-disabled) inset 0 0 0 1px;
}
</style>
