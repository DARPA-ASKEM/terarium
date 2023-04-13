<template>
	<main class="asset" id="Top">
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
				<slot v-if="isEditable" name="right-header-buttons" />
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

h4 {
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
</style>
