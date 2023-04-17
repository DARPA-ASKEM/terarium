<template>
	<nav>
		<a @click="scrollTo('asset-toc-top')">Top</a>
		<template v-for="content in assetContent">
			<a v-if="!isEmpty(content.value)" :key="content.key" @click="scrollTo(content.key)">
				{{ content.key.replace('-', ' ') }}
				<span v-if="Array.isArray(content.value)"> ({{ content.value.length }}) </span>
			</a>
		</template>
	</nav>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';

defineProps<{
	assetContent: { key: string; value: any }[];
}>();

function scrollTo(elementId: string) {
	document.getElementById(elementId)?.scrollIntoView({ behavior: 'smooth' });
}
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	margin-top: 1rem;
}
</style>
