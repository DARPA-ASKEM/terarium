<template>
	<nav>
		<slot name="viewing-mode" />
		<template v-if="showHeaderLinks">
			<a @click="scrollTo('asset-top')">Top</a>
			<template v-for="content in assetContent">
				<a v-if="!isEmpty(content.value)" :key="content.key" @click="scrollTo(content.key)">
					{{ content.key.replace('-', ' ') }}
					<span v-if="Array.isArray(content.value)"> ({{ content.value.length }}) </span>
				</a>
			</template>
			<slot name="page-search" />
		</template>
	</nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { isEmpty } from 'lodash';

const props = defineProps({
	assetContent: {
		type: Array<{ key: string; value: any }>,
		default: []
	},
	showHeaderLinks: {
		type: Boolean,
		default: true
	}
});

const navSpanStyle = computed(() => (props.showHeaderLinks ? '1 / span 2' : '1 / span 1'));

function scrollTo(elementId: string) {
	document.getElementById(elementId)?.scrollIntoView({ behavior: 'smooth' });
}
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	width: 14rem;
	gap: 1rem;
	padding-left: 1rem;
	padding-top: 1rem;
	margin-right: 0.5rem;
	grid-row: v-bind(navSpanStyle);
	/* Responsible for stickiness */
	position: sticky;
	top: 0;
	height: fit-content;
}
</style>
