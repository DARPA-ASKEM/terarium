<template>
	<template v-if="config.isActive">
		<section class="input-row">
			<p>
				Find the
				<Dropdown
					class="toolbar-button"
					v-model="knobs.optimizationType"
					:options="OPTIMIZATION_TYPES"
				/>
				for the <strong>{{ config.intervention?.type }}</strong>
				<strong>{{ config.intervention?.appliedTo }}</strong
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
							v-model="knobs.newValueOption"
							:options="NEW_VALUE_OPTIONS"
						/>
					</template>
					<span v-if="showNewValueOptions && showStartTimeOptions">and at the</span>
					<template v-if="showStartTimeOptions">
						<Dropdown
							class="toolbar-button ml-1 mr-1"
							v-model="knobs.startTimeOption"
							:options="START_TIME_OPTIONS"
						/>
						<span>start time</span>
					</template>
					<span>.</span>
				</p>
			</section>

			<section v-if="showNewValueOptions">
				<h6 class="pt-4, pb-3">New Value</h6>
				<div class="input-row">
					<div v-for="(objective, index) in newValueInputs" :key="index" class="label-and-input">
						<div class="label-and-input">
							<label :for="objective">{{ NEW_VALUE_OPTIONS[index] }}</label>
							<tera-input type="number" v-model="knobs[objective]" />
						</div>
					</div>
				</div>
			</section>

			<section v-if="showStartTimeOptions">
				<h6 class="pt-4, pb-3">Start Time</h6>
				<div class="input-row">
					<div
						v-for="(objective, index) in newStartTimeInputs"
						:key="index"
						class="label-and-input"
					>
						<div class="label-and-input">
							<label :for="objective">{{ NEW_VALUE_OPTIONS[index] }}</label>
							<tera-input type="number" v-model="knobs[objective]" />
						</div>
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
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import TeraInput from '@/components/widgets/tera-input.vue';
import { computed, ref } from 'vue';
import { StaticIntervention } from '@/types/Types';
import { InterventionPolicyGroup } from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';

const props = defineProps<{
	config: InterventionPolicyGroup;
	staticInterventions: StaticIntervention[];
}>();

const knobs = ref({
	optimizationType: props.config.optimizationType ?? 'new value',
	newValueOption: props.config.newValueOption ?? 'initial guess',
	startTimeOption: props.config.startTimeOption ?? 'earliest',
	startTime: props.config.startTime ?? 0,
	endTime: props.config.endTime ?? 0,
	lowerBound: props.config.lowerBound ?? 0,
	upperBound: props.config.upperBound ?? 0,
	startTimeGuess: props.config.startTimeGuess ?? 1,
	initialGuess: props.config.initialGuess ?? 0
});

// const emit = defineEmits(['update-self']);

const OPTIMIZATION_TYPES = ['new value', 'start time', 'new value and start time'];
const NEW_VALUE_OPTIONS = ['lower bound', 'upper bound', 'initial guess'];
const START_TIME_OPTIONS = ['earliest', 'latest', 'inital guess'];

const newValueInputs = ['lowerBound', 'upperBound', 'initialGuess'];
const newStartTimeInputs = ['startTime', 'endTime', 'startTimeGuess'];

const showStartTimeOptions = computed(
	() =>
		knobs.value.optimizationType === OPTIMIZATION_TYPES[1] ||
		knobs.value.optimizationType === OPTIMIZATION_TYPES[2]
);
const showNewValueOptions = computed(
	() =>
		knobs.value.optimizationType === OPTIMIZATION_TYPES[0] ||
		knobs.value.optimizationType === OPTIMIZATION_TYPES[2]
);

// TODO: Fix this
// const upstateState = () => {
// 	emit('update-self', configIntervention);
// };
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
</style>
