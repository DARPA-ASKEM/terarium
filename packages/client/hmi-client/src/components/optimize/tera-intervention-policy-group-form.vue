<template>
	<div class="policy-group" :style="`border-left: 9px solid ${props.config.borderColour}`">
		<div class="form-header">
			<div>
				<InputText
					v-if="isEditing"
					v-model="config.name"
					placeholder="Policy bounds"
					@focusout="emit('update-self', config)"
				/>
				<h6 v-else>{{ props.config.name }}</h6>
				<i
					:class="{ 'pi pi-check i': isEditing, 'pi pi-pencil i': !isEditing }"
					:style="'cursor: pointer'"
					@click="onEdit"
				/>
			</div>
			<div>
				<label for="active">Active</label>
				<InputSwitch v-model="config.isActive" @change="emit('update-self', config)" />
			</div>
			<div>
				<i
					class="trash-button pi pi-trash"
					:style="'cursor: pointer'"
					@click="emit('delete-self')"
				/>
			</div>
		</div>
		<div class="input-row">
			<div class="label-and-input">
				<label for="parameter">Parameter</label>
				<Dropdown
					class="p-inputtext-sm"
					:options="props.parameterOptions"
					v-model="config.parameter"
					placeholder="Select"
					@update:model-value="emit('update-self', config)"
				/>
			</div>
			<div class="label-and-input">
				<label for="initial-guess">Initial guess</label>
				<tera-input-number
					class="p-inputtext-sm"
					:min-fraction-digits="1"
					:max-fraction-digits="10"
					v-model="config.initialGuess"
					@update:model-value="emit('update-self', config)"
				/>
			</div>
			<div class="label-and-input"></div>
		</div>
		<div class="input-row">
			<div class="label-and-input">
				<label for="lower-bound">Lower bound</label>
				<tera-input-number
					class="p-inputtext-sm"
					:min-fraction-digits="1"
					:max-fraction-digits="10"
					v-model="config.lowerBound"
					@update:model-value="emit('update-self', config)"
				/>
			</div>
			<div class="label-and-input">
				<label for="upper-bound">Upper bound</label>
				<tera-input-number
					class="p-inputtext-sm"
					:min-fraction-digits="1"
					:max-fraction-digits="10"
					v-model="config.upperBound"
					@update:model-value="emit('update-self', config)"
				/>
			</div>
			<div class="label-and-input">
				<label for="start-time">Start time</label>
				<tera-input-number
					class="p-inputtext-sm"
					v-model="config.startTime"
					@update:model-value="emit('update-self', config)"
				/>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import InputSwitch from 'primevue/inputswitch';
import { InterventionPolicyGroup } from '@/workflow/ops/optimize-ciemss/optimize-ciemss-operation';

const props = defineProps<{
	parameterOptions: string[];
	config: InterventionPolicyGroup;
}>();

const emit = defineEmits(['update-self', 'delete-self']);

const config = ref<InterventionPolicyGroup>(_.cloneDeep(props.config));
const isEditing = ref<boolean>(false);

const onEdit = () => {
	isEditing.value = !isEditing.value;
};
</script>

<style scoped>
.policy-group {
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	gap: 0.5rem;
	border-radius: 0.375rem;
	background: #fff;
	border: 1px solid rgba(0, 0, 0, 0.08);
	/* Shadow/medium */
	box-shadow:
		0px 2px 4px -1px rgba(0, 0, 0, 0.06),
		0px 4px 6px -1px rgba(0, 0, 0, 0.08);
}

.form-header {
	width: 100%;
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	gap: 1rem;
	padding-bottom: 0.5rem;

	& > *:first-child {
		margin-right: auto;
	}

	& > * {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		gap: 0.5rem;
	}
}

.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: 0.5rem;

	& > *:first-child {
		flex: 2;
	}

	& > *:not(:first-child) {
		flex: 1;
	}
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}
</style>
