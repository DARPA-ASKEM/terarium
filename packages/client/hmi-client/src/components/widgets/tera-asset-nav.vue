<template>
	<nav>
		<slot name="viewing-mode" />
		<template v-if="showAnchors">
			<a @click="scrollTo('asset-toc-top')">Top</a>
			<template v-for="content in assetContent">
				<a v-if="!isEmpty(content.value)" :key="content.key" @click="scrollTo(content.key)">
					{{ content.key.replace('-', ' ') }}
					<span v-if="Array.isArray(content.value)"> ({{ content.value.length }}) </span>
				</a>
			</template>
		</template>
	</nav>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';

defineProps({
	assetContent: {
		type: Array<{ key: string; value: any }>,
		default: []
	},
	showAnchors: {
		type: Boolean,
		default: true
	}
});

function scrollTo(elementId: string) {
	document.getElementById(elementId)?.scrollIntoView({ behavior: 'smooth' });
}
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding-left: 1rem;
	padding-top: 1rem;
	min-width: 14rem;
	position: sticky;
	top: 0;
}
</style>
