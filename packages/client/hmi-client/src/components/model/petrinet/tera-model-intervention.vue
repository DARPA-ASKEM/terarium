<template>
	<div class="form">
		<div class="label-col">
			<label>Parameter name </label>
			<Dropdown
				:options="parameterOptions"
				v-model.lazy="name"
				@update:model-value="updateIntervention"
			/>
		</div>
		<div class="label-col">
			<label>Timestep</label>
			<tera-input-number v-model.lazy="timestep" @update:model-value="updateIntervention" />
		</div>
		<div class="label-col">
			<label>Value</label>
			<tera-input-number v-model.lazy="value" @update:model-value="updateIntervention" />
		</div>
		<Button label="Delete" icon="pi pi-trash" @click="$emit('delete')" rounded text />
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Intervention } from '@/types/Types';
import teraInputNumber from '@/components/widgets/tera-input-number.vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';

const props = defineProps<{
	intervention: Intervention;
	parameterOptions: string[];
}>();
const emit = defineEmits(['update-value', 'delete']);

const name = ref(props.intervention.name);
const timestep = ref(props.intervention.timestep);
const value = ref(props.intervention.value);

function updateIntervention() {
	const intervention: Intervention = {
		id: props.intervention.id,
		name: name.value,
		timestep: timestep.value,
		value: value.value
	};
	emit('update-value', intervention);
}
</script>

<style scoped>
.form {
	display: flex;
}

.label-col {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}
</style>
