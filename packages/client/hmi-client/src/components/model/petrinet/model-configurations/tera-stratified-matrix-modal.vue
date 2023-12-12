<template>
	<tera-modal @modal-mask-clicked="emit('close-modal')">
		<template #header>
			<h4>{{ id }}</h4>
			<span>Configure the matrix values</span>
			<div class="flex align-items-center">
				<Checkbox
					inputId="matrixShouldEval"
					v-model="matrixShouldEval"
					:binary="true"
					label="Evaluate expressions?"
				/>
				<label for="matrixShouldEval" class="ml-2"> Evaluate Expressions? </label>
			</div>
		</template>
		<template #default>
			<!-- TODO: Implement value tabs for the modal once we are ready
					<TabView v-model:activeIndex="activeIndex">
					<TabPanel v-for="(extraction, i) in extractions" :key="i">
						<template #header>
							<span>{{ extraction.name }}</span>
						</template>
						<div>
							<label for="name">Name</label>
							<InputText class="p-inputtext-sm" :key="'name' + i" v-model="extraction.name" />
						</div>
						<div>
							<label for="name">Matrix</label>
							<tera-stratified-value-matrix
								:model-configuration="modelConfigurations[modalAttributes.configIndex]"
								:id="modalAttributes.id"
								:stratified-model-type="stratifiedModelType"
								:node-type="modalAttributes.stratifiedMatrixType"
							/>
						</div>
					</TabPanel>
				</TabView> -->
			<tera-stratified-matrix
				v-bind="props"
				:should-eval="matrixShouldEval"
				@update-configuration="
					(configToUpdate: ModelConfiguration) => emit('update-configuration', configToUpdate)
				"
			/>
		</template>
		<template #footer>
			<Button label="OK" @click="emit('close-modal')" />
			<Button class="p-button-outlined" label="Cancel" @click="emit('close-modal')" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { StratifiedModel } from '@/model-representation/petrinet/petrinet-service';
import { ModelConfiguration } from '@/types/Types';
import Checkbox from 'primevue/checkbox';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { StratifiedMatrix } from '@/types/Model';
import TeraStratifiedMatrix from './tera-stratified-matrix.vue';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	id: string;
	stratifiedModelType: StratifiedModel;
	stratifiedMatrixType: StratifiedMatrix;
	openValueConfig: boolean;
}>();

const emit = defineEmits(['close-modal', 'update-configuration']);

const matrixShouldEval = ref(true);
</script>
