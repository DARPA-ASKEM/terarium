<template>
	<div class="strata-group" style="{border-left: 9px solid {{ props.config.borderColour }};}">
		<!-- TOM TODO ^ make colouring border read from props properly -->
		<div class="sub-header">
			<label for="strata-name">Cartesian product</label>
			<InputSwitch v-model="cartesianProduct" />
			<i class="pi pi-trash" />
		</div>
		<div class="first-row">
			<div class="age-group">
				<label for="strata-name">Name of strata</label>
				<InputText id="strata-name" v-model="strataName" placeholder="Age group" />
			</div>
			<div class="select-variables">
				<label for="variables-select">Select variables and parameters to stratify</label>
				<MultiSelect
					id="variables-select"
					v-model="selectedVariables"
					:options="props.modelStates"
					placeholder="Model states"
					display="chip"
				></MultiSelect>
			</div>
		</div>
		<div class="flex flex-column">
			<label for="group-labels"
				>Enter a comma separated list of labels for each group.
				<span class="subdued-text">(Max 100)</span></label
			>
			<InputText id="group-labels" v-model="labels" placeholder="Young, Old" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InputText from 'primevue/inputtext';
import MultiSelect from 'primevue/multiselect';
import InputSwitch from 'primevue/inputswitch';
import { StratifyGroup } from '@/workflow/ops/stratify-mira/stratify-mira-operation';

const props = defineProps<{
	modelStates: string[];
	config: StratifyGroup;
}>();

const strataName = ref('');
const selectedVariables = ref<string[]>([]);
const labels = ref('');
const cartesianProduct = ref<boolean>(true);
</script>

<style>
.strata-group {
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	border-radius: 0.375rem;
	background: #fff;
	border-left: 8px solid #00c387;
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
