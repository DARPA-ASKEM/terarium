<template>
	<div v-if="config.isActive">
		<div class="input-row">
			<p>
				Find the
				<Dropdown
					class="toolbar-button"
					v-model="optimizeOption"
					option-label="label"
					option-value="value"
					:options="optimizeOptions"
				/>
				for the parameter <b>{{ config.intervention.appliedTo }}</b
				>.
			</p>
		</div>
		<div>
			<div class="input-row">
				<p>
					The objective is the
					<span v-if="showNewValueOptions">value closet to the</span>
					<Dropdown
						v-if="showNewValueOptions"
						class="toolbar-button ml-1 mr-1"
						v-model="newValueOption"
						optionLabel="label"
						optionValue="value"
						:options="objectiveOptions"
						@update:model-value="emit('update-self', config)"
					/>
					<span v-if="showNewValueOptions && showStartTimeOptions">and at the</span>
					<Dropdown
						v-if="showStartTimeOptions"
						class="toolbar-button ml-1 mr-1"
						v-model="startTimeOption"
						optionLabel="timeLabel"
						optionValue="value"
						:options="objectiveOptions"
						@update:model-value="emit('update-self', config)"
					/>
					<span v-if="showStartTimeOptions">start time</span>
					<span>.</span>
				</p>
			</div>

			<div v-if="showNewValueOptions">
				<h6 class="pt-4, pb-3">New Value</h6>
				<div class="input-row">
					<div v-for="objective in objectiveOptions" :key="objective.value" class="label-and-input">
						<div class="label-and-input">
							<label :for="objective.value">{{ objective.label }}</label>
							<InputNumber
								type="number"
								v-model="newValueObjectives[objective.value]"
								@update:model-value="emit('update-self', config)"
							/>
						</div>
					</div>
				</div>
			</div>

			<div v-if="showStartTimeOptions">
				<h6 class="pt-4, pb-3">Start Time</h6>
				<div class="input-row">
					<div v-for="objective in objectiveOptions" :key="objective.value" class="label-and-input">
						<div class="label-and-input">
							<label :for="objective.value">{{ objective.label }}</label>
							<InputNumber
								type="number"
								v-model="startTimeObjectives[objective.value]"
								@update:model-value="emit('update-self', config)"
							/>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div v-else>
		<div v-for="(staticIntervention, index) in staticInterventions" :key="index">
			<p>
				Set the parameter <b>{{ config.intervention.appliedTo }}</b> to a new value of
				<b>{{ staticIntervention.value }}</b> day at start time
				<b>{{ staticIntervention.threshold }}</b> day.
			</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import { computed, ref } from 'vue';
import { StaticIntervention } from '@/types/Types';

const props = defineProps<{
	config: any;
	knobs: any;
	staticInterventions: StaticIntervention[];
}>();

const startTime = ref(props.config.startTime);
const endTime = ref(props.knobs.endTime);
const initialGuess = ref(props.staticInterventions[0]?.value);

const emit = defineEmits(['update-self', 'delete-self']);

const LOWER_BOUND = 'LOWER_BOUND';
const UPPER_BOUND = 'UPPER_BOUND';
const INITIAL_GUESS = 'INITIAL_GUESS';
const VALUE = 'VALUE';
const START_TIME = 'START_TIME';
const VALUE_START_TIME = 'VALUE_START_TIME';

const OPTIONS = {
	[LOWER_BOUND]: 'Lower bound',
	[UPPER_BOUND]: 'Upper bound',
	[INITIAL_GUESS]: 'Initial guess',
	[VALUE]: 'new value',
	[START_TIME]: 'new start time',
	[VALUE_START_TIME]: 'new value and start time'
};

const optimizeOption = ref(VALUE);
const startTimeOption = ref(LOWER_BOUND);
const newValueOption = ref(INITIAL_GUESS);

const startTimeObjectives = ref({
	[LOWER_BOUND]: startTime.value,
	[UPPER_BOUND]: endTime.value,
	[INITIAL_GUESS]: initialGuess.value
});

const newValueObjectives = ref({
	[LOWER_BOUND]: 0,
	[UPPER_BOUND]: 0,
	[INITIAL_GUESS]: initialGuess.value
});

const optimizeOptions = [
	{ label: OPTIONS[VALUE], value: VALUE },
	{ label: OPTIONS[START_TIME], value: START_TIME },
	{ label: OPTIONS[VALUE_START_TIME], value: VALUE_START_TIME }
];

const objectiveOptions = [
	{ label: OPTIONS[LOWER_BOUND], timeLabel: 'earliest', value: LOWER_BOUND },
	{ label: OPTIONS[UPPER_BOUND], timeLabel: 'latest', value: UPPER_BOUND },
	{ label: OPTIONS[INITIAL_GUESS], timeLabel: OPTIONS[INITIAL_GUESS], value: INITIAL_GUESS }
];

const showStartTimeOptions = computed(
	() => optimizeOption.value === START_TIME || optimizeOption.value === VALUE_START_TIME
);
const showNewValueOptions = computed(
	() => optimizeOption.value === VALUE || optimizeOption.value === VALUE_START_TIME
);
</script>

<style scoped>
.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: 0.5rem;
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
	gap: 0.5rem;
}
</style>
