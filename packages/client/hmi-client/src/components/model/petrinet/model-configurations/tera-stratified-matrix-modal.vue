<template>
	<tera-modal @modal-mask-clicked="emit('close-modal')">
		<template #header>
			<div class="flex align-items-center justify-space-between">
				<h4 class="w-full">{{ id }} matrix</h4>
				<div class="flex align-items-center gap-2 white-space-nowrap">
					<label for="matrixShouldEval" class="mr-2">Evaluate expressions</label>
					<InputSwitch
						inputId="matrixShouldEval"
						v-model="matrixShouldEval"
						:binary="true"
						label="Evaluate expressions?"
					/>
				</div>
			</div>
		</template>
		<template #default>
			<tera-stratified-matrix
				v-bind="props"
				:should-eval="matrixShouldEval"
				@update-configuration="
					(configToUpdate: ModelConfiguration) => emit('update-configuration', configToUpdate)
				"
			/>
		</template>
		<template #footer>
			<Button size="large" label="OK" @click="emit('close-modal')" />
			<Button
				size="large"
				severity="secondary"
				class="p-button-outlined"
				label="Cancel"
				@click="emit('close-modal')"
			/>
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { ModelConfiguration } from '@/types/Types';
import InputSwitch from 'primevue/inputswitch';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { StratifiedMatrix } from '@/types/Model';
import TeraStratifiedMatrix from './tera-stratified-matrix.vue';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	id: string;
	stratifiedMatrixType: StratifiedMatrix;
	openValueConfig: boolean;
}>();

const emit = defineEmits(['close-modal', 'update-configuration']);

const matrixShouldEval = ref(false);
</script>

<style scoped>
main:deep(.content) {
	min-width: 40rem;
}
</style>
