<template>
	<aside class="tree-container">
		<Tree
			v-model:selectionKeys="selectedKey"
			v-model:expanded-keys="expandedKeys"
			:value="directoryTree"
			selectionMode="single"
			:filter="true"
			filterMode="lenient"
			filter-placeholder="Find"
			@node-select="onNodeSelect"
		/>
	</aside>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import Tree, { TreeExpandedKeys, TreeSelectionKeys } from 'primevue/tree';
import type { TreeNode } from 'primevue/treenode';

const directoryTree = ref<TreeNode[]>([]);

const props = defineProps<{
	files: string[];
}>();

const emit = defineEmits(['file-clicked']);

const selectedKey = ref<TreeSelectionKeys>();
const expandedKeys = ref<TreeExpandedKeys>();

onMounted(() => {
	directoryTree.value = buildDirectoryTree(props.files)[0].children ?? [];
	expandFirstDirectory(directoryTree.value);
});

function buildDirectoryTree(files) {
	const root: TreeNode = {
		key: '..',
		label: '..',
		icon: 'pi pi-fw pi-folder',
		selectable: false,
		data: {
			isDirectory: true
		},
		children: []
	};

	files.forEach((filePath) => {
		const parts = filePath.split('/');
		let currentNode: TreeNode = root;

		parts.forEach((part, i) => {
			const isDirectory = i < parts.length - 1;
			const existingNode = currentNode.children?.find((node) => node.label === part);

			if (existingNode) {
				currentNode = existingNode;
			} else {
				const newNode: TreeNode = {
					key: part,
					icon: `pi pi-fw ${isDirectory ? 'pi-folder' : 'pi-file'}`,
					label: part,
					selectable: !isDirectory,
					data: {
						isDirectory
					}
				};

				if (!isDirectory) {
					newNode.data.fullPath = filePath;
				}

				if (!currentNode.children) {
					currentNode.children = [];
				}

				currentNode.children.push(newNode);
				currentNode = newNode;
			}
		});
	});
	return [root];
}

// function to find if an inital directory exists, if so expand it when first loaded
function expandFirstDirectory(nodes: TreeNode[]) {
	const directoryNode = nodes.find((node) => node.data.isDirectory);
	if (!directoryNode || !directoryNode.key) return;

	expandedKeys.value = { [directoryNode.key]: true };
}
const onNodeSelect = (node: TreeNode) => {
	emit('file-clicked', node.data.fullPath);
};

watch(
	() => props.files,
	() => {
		directoryTree.value = buildDirectoryTree(props.files)[0].children ?? [];
	}
);
</script>

<style scoped>
.tree-container {
	min-width: 10rem;
	max-width: 20rem;
}

.tree-container:deep(.p-tree) {
	display: flex;
	flex-direction: column;
	height: 100%;
}
</style>
