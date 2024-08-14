<template>
	<section class="form-section">
		<h5>
			Output settings
			<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
		</h5>

		<!--Summary-->
		<h5>Summary</h5>
		<tera-checkbox
			v-model="knobs.summaryCheckbox"
			inputId="generate-summary"
			label="Auto-generate operation summary"
			subtext="Automatically generates a brief summary of the inputs and outputs."
			disabled
			@update:model-value="emit('update-self', knobs)"
		/>

		<!-- Note that this only exists for optimize. -->
		<div v-if="props.successDisplayChartsCheckbox">
			<!--Success Criteria-->
			<Divider />
			<h5>
				Success Criteria
				<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
			</h5>
			<tera-checkbox
				v-model="knobs.successDisplayChartsCheckbox"
				inputId="success-criteria-display-charts"
				label="Display chart(s)"
				subtext="Turn this on to generate an interactive chart of the success criteria conditions."
				disabled
				@update:model-value="emit('update-self', knobs)"
			/>
		</div>

		<div v-if="interventionsOptions.length > 0">
			<Divider />
			<!--Interventions-->
			<h5>
				Interventions
				<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
			</h5>
			<MultiSelect
				v-model="knobs.selectedInterventionVariables"
				:options="props.interventionsOptions"
				placeholder="What do you want to see?"
				filter
				@update:modelValue="emit('update-self', knobs)"
			/>
			<tera-checkbox
				v-model="knobs.interventionsDisplayChartsCheckbox"
				inputId="interventions-display-charts"
				label="Display chart(s)"
				disabled
				@update:model-value="emit('update-self', knobs)"
			/>
		</div>
		<Divider />

		<!--Simulation plots-->
		<h5>
			Simulation plots
			<i v-tooltip="outputSettingsToolTip" class="pi pi-info-circle" />
		</h5>
		<MultiSelect
			v-model="knobs.selectedSimulationVariables"
			:options="props.simulationChartOptions"
			placeholder="What do you want to see?"
			filter
			@update:modelValue="emit('update-self', knobs)"
		/>
		<tera-checkbox
			v-model="knobs.simulationDisplayChartsCheckbox"
			inputId="sim-plots-display-charts"
			label="Display chart(s)"
			disabled
			@update:model-value="emit('update-self', knobs)"
		/>
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import Divider from 'primevue/divider';
import MultiSelect from 'primevue/multiselect';

const props = defineProps<{
	successDisplayChartsCheckbox?: boolean;
	summaryCheckbox?: boolean;
	interventionsDisplayChartsCheckbox?: boolean;
	simulationDisplayChartsCheckbox?: boolean;
	selectedSimulationVariables?: string[];
	selectedInterventionVariables?: string[];
	simulationChartOptions: string[];
	interventionsOptions: string[];
}>();
const emit = defineEmits(['update-self']);
export interface OutputSettingKnobs {
	successDisplayChartsCheckbox: boolean;
	summaryCheckbox: boolean;
	interventionsDisplayChartsCheckbox: boolean;
	simulationDisplayChartsCheckbox: boolean;
	selectedSimulationVariables: string[];
	selectedInterventionVariables: string[];
}
const knobs = ref<OutputSettingKnobs>({
	successDisplayChartsCheckbox: props.successDisplayChartsCheckbox ?? false,
	summaryCheckbox: props.summaryCheckbox ?? true,
	interventionsDisplayChartsCheckbox: props.interventionsDisplayChartsCheckbox ?? true,
	simulationDisplayChartsCheckbox: props.simulationDisplayChartsCheckbox ?? true,
	selectedSimulationVariables: props.selectedSimulationVariables ?? [],
	selectedInterventionVariables: props.selectedInterventionVariables ?? []
});
const outputSettingsToolTip = 'TODO';
</script>

<style scoped></style>
