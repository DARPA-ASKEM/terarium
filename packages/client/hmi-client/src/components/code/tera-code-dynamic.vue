<template>
	<h3>{{ filename ?? '' }}</h3>
	<Card
		v-for="(block, index) in codefile?.dynamics?.block"
		class="code-dynamic-card-container"
		:key="index"
	>
		<template #title>
			Code Block
			<div v-if="!editable">
				<!-- <Button text icon="pi pi-pencil" @click="editable = true" /> -->
				<Button text icon="pi pi-trash" @click="deleteCodeBlock(index)" />
			</div>
		</template>
		<template #content>
			<div>
				Lines {{ extractDynamicRows(block).startRow + 1 }} to
				{{ extractDynamicRows(block).endRow + 1 }}
			</div>
		</template>
	</Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Button from 'primevue/button';
import { ref } from 'vue';
import { CodeFile } from '@/types/Types';
import { extractDynamicRows } from '@/utils/code-asset';
import { cloneDeep } from 'lodash';

const props = defineProps<{
	filename: string;
	codefile: CodeFile;
}>();

const emit = defineEmits(['remove']);
const editable = ref(false);

const deleteCodeBlock = (index: number) => {
	const clonedCodefile = cloneDeep(props.codefile);
	clonedCodefile.dynamics.block.splice(index, 1);

	emit('remove', {
		[props.filename]: {
			...clonedCodefile
		}
	});
};
</script>

<style scoped>
h3 {
	padding-top: 2rem;
}
.code-dynamic-card-container {
	width: 240px;
	max-width: 240px;
	border-left: solid 0.5rem var(--primary-color);
	margin-top: 1rem;
}

.code-dynamic-card-container:deep(.p-card-title) {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.edit-container {
	display: flex;
}

.edit-input-container:deep(.p-inputnumber-input) {
	width: 100px;
}

.footer-container {
	display: flex;
	justify-content: flex-end;
}
</style>
