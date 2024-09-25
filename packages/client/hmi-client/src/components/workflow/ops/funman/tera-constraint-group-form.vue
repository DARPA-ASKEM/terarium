<template>
	<section>
		<i class="ml-2" v-if="config.constraintType === ConstraintType.Following">
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
						// Lock if constraintType is Following
						config.constraintType === ConstraintType.Following
					"
					@update:model-value="emit('update-self', { key: 'isActive', value: $event })"
				/>
				<Button icon="pi pi-trash" text rounded @click="emit('delete-self')" />
			</div>
		</header>
		<p>
			The
			<Dropdown
				:model-value="config.constraint"
				:options="Object.values(Constraint)"
				@update:model-value="emit('update-self', { key: 'constraint', value: $event })"
			/>
			<MultiSelect
				:model-value="config.variables"
				placeholder="Select variables"
				:options="variableOptions"
				@update:model-value="emit('update-self', { key: 'variables', value: $event })"
			/>
			should be
			<Dropdown
				:model-value="config.constraintType"
				:options="constraintTypeOptions"
				@update:model-value="
					($event: ConstraintType) => {
						// Disable isActive if constraintType is Following
						if ($event === ConstraintType.Following) {
							emit('update-self', { key: 'isActive', value: false });
						}
						emit('update-self', { key: 'constraintType', value: $event });
					}
				"
			/>
			<tera-input-number
				v-if="
					config.constraintType === ConstraintType.LessThan ||
					config.constraintType === ConstraintType.LessThanOrEqualTo
				"
				auto-width
				:model-value="config.interval.ub"
				@update:model-value="emit('update-self', { key: 'interval', value: { lb: config.interval.lb, ub: $event } })"
			/>
			<tera-input-number
				v-else-if="
					config.constraintType === ConstraintType.GreaterThan ||
					config.constraintType === ConstraintType.GreaterThanOrEqualTo
				"
				auto-width
				:model-value="config.interval.lb"
				@update:model-value="emit('update-self', { key: 'interval', value: { lb: $event, ub: config.interval.ub } })"
			/>
			<template
				v-if="
					config.constraintType === ConstraintType.LessThan ||
					config.constraintType === ConstraintType.LessThanOrEqualTo ||
					config.constraintType === ConstraintType.GreaterThan ||
					config.constraintType === ConstraintType.GreaterThanOrEqualTo
				"
			>
				persons
			</template>
			<template v-if="config.constraintType !== ConstraintType.Following">
				from timepoint
				<tera-input-number
					auto-width
					:model-value="config.timepoints.lb"
					@update:model-value="
						emit('update-self', { key: 'timepoints', value: { lb: $event, ub: config.timepoints.ub } })
					"
				/>
				day to timepoint
				<tera-input-number
					auto-width
					:model-value="config.timepoints.ub"
					@update:model-value="
						emit('update-self', { key: 'timepoints', value: { lb: config.timepoints.lb, ub: $event } })
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
					:model-value="config.timepoints.ub"
					@update:model-value="
						emit('update-self', { key: 'timepoints', value: { lb: config.timepoints.lb, ub: $event } })
					"
				/>
			</template>
			<!--TODO: should be based on time variable in model semantics-->
			days.
		</p>
		<!--TODO: Add dataset column -> variable mapping for following option-->
		<div
			v-if="config.constraintType === ConstraintType.LinearlyConstrained"
			class="flex flex-wrap align-items-center gap-2 mt-3"
		>
			<tera-input-number
				auto-width
				:model-value="config.interval.lb"
				@update:model-value="emit('update-self', { key: 'interval', value: { lb: $event, ub: config.interval.ub } })"
			/>
			<katex-element :expression="stringToLatexExpression(`\\leq [`)" />
			<template v-for="(variable, index) in config.variables" :key="index">
				<tera-input-number
					auto-width
					:model-value="config.weights[index]"
					@update:model-value="
						($event) => {
							const newWeights = cloneDeep(config.weights);
							if (isNaN($event)) return; // Don't accept empty value
							newWeights[index] = $event;
							emit('update-self', { key: 'weights', value: newWeights });
						}
					"
				/>
				<katex-element
					:expression="stringToLatexExpression(`${variable} ${index === config.variables.length - 1 ? '' : '\\ +'}`)"
				/>
			</template>
			<katex-element :expression="stringToLatexExpression(`] \\leq`)" />
			<tera-input-number
				auto-width
				:model-value="config.interval.ub"
				@update:model-value="emit('update-self', { key: 'interval', value: { lb: config.interval.lb, ub: $event } })"
			/>
			<katex-element
				:expression="stringToLatexExpression(`\\forall \\ t \\in [${config.timepoints.lb}, ${config.timepoints.ub}]`)"
			/>
		</div>
		<katex-element
			class="mt-3"
			v-else-if="config.constraintType !== ConstraintType.Following"
			:expression="stringToLatexExpression(generateExpression(config))"
		/>
	</section>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { computed } from 'vue';
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import MultiSelect from 'primevue/multiselect';
import Dropdown from 'primevue/dropdown';
import InputSwitch from 'primevue/inputswitch';
import Button from 'primevue/button';
import { ConstraintGroup, Constraint, ConstraintType } from '@/components/workflow/ops/funman/funman-operation';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { stringToLatexExpression } from '@/services/model';
import { generateExpression } from '@/services/models/funman-service';

const props = defineProps<{
	stateIds: string[];
	parameterIds: string[];
	observableIds: string[];
	config: ConstraintGroup;
}>();

const emit = defineEmits(['delete-self', 'update-self']);

// Not supporting GreaterThan
const constraintTypeOptions = [
	ConstraintType.LessThan,
	ConstraintType.LessThanOrEqualTo,
	ConstraintType.GreaterThanOrEqualTo,
	ConstraintType.Increasing,
	ConstraintType.Decreasing,
	ConstraintType.LinearlyConstrained,
	ConstraintType.Following
];

const variableOptions = computed(() => {
	switch (props.config.constraint) {
		case Constraint.State:
			return props.stateIds;
		case Constraint.Parameter:
			return props.parameterIds;
		case Constraint.Observable:
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
