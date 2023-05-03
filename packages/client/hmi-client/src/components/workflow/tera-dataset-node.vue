<template>
	<section>
		<Dropdown class="w-full" :options="datasets" option-label="name" v-model="selectedDataset">
		</Dropdown>
		<Accordion>
			<AccordionTab>
				<tera-dataset-datatable v-if="rawContent" :raw-content="rawContent" />
			</AccordionTab>
		</Accordion>
	</section>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import { Dataset } from '@/types/Dataset';
import { ref, watch } from 'vue';
import { downloadRawFile } from '@/services/dataset';
import { CsvAsset } from '@/types/Types';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import teraDatasetDatatable from '../dataset/tera-dataset-datatable.vue';

defineProps<{
	datasets: Dataset[];
}>();

const selectedDataset = ref<Dataset>();
const rawContent = ref<CsvAsset | null>(null);

watch(
	() => selectedDataset.value,
	async () => {
		if (selectedDataset.value) {
			rawContent.value = await downloadRawFile(selectedDataset.value.id.toString(), 10);
		}
	}
);
</script>

<style scoped>
section {
	display: flex;
	justify-content: center;
	flex-direction: column;
}
</style>
