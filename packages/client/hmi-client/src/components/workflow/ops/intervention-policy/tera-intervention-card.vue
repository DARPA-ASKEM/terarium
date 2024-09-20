<template>
	<div class="intervention-card">
		<header class="flex align-items-center gap-2">
			<tera-toggleable-input :model-value="intervention.name" @update:model-value="onUpdateName($event)" tag="h6" />
			<div class="flex align-items-center ml-auto">
				<RadioButton
					:model-value="interventionType"
					:input-id="uniqueId()"
					name="interventionType"
					value="static"
					@click="onInterventionTypeChange('static')"
				/>
				<label for="static" class="ml-2">Static</label>
				<RadioButton
					:model-value="interventionType"
					:input-id="uniqueId()"
					name="interventionType"
					value="dynamic"
					class="ml-3"
					@click="onInterventionTypeChange('dynamic')"
				/>
				<label for="dynamic" class="ml-2">Dynamic</label>
				<Button class="ml-3" icon="pi pi-trash" text @click="emit('delete')" />
			</div>
		</header>
		<section>
			<div class="flex align-items-center flex-wrap gap-2">
				Set
				<section>
					<Dropdown
						class="type-menu"
						:model-value="intervention.type"
						@change="onSemanticChange"
						:options="interventionSemanticOptions"
						option-label="label"
						option-value="value"
					/>
					<Dropdown
						class="applied-to-menu"
						:model-value="intervention.appliedTo"
						@change="onAppliedToParameterChange"
						:options="semanticOptions"
						option-label="label"
						option-value="value"
						placeholder="Select"
					/>
				</section>
				<!-- Static -->
				<template v-if="interventionType === 'static'">
					to
					<template v-if="intervention.staticInterventions.length > 1">...</template>
					<template v-else-if="intervention.staticInterventions.length === 1">
						<tera-input-number
							auto-width
							:model-value="intervention.staticInterventions[0].value"
							@update:model-value="(val) => onUpdateValue(val, 0)"
							placeholder="value"
						/>
						starting at
						<tera-input-number
							auto-width
							:model-value="intervention.staticInterventions[0].timestep"
							@update:model-value="(val) => onUpdateThreshold(val, 0)"
							placeholder="timestep"
						/>
						.
					</template>

					<ul v-if="intervention.staticInterventions.length > 1" class="w-full">
						<li v-for="(i, index) in intervention.staticInterventions" class="flex-1" :key="index">
							<div class="flex align-items-center pt-2 pb-2 gap-2">
								<tera-input-number
									auto-width
									:model-value="i.value"
									@update:model-value="(val) => onUpdateValue(val, index)"
									placeholder="value"
								/>
								starting at
								<tera-input-number
									auto-width
									:model-value="i.timestep"
									@update:model-value="(val) => onUpdateThreshold(val, index)"
									placeholder="timestep"
								/>
								.
								<Button class="ml-auto" icon="pi pi-times" text @click="onRemoveStaticIntervention(index)" />
							</div>
							<Divider />
						</li>
					</ul>
				</template>

				<!-- Dynamic -->
				<template v-else>
					to
					<tera-input-number
						auto-width
						:model-value="intervention.dynamicInterventions[0].value"
						@update:model-value="(val) => onUpdateValue(val, 0)"
						placeholder="value"
					/>
					when
					<Dropdown
						:model-value="intervention.dynamicInterventions[0].parameter"
						@change="onTargetParameterChange"
						:options="stateOptions"
						option-label="label"
						option-value="value"
						placeholder="Select a trigger"
					/>
					<Dropdown
						:model-value="intervention.dynamicInterventions[0].isGreaterThan"
						@change="onComparisonOperatorChange"
						:options="comparisonOperations"
						option-label="label"
						option-value="value"
					/>
					<tera-input-number
						auto-width
						:model-value="intervention.dynamicInterventions[0].threshold"
						@update:model-value="(val) => onUpdateThreshold(val, 0)"
						placeholder="threshold"
					/>
					{{ dynamicInterventionUnits }}.
				</template>
			</div>
		</section>
		<footer>
			<Button
				v-if="interventionType === 'static'"
				text
				icon="pi pi-plus"
				label="Add"
				@click="onAddNewStaticIntervention"
				size="small"
			/>
		</footer>
	</div>
</template>

<script setup lang="ts">
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import Button from 'primevue/button';
import RadioButton from 'primevue/radiobutton';
import { computed } from 'vue';
import { Intervention, InterventionSemanticType } from '@/types/Types';
import Dropdown, { DropdownChangeEvent } from 'primevue/dropdown';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { cloneDeep, debounce, uniqueId } from 'lodash';
import Divider from 'primevue/divider';

const emit = defineEmits(['update', 'delete', 'add']);
const props = defineProps<{
	intervention: Intervention;
	parameterOptions: { label: string; value: string; units?: string }[];
	stateOptions: { label: string; value: string; units?: string }[];
}>();

const interventionSemanticOptions = [
	{ label: 'Parameter', value: InterventionSemanticType.Parameter },
	{ label: 'State', value: InterventionSemanticType.State }
];

const semanticOptions = computed(() => {
	if (props.intervention.type === InterventionSemanticType.State) {
		return props.stateOptions;
	}
	return props.parameterOptions;
});

const interventionType = computed(() => {
	if (props.intervention.staticInterventions.length > 0) {
		return 'static';
	}
	if (props.intervention.dynamicInterventions.length > 0) {
		return 'dynamic';
	}
	return 'static';
});

const dynamicInterventionUnits = computed(() => {
	let units = '';
	const type = props.intervention.type;
	const appliedTo = props.intervention.appliedTo;

	if (type === InterventionSemanticType.Parameter) {
		units = props.parameterOptions.find((parameter) => parameter.label === appliedTo)?.units ?? '';
	} else {
		units = props.stateOptions.find((state) => state.label === appliedTo)?.units ?? '';
	}
	return units;
});

const comparisonOperations = [
	{ label: 'increases to above', value: true },
	{ label: 'decreases to below', value: false }
];

const onUpdateName = (name: string) => {
	const intervention = cloneDeep(props.intervention);
	intervention.name = name;
	debounceUpdateState(intervention);
};

const onAppliedToParameterChange = (event: DropdownChangeEvent) => {
	const intervention = cloneDeep(props.intervention);
	intervention.appliedTo = event.value;
	emit('update', intervention);
};

const onUpdateThreshold = (value: number, index: number) => {
	const intervention = cloneDeep(props.intervention);
	if (interventionType.value === 'static') {
		intervention.staticInterventions[index].timestep = value;
	} else {
		intervention.dynamicInterventions[index].threshold = value;
	}
	emit('update', intervention);
};

const onUpdateValue = (value: number, index: number) => {
	const intervention = cloneDeep(props.intervention);
	if (interventionType.value === 'static') {
		intervention.staticInterventions[index].value = value;
	} else {
		intervention.dynamicInterventions[index].value = value;
	}
	emit('update', intervention);
};

const onRemoveStaticIntervention = (index: number) => {
	const intervention = cloneDeep(props.intervention);
	intervention.staticInterventions.splice(index, 1);
	emit('update', intervention);
};

const onAddNewStaticIntervention = () => {
	const intervention = cloneDeep(props.intervention);
	intervention.staticInterventions.push({
		timestep: Number.NaN,
		value: Number.NaN
	});
	emit('update', intervention);
};

const onInterventionTypeChange = (value: string) => {
	const intervention = cloneDeep(props.intervention);
	if (value === 'static') {
		intervention.staticInterventions = [
			{
				timestep: Number.NaN,
				value: Number.NaN
			}
		];
		intervention.dynamicInterventions = [];
	} else if (value === 'dynamic') {
		intervention.staticInterventions = [];
		intervention.dynamicInterventions = [
			{
				threshold: Number.NaN,
				value: Number.NaN,
				parameter: '',
				isGreaterThan: true
			}
		];
	}

	emit('update', intervention);
};

const onTargetParameterChange = (event: DropdownChangeEvent) => {
	const intervention = cloneDeep(props.intervention);
	intervention.dynamicInterventions[0].parameter = event.value;
	emit('update', intervention);
};

const onComparisonOperatorChange = (event: DropdownChangeEvent) => {
	const intervention = cloneDeep(props.intervention);
	intervention.dynamicInterventions[0].isGreaterThan = event.value;
	emit('update', intervention);
};

const onSemanticChange = (event: DropdownChangeEvent) => {
	const intervention = cloneDeep(props.intervention);
	intervention.type = event.value;
	if (event.value === InterventionSemanticType.State) {
		intervention.appliedTo = '';
	} else {
		intervention.appliedTo = '';
	}
	emit('update', intervention);
};

const debounceUpdateState = debounce((intervention) => {
	emit('update', intervention);
}, 100);
</script>

<style scoped>
:deep(.p-divider) {
	&.p-divider-horizontal {
		margin-top: 0;
		margin-bottom: 0;
		color: var(--gray-300);
	}
}

.type-menu {
	border-radius: var(--border-radius) 0 0 var(--border-radius);
	background: var(--surface-200);
}

.applied-to-menu {
	border-radius: 0 var(--border-radius) var(--border-radius) 0;
}

.intervention-card {
	background-color: var(--surface-50);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius-medium);
	padding: var(--gap-2) var(--gap);
	gap: var(--gap-2);
	display: flex;
	flex-direction: column;
}

ul {
	list-style: none;
}
</style>
