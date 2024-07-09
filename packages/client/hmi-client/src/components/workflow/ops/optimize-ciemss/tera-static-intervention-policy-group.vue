<template>
	<div class="policy-group">
		<div class="form-header">
			<label class="mr-auto" tag="h5"> {{ config.intervention?.name ?? `Intervention` }}</label>
			<div>
				<label for="active">Optimize</label>
				<InputSwitch
					v-model="knobs.isActive"
					:disabled="isNotEditable"
					@change="emit('update-self', knobs)"
				/>
			</div>
		</div>
		<template v-if="knobs.isActive">
			<section class="input-row">
				<p>
					Find the
					<Dropdown
						class="toolbar-button"
						v-model="knobs.optimizationType"
						option-value="value"
						option-label="label"
						:options="OPTIMIZATION_TYPE_MAP"
						@change="emit('update-self', knobs)"
					/>
					for the <strong>{{ knobs.intervention.type }}</strong>
					<strong>{{ knobs.intervention.appliedTo }}</strong
					>.
				</p>
			</section>
			<div>
				<section class="input-row">
					<p>
						The objective is the
						<template v-if="showNewValueOptions">
							<span>value closet to the</span>
							<Dropdown
								class="toolbar-button ml-1 mr-1"
								v-model="knobs.objectiveFunctionOption"
								option-value="value"
								option-label="label"
								:options="OBJECTIVE_FUNCTION_MAP"
								@change="emit('update-self', knobs)"
							/>
						</template>
						<span v-if="showNewValueOptions && showStartTimeOptions">and at the</span>
						<template v-if="showStartTimeOptions">
							<Dropdown
								class="toolbar-button ml-1 mr-1"
								v-model="knobs.objectiveFunctionOption"
								option-value="value"
								option-label="label"
								:options="OBJECTIVE_FUNCTION_MAP"
								@change="emit('update-self', knobs)"
							/>
							<span>start time</span>
						</template>
						<span>.</span>
					</p>
				</section>

				<section v-if="showNewValueOptions">
					<h6 class="pt-4, pb-3">New Value</h6>
					<div class="input-row">
						<div class="label-and-input">
							<label>Lower bound</label>
							<tera-input
								type="number"
								v-model="knobs.lowerBoundValue"
								@update:model-value="$emit('update-self', knobs)"
							/>
						</div>
						<div class="label-and-input">
							<label>Upper bound</label>
							<tera-input
								type="number"
								v-model="knobs.upperBoundValue"
								@update:model-value="emit('update-self', knobs)"
							/>
						</div>
						<div class="label-and-input">
							<label>Initial guess</label>
							<tera-input
								type="number"
								v-model="knobs.initialGuessValue"
								@update:model-value="emit('update-self', knobs)"
							/>
						</div>
					</div>
				</section>
				<section v-if="showStartTimeOptions">
					<h6 class="pt-4, pb-3">Start Time</h6>
					<div class="input-row">
						<div class="label-and-input">
							<label>Start time</label>
							<tera-input
								type="number"
								v-model="knobs.startTime"
								@update:model-value="emit('update-self', knobs)"
							/>
						</div>
						<div class="label-and-input">
							<label>End time</label>
							<tera-input
								type="number"
								v-model="knobs.endTime"
								@update:model-value="emit('update-self', knobs)"
							/>
						</div>
						<div class="label-and-input">
							<label>Initial guess</label>
							<tera-input
								type="number"
								v-model="knobs.initialGuessTime"
								@update:model-value="emit('update-self', knobs)"
							/>
						</div>
					</div>
				</section>
			</div>
		</template>
		<template v-else>
			<p v-for="(staticIntervention, index) in staticInterventions" :key="index">
				Set the <strong>{{ config.intervention?.type }}</strong>
				<strong>{{ config.intervention?.appliedTo }}</strong> to a new value of
				<strong>{{ staticIntervention.value }}</strong> day at start time
				<strong>{{ staticIntervention.timestep }}</strong> day.
			</p>
		</template>
	</div>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import TeraInput from '@/components/widgets/tera-input.vue';
import InputSwitch from 'primevue/inputswitch';
import { computed, ref } from 'vue';
import { StaticIntervention } from '@/types/Types';
import {
	InterventionPolicyGroupForm,
	InterventionTypes,
	OPTIMIZATION_TYPE_MAP,
	OBJECTIVE_FUNCTION_MAP
} from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';

const props = defineProps<{
	config: InterventionPolicyGroupForm;
}>();

const emit = defineEmits(['update-self']);

const staticInterventions = ref<StaticIntervention[]>(
	props.config.intervention.staticInterventions
);

const knobs = ref<InterventionPolicyGroupForm>({
	isActive: props.config.isActive,
	intervention: props.config.intervention,
	optimizationType: props.config.optimizationType,
	objectiveFunctionOption: props.config.objectiveFunctionOption,
	startTime: props.config.startTime,
	endTime: props.config.endTime,
	lowerBoundValue: props.config.lowerBoundValue,
	upperBoundValue: props.config.upperBoundValue,
	initialGuessValue: props.config.initialGuessValue,
	initialGuessTime: props.config.initialGuessTime
});

const isNotEditable = computed(() => {
	if (staticInterventions.value.length === 1) return false;
	return true;
});

const showStartTimeOptions = computed(
	() => knobs.value.optimizationType === InterventionTypes.paramValue
	// TODO https://github.com/DARPA-ASKEM/terarium/issues/3909
	// || knobs.value.optimizationType === InterventionTypes.paramValueAndStartTime
);
const showNewValueOptions = computed(
	() => knobs.value.optimizationType === InterventionTypes.startTime
	// TODO https://github.com/DARPA-ASKEM/terarium/issues/3909
	// || knobs.value.optimizationType === InterventionTypes.paramValueAndStartTime
);
</script>

<style scoped>
.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: var(--gap-2);
	padding-bottom: 0.5rem;

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
	gap: var(--gap-2);
}

.form-header {
	width: 100%;
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	gap: var(--gap-4);
	padding-bottom: 0.5rem;

	& > *:first-child {
		margin-right: auto;
	}

	& > * {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		gap: var(--gap-2);
	}
}

.policy-group {
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	gap: var(--gap-2);
	border-radius: 0.375rem;
	background: #fff;
	border: 1px solid rgba(0, 0, 0, 0.08);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}
</style>
