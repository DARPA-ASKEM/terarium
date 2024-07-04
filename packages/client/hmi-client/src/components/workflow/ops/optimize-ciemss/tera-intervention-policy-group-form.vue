<template>
	<div class="policy-group">
		<div class="form-header">
			<div>
				<InputText
					v-if="isEditing"
					v-model="config.name"
					placeholder="Policy bounds"
					@focusout="emit('update-self', config)"
				/>
				<h6 v-else>{{ props.config.name }}</h6>
				<i
					:class="{ 'pi pi-check i': isEditing, 'pi pi-pencil i': !isEditing }"
					:style="'cursor: pointer'"
					@click="onEdit"
				/>
			</div>
			<div>
				<label for="active">Optimize</label>
				<InputSwitch v-model="config.isActive" @change="emit('update-self', config)" />
			</div>
			<div>
				<i
					class="trash-button pi pi-trash"
					:style="'cursor: pointer'"
					@click="emit('delete-self')"
				/>
			</div>
		</div>

		<div class="input-row">
			<p v-if="config.isActive">
				Find the
				<Dropdown
					class="toolbar-button"
					v-model="optimizeOption"
					option-label="label"
					option-value="value"
					:options="optimizeOptions"
				/>
				for the parameter <b>[name]</b>.
			</p>
			<p v-else>
				Set the parameter <b>[name]</b> to <b>{{ OPTIONS[optimizeOption] }}</b> day.
			</p>
		</div>
		<div v-if="config.isActive">
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
						optionLabel="label"
						optionValue="value"
						:options="objectiveOptions"
						@update:model-value="emit('update-self', config)"
					/>
					<span v-if="showStartTimeOptions">start time</span>
					<span>.</span>
				</p>
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
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import InputSwitch from 'primevue/inputswitch';
import {
	InterventionPolicyGroup,
	InterventionTypes
} from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';

const props = defineProps<{
	parameterOptions: string[];
	config: InterventionPolicyGroup;
	interventionType?: InterventionTypes;
}>();

const emit = defineEmits(['update-self', 'delete-self']);

const config = ref<InterventionPolicyGroup>(_.cloneDeep(props.config));
const isEditing = ref<boolean>(false);

const onEdit = () => {
	isEditing.value = !isEditing.value;
};

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
	[LOWER_BOUND]: 0,
	[UPPER_BOUND]: 0,
	[INITIAL_GUESS]: 0
});

const newValueObjectives = ref({
	[LOWER_BOUND]: 0,
	[UPPER_BOUND]: 1,
	[INITIAL_GUESS]: 0.5
});

const optimizeOptions = [
	{ label: OPTIONS[VALUE], value: VALUE },
	{ label: OPTIONS[START_TIME], value: START_TIME },
	{ label: OPTIONS[VALUE_START_TIME], value: VALUE_START_TIME }
];

const objectiveOptions = [
	{ label: OPTIONS[LOWER_BOUND], value: LOWER_BOUND },
	{ label: OPTIONS[UPPER_BOUND], value: UPPER_BOUND },
	{ label: OPTIONS[INITIAL_GUESS], value: INITIAL_GUESS }
];

const showStartTimeOptions = computed(
	() => optimizeOption.value === START_TIME || optimizeOption.value === VALUE_START_TIME
);
const showNewValueOptions = computed(
	() => optimizeOption.value === VALUE || optimizeOption.value === VALUE_START_TIME
);
</script>

<style scoped>
.policy-group {
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	gap: 0.5rem;
	border-radius: 0.375rem;
	background: #fff;
	border: 1px solid rgba(0, 0, 0, 0.08);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}

.form-header {
	width: 100%;
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	gap: 1rem;
	padding-bottom: 0.5rem;

	& > *:first-child {
		margin-right: auto;
	}

	& > * {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		gap: 0.5rem;
	}
}

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
