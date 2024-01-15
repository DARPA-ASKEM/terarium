<template>
	<ul>
		<li v-for="(codeBlocks, filename) in formattedCodeBlocks" :key="filename">
			<h3>{{ filename ?? '' }}</h3>
			<ul>
				<li v-for="(codeBlock, i) in codeBlocks" :key="i">
					<tera-asset-block
						is-deletable
						is-editable
						:is-permitted="false"
						:is-toggleable="false"
						@edit="editCodeBlock(codeBlock.asset.block, filename as string, i)"
						@delete="deleteCodeBlock(filename as string, i)"
						:key="i"
					>
						<template #header>
							<h5>Code Block</h5>
						</template>
						<div
							v-if="
								(editRef?.index !== i || editRef?.filename !== filename) && codeBlock.asset.block
							"
						>
							Lines {{ extractDynamicRows(codeBlock.asset.block).startRow + 1 }} to
							{{ extractDynamicRows(codeBlock.asset.block).endRow + 1 }}
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

						<template #footer v-if="editRef?.index === i && editRef?.filename === filename">
							<div class="footer-container">
								<Button outlined label="Cancel" @click="editRef = null" />
								<Button label="OK" @click="saveCodeBlockChanges" />
							</div>
						</template>
					</tera-asset-block>
				</li>
			</ul>
		</li>
	</ul>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import { computed, onMounted, ref, watch } from 'vue';
import { Code } from '@/types/Types';
import { CodeBlock, extractDynamicRows, getCodeBlocks } from '@/utils/code-asset';
import { cloneDeep, groupBy } from 'lodash';
import { AssetBlock } from '@/types/workflow';
import TeraAssetBlock from '../widgets/tera-asset-block.vue';

const props = defineProps<{
	code: Code;
	isPreview?: boolean;
}>();

const emit = defineEmits(['remove', 'save']);
const editRef = ref<{ filename: string; index: number } | null>(null);
const startLine = ref();
const endLine = ref();
const codeBlocks = ref<AssetBlock<CodeBlock>[]>();

const formattedCodeBlocks = computed(() => groupBy(codeBlocks.value, 'asset.filename'));

onMounted(async () => {
	if (props.code) {
		codeBlocks.value = await getCodeBlocks(props.code);
	}
});
watch(
	() => props.code,
	async () => {
		codeBlocks.value = await getCodeBlocks(props.code);
	},
	{ deep: true }
);
const deleteCodeBlock = (filename: string, index: number) => {
	const clonedCodefile = cloneDeep(props.code?.files?.[filename]);
	if (!clonedCodefile) return;
	clonedCodefile.dynamics.block.splice(index, 1);
	editRef.value = null;

	emit('remove', {
		[filename]: {
			...clonedCodefile
		}
	});
};

const editCodeBlock = (block: string | undefined, filename: string, index: number) => {
	if (!block) return;
	const { startRow, endRow } = extractDynamicRows(block);
	startLine.value = startRow + 1;
	endLine.value = endRow + 1;
	editRef.value = { filename, index };
};

const saveCodeBlockChanges = () => {
	if (isValidRange() && editRef.value) {
		const filename = editRef.value.filename;
		const index = editRef.value.index;
		const clonedCodefile = cloneDeep(props.code?.files?.[filename]);
		if (!clonedCodefile) return;
		clonedCodefile.dynamics.block[index] = `L${startLine.value}-L${endLine.value}`;
		emit('save', {
			[filename]: {
				...clonedCodefile
			}
		});
		editRef.value = null;
	}
};

function isValidRange(): boolean {
	return startLine.value < endLine.value;
}
</script>

<style scoped>
ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
}

.edit-container {
	display: flex;
}

.edit-input-container:deep(.p-inputnumber-input) {
	width: 100px;
}
</style>
