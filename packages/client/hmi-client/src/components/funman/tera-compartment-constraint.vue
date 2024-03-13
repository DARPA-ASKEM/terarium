<template>
	<div class="constraint-group" :style="`border-left: 9px solid black`">
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
	border-radius: 0.375rem;
	background: #fff;
	border: 1px solid rgba(0, 0, 0, 0.08);
	/* Shadow/medium */
	box-shadow:
		0px 2px 4px -1px rgba(0, 0, 0, 0.06),
		0px 4px 6px -1px rgba(0, 0, 0, 0.08);
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
