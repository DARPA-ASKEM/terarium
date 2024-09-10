<template>
	<div class="constraint-group">
		<div class="section-row">
			<label>Compartmental constraint</label>
		</div>
		<div class="section-row" v-if="props.variables">
			<div>{{ props.variables.join(' + ') }} = {{ props.mass }}</div>
			<span v-for="v of props.variables" :key="v"> {{ v }} &#8805; 0, </span>
		</div>
	</div>
</template>

<script setup lang="ts">
const props = defineProps<{
	variables: string[];
	mass: string;
}>();

// const emit = defineEmits(['delete-self', 'update-self']);
</script>

<style scoped>
.constraint-group {
	width: 100%;
	margin-top: 1rem;
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	background: var(--gray-50);
	border: 1px solid var(--gray-200);
	border-radius: var(--border-radius);
	overflow: hidden;
}

.section-row {
	display: flex;
	/* flex-direction: column; */
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.5rem;
	align-self: stretch;
	flex-wrap: wrap;
}
</style>
