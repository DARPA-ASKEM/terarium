<template>
	<div class="strata-group" :style="`border-left: 9px solid ${props.config.borderColour}`">
		<div class="sub-header">
			<label for="strata-name">Cartesian product</label>
			<InputSwitch @change="emit('update-self', updatedConfig)" v-model="cartesianProduct" />
		</div>
		<div class="first-row">
			<div class="age-group">
				<label for="strata-name">Name of strata</label>
				<InputText
					v-model="strataName"
					placeholder="Age group"
					@focusout="emit('update-self', updatedConfig)"
				/>
			</div>
			<div class="select-variables">
				<label for="variables-select">Select variables and parameters to stratify</label>
				<MultiSelect
					v-model="selectedVariables"
					:options="props.modelNodeOptions"
					placeholder="Model states"
					display="chip"
					@update:model-value="emit('update-self', updatedConfig)"
				></MultiSelect>
			</div>
		</div>
		<div class="flex flex-column">
			<label for="group-labels"
				>Enter a comma separated list of labels for each group.
				<span class="subdued-text">(Max 100)</span></label
			>
			<InputText
				v-model="labels"
				placeholder="Young, Old"
				@focusout="emit('update-self', updatedConfig)"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import InputText from 'primevue/inputtext';
import MultiSelect from 'primevue/multiselect';
import InputSwitch from 'primevue/inputswitch';
import { StratifyGroup } from '@/workflow/ops/stratify-mira/stratify-mira-operation';

const props = defineProps<{
	modelNodeOptions: string[];
	config: StratifyGroup;
}>();

const emit = defineEmits(['update-self']);

const strataName = ref(props.config.name);
const selectedVariables = ref<string[]>(props.config.selectedVariables);
const labels = ref(props.config.groupLabels);
const cartesianProduct = ref<boolean>(props.config.cartesianProduct);

const updatedConfig = computed<StratifyGroup>(() => ({
	borderColour: props.config.borderColour,
	name: strataName.value,
	selectedVariables: selectedVariables.value,
	groupLabels: labels.value,
	cartesianProduct: cartesianProduct.value
}));

watch(
	() => props.config,
	() => {
		strataName.value = props.config.name;
		selectedVariables.value = props.config.selectedVariables;
		labels.value = props.config.groupLabels;
		cartesianProduct.value = props.config.cartesianProduct;
	}
);
</script>

<style scoped>
.strata-group {
	margin: 1rem;
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	border-radius: 0.375rem;
	background: #fff;
	/* Shadow/medium */
	box-shadow: 0px 2px 4px -1px rgba(0, 0, 0, 0.06), 0px 4px 6px -1px rgba(0, 0, 0, 0.08);
}

.sub-header {
	display: flex;
	padding-bottom: 0px;
	justify-content: flex-end;
	align-items: center;
	gap: 1rem;
	align-self: stretch;
}

.first-row {
	display: flex;
}
.age-group {
	display: flex;
	flex-direction: column;
	padding-right: 2rem;
	padding-bottom: 1rem;
}

.select-variables {
	display: flex;
	flex-direction: column;
}

.subdued-text {
	color: var(--text-color-subdued);
}
</style>
