<template>
	<div class="constraint-group">
		<header>
			<label>Compartmental constraint</label>
			<label class="mr-2">Active</label>
			<InputSwitch class="mr-3" />
		</header>
		<div class="section-row" v-if="props.variables">
			<div>{{ props.variables.join(' + ') }} = {{ props.mass }}</div>
			<span v-for="v of props.variables" :key="v"> {{ v }} &#8805; 0, </span>
		</div>
	</div>
</template>

<script setup lang="ts">
// import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import InputSwitch from 'primevue/inputswitch';

const props = defineProps<{
	variables: string[];
	mass: string;
}>();

// const emit = defineEmits(['delete-self', 'update-self']);
</script>

<style scoped>
.constraint-group {
	width: 100%;
	display: flex;
	padding: var(--gap-2) var(--gap-4);
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	background: var(--gray-50);
	border: 1px solid var(--surface-border-light);
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
