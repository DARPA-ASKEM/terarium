<template>
	<div class="form">
		<div class="label-col">
			<label>Parameter name</label>
			<InputText v-model.lazy="name" @update:model-value="updateIntervention" />
		</div>
		<div class="label-col">
			<label>Value</label>
			<tera-input-number v-model.lazy="timestep" @update:model-value="updateIntervention" />
		</div>
		<div class="label-col">
			<label>Timestep</label>
			<tera-input-number v-model.lazy="value" @update:model-value="updateIntervention" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Intervention } from '@/types/Types';
import teraInputNumber from '@/components/widgets/tera-input-number.vue';
import InputText from 'primevue/inputtext';

const props = defineProps<{
	modelIntervention: Intervention;
}>();
const emit = defineEmits(['update-value']);

const name = ref(props.modelIntervention.name);
const timestep = ref(props.modelIntervention.timestep);
const value = ref(props.modelIntervention.value);

function updateIntervention() {
	const intervention: Intervention = {
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
