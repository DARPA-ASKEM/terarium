<template>
	<section>
		<i class="ml-2" v-if="config.derivativeType === DerivativeType.Following">
			"following" option is not supported yet
		</i>
		<header class="flex w-full gap-3 mb-2">
			<tera-toggleable-input
				:model-value="config.name"
				tag="h3"
				@update:model-value="emit('update-self', { key: 'name', value: $event })"
			/>
			<div class="ml-auto flex align-items-center">
				<label class="mr-2">Active</label>
				<InputSwitch
					class="mr-3"
					:model-value="config.isActive"
					:disabled="
						// Lock if derivativeType is Following
						config.derivativeType === DerivativeType.Following
					"
					@update:model-value="emit('update-self', { key: 'isActive', value: $event })"
				/>
				<Button icon="pi pi-trash" text rounded @click="emit('delete-self')" />
			</div>
		</header>
		<p>
			The
			<Dropdown
				:model-value="config.constraintType"
				:options="Object.values(ConstraintType)"
				@update:model-value="emit('update-self', { key: 'constraintType', value: $event })"
			/>
			<MultiSelect
				:model-value="config.variables"
				placeholder="Select variables"
				:options="variableOptions"
				@update:model-value="emit('update-self', { key: 'variables', value: $event })"
			/>
			should be
			<Dropdown
				:model-value="config.derivativeType"
				:options="Object.values(DerivativeType)"
				@update:model-value="
					($event: DerivativeType) => {
						// Disable isActive if derivativeType is Following
						if ($event === DerivativeType.Following) {
							emit('update-self', { key: 'isActive', value: false });
						}
						emit('update-self', { key: 'derivativeType', value: $event });
					}
				"
			/>
			<tera-input-number
				v-if="config.derivativeType === DerivativeType.LessThan"
				auto-width
				:model-value="config.interval?.ub ?? 0"
				@update:model-value="emit('update-self', { key: 'interval', value: { lb: config.interval?.lb, ub: $event } })"
			/>
			<tera-input-number
				v-if="config.derivativeType === DerivativeType.GreaterThan"
				auto-width
				:model-value="config.interval?.lb ?? 0"
				@update:model-value="emit('update-self', { key: 'interval', value: { lb: $event, ub: config.interval?.ub } })"
			/>
			<template
				v-if="config.derivativeType === DerivativeType.LessThan || config.derivativeType === DerivativeType.GreaterThan"
			>
				persons
			</template>
			<template v-if="config.derivativeType !== DerivativeType.Following">
				from timepoint
				<tera-input-number
					auto-width
					:model-value="config.timepoints?.lb ?? 0"
					@update:model-value="
						emit('update-self', { key: 'timepoints', value: { lb: $event, ub: config.timepoints?.ub } })
					"
				/>
				day to timepoint
				<tera-input-number
					auto-width
					:model-value="config.timepoints?.ub ?? 100"
					@update:model-value="
						emit('update-self', { key: 'timepoints', value: { lb: config.timepoints?.lb, ub: $event } })
					"
				/>
			</template>
			<!--Wrong variables being mutated-->
			<template v-else>
				time-series dataset within +/-
				<tera-input-number auto-width :model-value="0" />
				persons within a time window of
				<tera-input-number
					auto-width
					:model-value="config.timepoints?.ub ?? 100"
					@update:model-value="
						emit('update-self', { key: 'timepoints', value: { lb: config.timepoints?.lb, ub: $event } })
					"
				/>
			</template>
			days.
		</p>
		<ul v-if="config.weights && !isEmpty(config.weights)">
			<li v-for="(variable, index) of config.variables" :key="index">
				<tera-input-number
					:label="variable + ' Weight'"
					:placeholder="variable"
					:model-value="config.weights[index]"
					@update:model-value="
						($event) => {
							const newWeights = cloneDeep(config.weights);
							if (!newWeights) return;
							newWeights[index] = $event;
							emit('update-self', { key: 'weights', value: newWeights });
						}
					"
				/>
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { computed } from 'vue';
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

const variableOptions = computed(() => {
	switch (props.config.constraintType) {
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
	display: flex;
	flex-wrap: wrap;
	gap: var(--gap-1);
	align-items: center;

	&:deep(.tera-input) {
		display: inline-flex;
		height: 2.4rem; /* Match height of dropdowns */
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
