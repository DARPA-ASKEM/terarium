<template>
	<h3>{{ filename ?? '' }}</h3>
	<Card v-for="(block, index) in codeBlocks" class="code-dynamic-card-container" :key="index">
		<template #title>
			Code Block
			<div v-if="editIndex !== index">
				<Button text icon="pi pi-pencil" @click="editCodeBlock(block.block, index)" />
				<Button text icon="pi pi-trash" @click="deleteCodeBlock(index)" />
			</div>
		</template>
		<template #content>
			<div v-if="editIndex !== index">
				Lines {{ extractDynamicRows(block.block).startRow + 1 }} to
				{{ extractDynamicRows(block.block).endRow + 1 }}
			</div>
			<div class="edit-container" v-else>
				<div class="edit-input-container">
					<label for="code-dynamic-start">Start</label>
					<InputNumber v-model="startLine" input-id="code-dynamic-start" />
				</div>

				<div class="edit-input-container">
					<label for="code-dynamic-start">End</label>
					<InputNumber v-model="endLine" input-id="code-dynamic-end" />
				</div>
			</div>

			<div v-if="props.isPreview && editIndex !== index" class="switch-container">
				<label>Include in process</label>
				<InputSwitch v-model="block.includeInProcess" />
			</div>
		</template>

		<template #footer v-if="editIndex === index">
			<div class="footer-container">
				<Button outlined label="Cancel" @click="editIndex = null" />
				<Button label="OK" @click="saveCodeBlockChanges(index)" />
			</div>
		</template>
	</Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import InputSwitch from 'primevue/inputswitch';
import { onMounted, ref, watch } from 'vue';
import { CodeFile } from '@/types/Types';
import { extractDynamicRows } from '@/utils/code-asset';
import { cloneDeep } from 'lodash';

const props = defineProps<{
	filename: string;
	codefile: CodeFile;
	isPreview?: boolean;
}>();

const emit = defineEmits(['remove', 'save']);
const editIndex = ref<number | null>(null);
const startLine = ref();
const endLine = ref();
const codeBlocks = ref<any>([]);

onMounted(() => {
	codeBlocks.value = props.codefile.dynamics.block.map((b) => ({
		block: b,
		includeInProcess: true
	}));
});
watch(
	() => props.codefile,
	() => {
		codeBlocks.value = props.codefile.dynamics.block.map((b) => ({
			block: b,
			includeInProcess: true
		}));
	}
);
const deleteCodeBlock = (index: number) => {
	const clonedCodefile = cloneDeep(props.codefile);
	clonedCodefile.dynamics.block.splice(index, 1);
	editIndex.value = null;

	emit('remove', {
		[props.filename]: {
			...clonedCodefile
		}
	});
};

const editCodeBlock = (block: string, index: number) => {
	const { startRow, endRow } = extractDynamicRows(block);
	startLine.value = startRow + 1;
	endLine.value = endRow + 1;
	editIndex.value = index;
};

const saveCodeBlockChanges = (index: number) => {
	if (isValidRange()) {
		const clonedCodefile = cloneDeep(props.codefile);
		clonedCodefile.dynamics.block[index] = `L${startLine.value}-L${endLine.value}`;
		emit('save', {
			[props.filename]: {
				...clonedCodefile
			}
		});
		editIndex.value = null;
	}
};

function isValidRange(): boolean {
	return startLine.value < endLine.value;
}
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

.footer-container > * {
	margin-left: 0.5rem;
}

.switch-container {
	display: flex;
	justify-content: space-between;
	padding-top: 1rem;
}
</style>
