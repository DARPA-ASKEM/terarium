<template>
	<div v-if="config.isActive">
		<div class="input-row">
			<p>
				Find the
				<Dropdown
					class="toolbar-button"
					v-model="knobs.optimizationType"
					:options="OPTIMIZATION_TYPES"
				/>
				for the <b>{{ config.intervention?.type }}</b> <b>{{ config.intervention?.appliedTo }}</b
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
						v-model="knobs.newValueOption"
						:options="NEW_VALUE_OPTIONS"
					/>
					<span v-if="showNewValueOptions && showStartTimeOptions">and at the</span>
					<Dropdown
						v-if="showStartTimeOptions"
						class="toolbar-button ml-1 mr-1"
						v-model="knobs.startTimeOption"
						:options="START_TIME_OPTIONS"
					/>
					<span v-if="showStartTimeOptions">start time</span>
					<span>.</span>
				</p>
			</div>

			<div v-if="showNewValueOptions">
				<h6 class="pt-4, pb-3">New Value</h6>
				<div class="input-row">
					<div v-for="(objective, index) in NEW_VALUE_OPTIONS" :key="index" class="label-and-input">
						<div class="label-and-input">
							<label :for="objective">{{ objective }}</label>
							<tera-input type="number" v-model="knobs[objective]" />
						</div>
					</div>
				</div>
			</div>

			<div v-if="showStartTimeOptions">
				<h6 class="pt-4, pb-3">Start Time</h6>
				<div class="input-row">
					<div
						v-for="(objective, index) in START_TIME_OPTIONS"
						:key="index"
						class="label-and-input"
					>
						<div class="label-and-input">
							<label :for="objective">{{ objective }}</label>
							<tera-input type="number" v-model="knobs[objective]" />
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div v-else>
		<div v-for="(staticIntervention, index) in staticInterventions" :key="index">
			<p>
				Set the <b>{{ config.intervention?.type }}</b>
				<b>{{ config.intervention?.appliedTo }}</b> to a new value of
				<b>{{ staticIntervention.value }}</b> day at start time
				<b>{{ staticIntervention.timestep }}</b> day.
			</p>
		</div>
	</div>
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
	startTimeOption: props.config.startTimeOption ?? 'earliest'
});

// const emit = defineEmits(['update-self']);

const OPTIMIZATION_TYPES = ['new value', 'start time', 'new value and start time'];
const NEW_VALUE_OPTIONS = ['lower bound', 'upper bound', 'initial guess'];
const START_TIME_OPTIONS = ['earliest', 'latest', 'inital guess'];

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
