<template>
	<p v-for="extraction in extractions" :key="extraction.payload.id.id">
		<template v-if="extraction.payload?.names">
			<em>Name</em>
			<span>{{ extraction.payload.names.map((n) => n.name).join(', ') }}</span>
		</template>
		<template v-if="extraction.payload?.descriptions">
			<em>Descriptions</em>
			{{ extraction.payload.descriptions }}
		</template>
		<template v-if="extraction.payload?.groundings">
			<em>Concept</em>
			<a
				v-for="(grounding, index) in extraction.payload.groundings"
				:key="index"
				target="_blank"
				rel="noopener noreferrer"
				:href="`http://34.230.33.149:8772/${grounding.grounding_id}`"
			>
				{{ grounding.grounding_text }}
			</a>
		</template>
		<template v-if="extraction.payload?.value_specs">
			<em>Values</em>
			<span v-for="value in extraction.payload.value_specs" :key="value.id.id">
				{{ value }}
			</span>
		</template>
	</p>
</template>

<script setup lang="ts">
defineProps({
	extractions: {
		type: Array<any>,
		required: true
	}
});
</script>

<style scoped>
p {
	margin: 1rem;
}

em {
	display: inline-block;
	font-weight: var(--font-weight-semibold);
}

em + * {
	margin: 0 0.5rem;
}
</style>
