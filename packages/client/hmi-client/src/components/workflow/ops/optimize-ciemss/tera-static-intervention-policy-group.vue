<template>
	<div class="policy-group">
		<div class="form-header">
			<h6 class="mr-auto">{{ config.interventionName ?? `Intervention` }}</h6>
			<tera-signal-bars
				v-model="knobs.relativeImportance"
				@update:model-value="emit('update-self', knobs)"
				label="Relative importance"
			/>
		</div>
		<template v-if="knobs.relativeImportance">
			<section class="input-row">
				<p>
					Find the
					<Dropdown
						class="toolbar-button"
						v-model="knobs.optimizeFunction.type"
						option-value="value"
						option-label="label"
						:options="OPTIMIZATION_TYPE_MAP"
						@change="emit('update-self', knobs)"
					/>
					for the {{ knobs.individualIntervention.type }}&nbsp;<strong>{{
						knobs.individualIntervention.appliedTo
					}}</strong>
				</p>
				<p v-if="isOptimizationTypeParamValue">
					at the start time <strong>{{ knobs.individualIntervention.timestep }}</strong>
				</p>
				<p v-else-if="isOptimizationTypeStartTime">
					when the value is
					<strong
						>{{ knobs.individualIntervention.value
						}}{{ knobs.individualIntervention.valueType === InterventionValueType.Percentage ? '%' : '' }}</strong
					>
				</p>
			</section>
			<div>
				<section class="input-row">
					<p>
						The objective is the
						<template v-if="showNewValueOptions">
							<span>value closest to the</span>
							<Dropdown
								class="toolbar-button ml-1 mr-1"
								v-model="knobs.optimizeFunction.parameterObjectiveFunction"
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
								v-model="knobs.optimizeFunction.timeObjectiveFunction"
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
					<h6 class="pt-4, pb-3">Intervention Value</h6>
					<div class="input-row">
						<tera-input-number
							label="Lower bound"
							v-model="knobs.lowerBoundValue"
							@update:model-value="$emit('update-self', knobs)"
						/>
						<tera-input-number
							label="Upper bound"
							v-model="knobs.upperBoundValue"
							@update:model-value="emit('update-self', knobs)"
						/>
						<tera-input-number
							label="Initial guess"
							v-model="knobs.initialGuessValue"
							@update:model-value="emit('update-self', knobs)"
						/>
					</div>
				</section>
				<section v-if="showStartTimeOptions">
					<h6 class="pt-4, pb-3">Intervention Time</h6>
					<div class="input-row">
						<tera-timestep-calendar
							v-if="modelConfiguration"
							label="Start time"
							:start-date="modelConfiguration.temporalContext"
							:calendar-settings="calendarSettings"
							v-model="knobs.startTime"
							@update:model-value="$emit('update-self', knobs)"
						/>
						<tera-timestep-calendar
							v-if="modelConfiguration"
							label="End time"
							:start-date="modelConfiguration.temporalContext"
							:calendar-settings="calendarSettings"
							v-model="knobs.endTime"
							@update:model-value="$emit('update-self', knobs)"
						/>
						<tera-timestep-calendar
							v-if="modelConfiguration"
							label="Initial guess"
							:start-date="modelConfiguration.temporalContext"
							:calendar-settings="calendarSettings"
							v-model="knobs.startTimeGuess"
							@update:model-value="$emit('update-self', knobs)"
						/>
					</div>
				</section>
			</div>
		</template>
		<template v-else>
			<ul>
				<li class="list-position-inside">
					Set the <strong>{{ knobs.individualIntervention.type }}</strong>
					<strong>{{ knobs.individualIntervention.appliedTo }}</strong> to the value of
					<strong
						>{{ knobs.individualIntervention.value
						}}{{ knobs.individualIntervention.valueType === InterventionValueType.Percentage ? '%' : '' }}</strong
					>
					day at start time
					<strong>{{
						getTimePointString(knobs.individualIntervention.timestep, {
							startDate: modelConfiguration.temporalContext,
							calendarSettings
						})
					}}</strong>
				</li>
			</ul>
		</template>
	</div>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { computed, ref, watch } from 'vue';
import { InterventionValueType, Model, ModelConfiguration, StaticIntervention } from '@/types/Types';
import {
	OptimizationInterventionObjective,
	OPTIMIZATION_TYPE_MAP,
	OBJECTIVE_FUNCTION_MAP,
	InterventionPolicyGroupForm
} from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';
import TeraSignalBars from '@/components/widgets/tera-signal-bars.vue';
import teraTimestepCalendar from '@/components/widgets/tera-timestep-calendar.vue';
import { getTimePointString } from '@/utils/date';
import { getCalendarSettingsFromModel } from '@/services/model';

const props = defineProps<{
	model: Model;
	modelConfiguration: ModelConfiguration;
	config: StaticInterventionPolicyGroupForm;
}>();

export interface StaticInterventionPolicyGroupForm extends InterventionPolicyGroupForm {
	individualIntervention: StaticIntervention;
}

const calendarSettings = getCalendarSettingsFromModel(props.model);

const emit = defineEmits(['update-self']);

const knobs = ref<StaticInterventionPolicyGroupForm>({
	...props.config
});

const isOptimizationTypeStartTime = computed(
	() => knobs.value.optimizeFunction.type === OptimizationInterventionObjective.startTime
);
const isOptimizationTypeParamValue = computed(
	() => knobs.value.optimizeFunction.type === OptimizationInterventionObjective.paramValue
);
const isOptimizationTypeParamValueAndStartTime = computed(
	() => knobs.value.optimizeFunction.type === OptimizationInterventionObjective.paramValueAndStartTime
);
const showStartTimeOptions = computed(
	() => isOptimizationTypeStartTime.value || isOptimizationTypeParamValueAndStartTime.value
);
const showNewValueOptions = computed(
	() => isOptimizationTypeParamValue.value || isOptimizationTypeParamValueAndStartTime.value
);

watch(
	() => props.config,
	() => {
		knobs.value = { ...props.config };
	}
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
	padding: var(--gap-4);
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	gap: var(--gap-2);
	border-radius: var(--gap-1-5);
	background: var(--surface-section);
	border: 1px solid var(--surface-border-light);
	margin: var(--gap-1) 0;
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}

.policy-group + .policy-group {
	margin-top: var(--gap-2);
}

.list-position-inside {
	list-style-position: outside;
	margin-left: var(--gap-4);
	padding-bottom: var(--gap-1);
}
</style>
