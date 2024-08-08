<template>
	<div class="strata-group" :style="`border-left: 9px solid ${props.config.borderColour}`">
		<div class="input-row">
			<div class="label-and-input">
				<label>Name of strata</label>
				<tera-input-text
					v-model="strataName"
					placeholder="e.g., Age group"
					@focusout="emit('update-self', updatedConfig)"
				/>
			</div>
			<div class="label-and-input">
				<label>Select variables and parameters to stratify</label>
				<MultiSelect
					v-model="selectedVariables"
					:options="props.modelNodeOptions"
					placeholder="Click to select"
					display="chip"
					@update:model-value="emit('update-self', updatedConfig)"
				/>
			</div>
		</div>
		<div class="input-row">
			<div class="label-and-input">
				<label>
					Enter a comma separated list of labels for each group.
					<span class="subdued-text">(Max 100)</span>
				</label>
				<tera-input-text
					v-model="labels"
					placeholder="e.g., Young, Old"
					@focusout="emit('update-self', updatedConfig)"
				/>
			</div>
		</div>
		<div class="input-row justify-space-between">
			<div class="flex align-items-center gap-2">
				<label>Create new transitions between strata</label>
				<InputSwitch @change="emit('update-self', updatedConfig)" v-model="useStructure" />
			</div>
			<div class="flex align-items-center gap-2">
				<label>Allow existing interactions to involve multiple strata</label>
				<InputSwitch @change="emit('update-self', updatedConfig)" v-model="cartesianProduct" />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import MultiSelect from 'primevue/multiselect';
import InputSwitch from 'primevue/inputswitch';
import { StratifyGroup } from '@/components/workflow/ops/stratify-mira/stratify-mira-operation';

const props = defineProps<{
	modelNodeOptions: string[];
	config: StratifyGroup;
}>();

const emit = defineEmits(['update-self']);

const strataName = ref(props.config.name);
const selectedVariables = ref<string[]>(props.config.selectedVariables);
const labels = ref(props.config.groupLabels);
const cartesianProduct = ref<boolean>(props.config.cartesianProduct);
const directed = ref<boolean>(props.config.directed); // Currently not used, assume to be true
const structure = ref<any>(props.config.structure); // Proxied by "useStructure"
const useStructure = ref<any>(props.config.useStructure);

const updatedConfig = computed<StratifyGroup>(() => ({
	borderColour: props.config.borderColour,
	name: strataName.value,
	selectedVariables: selectedVariables.value,
	groupLabels: labels.value,
	cartesianProduct: cartesianProduct.value,
	directed: directed.value,
	structure: structure.value,
	useStructure: useStructure.value
}));

watch(
	() => props.config,
	() => {
		strataName.value = props.config.name;
		selectedVariables.value = props.config.selectedVariables;
		labels.value = props.config.groupLabels;
		cartesianProduct.value = props.config.cartesianProduct;
		structure.value = props.config.structure;
		useStructure.value = props.config.useStructure;
	}
);
</script>

<style scoped>
.strata-group {
	display: flex;
	padding: 1rem 1.5rem;
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

.sub-header {
	display: flex;
	padding-bottom: 0px;
	justify-content: flex-end;
	align-items: center;
	gap: var(--gap);
	align-self: stretch;
}

.subdued-text {
	color: var(--text-color-subdued);
}

.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: var(--gap-small);
	margin-bottom: var(--gap-small);

	& > * {
		flex: 1;
	}
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
	width: 0; /* CSS is weird but for some reason this prevents the Multiselect from going nuts */
}
</style>
