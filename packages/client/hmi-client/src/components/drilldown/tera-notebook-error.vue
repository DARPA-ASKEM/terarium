<template>
	<div class="container" :class="{ hide: hide || isEmpty(traceback) }">
		<h6>
			<span>{{ props.name }}</span>
			<Button rounded text icon="pi pi-times" @click="hide = true" />
		</h6>
		<Accordion multiple :active-index="currentActiveIndex" class="px-2">
			<AccordionTab header="Last Error Message">
				<code>{{ lastErrorMessage }}</code>
			</AccordionTab>
			<AccordionTab header="Full traceback">
				<code>{{ localTraceback }}</code>
			</AccordionTab>
		</Accordion>
	</div>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';

const props = defineProps<{
	name?: string;
	value?: string;
	traceback?: string;
}>();

const hide = ref(false);
const localTraceback = ref('');
const lastErrorMessage = ref('');
const currentActiveIndex = ref([0]);

watch(
	() => props.traceback,
	() => {
		if (!props.traceback) return;
		localTraceback.value = props.traceback;
		hide.value = false;
		const splitTraceback = props.traceback.split('Error:');
		if (splitTraceback.length > 1) {
			currentActiveIndex.value = [0];
			lastErrorMessage.value = splitTraceback[splitTraceback.length - 1];
		} else {
			// If for some reason we cannot find "Error:" then we will default to open the full trace
			currentActiveIndex.value = [1];
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.container {
	margin: 0 var(--gap-3) var(--gap-3) var(--gap-3);
	background-color: var(--surface-error);
	padding: var(--gap-3);
	border-radius: var(--border-radius);
}

h6 {
	display: flex;
	justify-content: space-between;
}

.hide {
	display: none;
}

:deep(.p-accordion-content) {
	background-color: var(--surface-error);
}
:deep(.p-accordion-header-link) {
	background-color: var(--surface-error);
}
</style>
