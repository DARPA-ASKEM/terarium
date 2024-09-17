<template>
	<section>
		<header class="flex w-full gap-3 mb-2">
			<tera-toggleable-input
				v-model="constraintName"
				tag="h3"
				@update:model-value="emit('update-self', updatedConfig)"
			/>
			<div class="ml-auto flex align-items-center">
				<label class="mr-2">Active</label>
				<InputSwitch class="mr-3" />
				<Button icon="pi pi-trash" text rounded @click="emit('delete-self')" />
			</div>
		</header>
		<p>
			The
			<Dropdown
				:model-value="constraintType"
				:options="Object.values(ConstraintType)"
				@update:model-value="changeConstraintType($event)"
			/>
			<MultiSelect
				v-model="variables"
				placeholder="Select variables"
				:options="ids"
				@update:model-value="updateChanges"
			/>
			should be
			<Dropdown
				:model-value="derivativeType"
				:options="Object.values(DerivativeType)"
				@update:model-value="changeDerivativeType($event)"
			/>
			<tera-input-number
				v-if="derivativeType === DerivativeType.LessThan"
				v-model="upperBound"
				@update:model-value="updateChanges"
			/>
			<tera-input-number
				v-if="derivativeType === DerivativeType.GreaterThan"
				v-model="lowerBound"
				@update:model-value="updateChanges"
			/>
			<template v-if="derivativeType === DerivativeType.LessThan || derivativeType === DerivativeType.GreaterThan">
				persons
			</template>
			<template
				v-if="
					derivativeType === DerivativeType.LessThan ||
					derivativeType === DerivativeType.GreaterThan ||
					derivativeType === DerivativeType.Increasing ||
					derivativeType === DerivativeType.Decreasing ||
					derivativeType === DerivativeType.LinearlyConstrained
				"
			>
				from timepoint
				<tera-input-number v-model="startTime" @update:model-value="updateChanges" />
				day to timepoint
				<tera-input-number v-model="endTime" @update:model-value="updateChanges" />
			</template>
			<!--Wrong variables being mutated-->
			<template v-if="derivativeType === DerivativeType.Following">
				time-series dataset within +/-
				<tera-input-number v-model="startTime" @update:model-value="updateChanges" />
				persons within a time window of
				<tera-input-number v-model="endTime" @update:model-value="updateChanges" />
			</template>
			days.
		</p>
		<!-- Weights -->
		<!-- <ul v-if="weights && !isEmpty(weights)">
			<li v-for="(variable, index) of variables" :key="index">
				<tera-input-number
					:label="variable + ' Weight'"
					:placeholder="variable"
					v-model="weights[index]"
					@update:model-value="updateChanges()"
				/>
			</li>
		</ul> -->
	</section>
</template>

<script setup lang="ts">
// import { isEmpty } from 'lodash';
import { watch, ref, computed } from 'vue';
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import MultiSelect from 'primevue/multiselect';
import Dropdown from 'primevue/dropdown';
import InputSwitch from 'primevue/inputswitch';
import Button from 'primevue/button';
import { ConstraintGroup, ConstraintType, DerivativeType } from '@/components/workflow/ops/funman/funman-operation';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';

const props = defineProps<{
	stateIds: string[];
	parameterIds: string[];
	observableIds: string[];
	config: ConstraintGroup;
}>();

const emit = defineEmits(['delete-self', 'update-self']);

const constraintName = ref(props.config.name);
const constraintType = ref(props.config.constraintType);
const upperBound = ref(props.config.interval?.ub);
const lowerBound = ref(props.config.interval?.lb);
const startTime = ref(props.config.timepoints?.lb);
const endTime = ref(props.config.timepoints?.ub);
const variables = ref(props.config.variables);
const weights = ref(props.config.weights);
const derivativeType = ref(props.config.derivativeType);

const ids = computed(() => {
	switch (constraintType.value) {
		case ConstraintType.State:
			return props.stateIds;
		case ConstraintType.Parameter:
			return props.parameterIds;
		case ConstraintType.Observable:
			return props.observableIds;
		default:
			return [];
	}
});

const updatedConfig = computed<ConstraintGroup>(
	() =>
		({
			borderColour: props.config.borderColour,
			name: constraintName.value,
			variables: variables.value,
			weights: weights.value,
			timepoints: { lb: startTime.value, ub: endTime.value },
			interval: { lb: lowerBound.value, ub: upperBound.value },
			constraintType: constraintType.value,
			derivativeType: derivativeType.value
		}) as ConstraintGroup
);

const updateChanges = () => {
	const wLen = updatedConfig.value.weights?.length ?? 0;
	const vLen = updatedConfig.value.variables.length;
	if (wLen !== vLen) {
		updatedConfig.value.weights = Array<number>(vLen).fill(1.0);
	}
	emit('update-self', updatedConfig.value);
};

// Changing type should wipe out current settings to avoid weird things from happening
const changeConstraintType = (newConstraintType: ConstraintType) => {
	constraintType.value = newConstraintType;
	weights.value = [];
	variables.value = [];
	startTime.value = 0;
	endTime.value = 100;
	lowerBound.value = 0;
	upperBound.value = 1;
	updateChanges();
};

const changeDerivativeType = (newDerivativeType: DerivativeType) => {
	derivativeType.value = newDerivativeType;
	// Mutate the rest of the sentence here
	updateChanges();
};

watch(
	() => variables.value,
	() => {
		if (!weights.value || weights.value.length === 0) {
			weights.value = Array<number>(props.config.variables.length).fill(1);
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: flex;
	padding: var(--gap-4);
	flex-direction: column;
	background: var(--gray-50);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
}

p {
	&:deep(.tera-input) {
		display: inline-flex;
		height: 2.4rem; /* Match height of dropdowns */
	}

	&:deep(input) {
		display: inline-flex;
		width: 4rem;
	}
}

ul {
	list-style-type: none;
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	margin-top: var(--gap-2);

	& > li {
		text-align: right;
	}
}
</style>
