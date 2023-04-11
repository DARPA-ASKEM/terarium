<template>
	<section class="asset" id="Top">
		<header>
			<section>
				<span class="asset-form">{{ assetForm }}</span>
				<h4 v-html="name" />
				<div v-if="authors" class="authors" v-html="authors" />
				<div v-if="doi">
					DOI: <a :href="`https://doi.org/${doi}`" rel="noreferrer noopener" v-html="doi" />
				</div>
				<slot name="bottom-buttons" />
			</section>
			<slot v-if="isEditable" name="right-buttons" />
		</header>
		<p v-if="description">
			{{ description }}
		</p>
		<slot name="default" />
	</section>
</template>

<script setup lang="ts">
defineProps<{
	name: string;
	assetForm: string;
	isEditable: boolean;
	description?: string;
	authors?: string;
	doi?: string;
}>();
</script>

<style scoped>
p {
	padding: 1rem;
	padding-bottom: 0.5rem;
	max-width: var(--constrain-width);
}

.bottom-buttons {
	display: flex;
	gap: 0.5rem;
}
</style>
