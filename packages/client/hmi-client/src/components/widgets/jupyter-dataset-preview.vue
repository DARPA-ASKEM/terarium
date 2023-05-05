<template>
    	<template v-if="props.rawContent">
		<h5>Preview</h5>
				<section v-if="csvContent">
					<span>{{ `${csvContent[0].length} columns | ${csvContent.length - 1} rows` }} </span>
					<DataTable class="p-datatable-xsm" :value="csvContent.slice(1)">
						<Column
							v-for="(colName, index) of csvHeaders"
							:key="index"
							:field="index.toString()"
							:header="colName"
						/>
					</DataTable>
					<span>Showing first {{ csvContent.length - 1 }} rows</span>
				</section>
	</template>
</template>


<script setup lang="ts">
import { computed } from 'vue';
import { SessionContext } from '@jupyterlab/apputils';
import { CsvAsset } from '@/types/Types';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const props = defineProps<{
	llmContext:  SessionContext,
	context?: String | null,
	context_info?: Object | null,
    rawContent?: CsvAsset | null,
}>();

const csvContent = computed(() => props.rawContent?.csv);
const csvHeaders = computed(() => props.rawContent?.headers);

</script>

<style scoped>
</style>
