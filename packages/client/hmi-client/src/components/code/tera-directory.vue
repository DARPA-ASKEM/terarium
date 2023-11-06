<template>
	<aside class="min-w-10rem max-w-20rem">
		<Tree
			v-model:selectionKeys="selectedKey"
			:value="directoryTree"
			selectionMode="single"
			:filter="true"
			filterMode="lenient"
			filter-placeholder="Find"
			@node-select="onNodeSelect"
		>
		</Tree>
	</aside>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import Tree, { TreeNode, TreeSelectionKeys } from 'primevue/tree';

const directoryTree = ref<TreeNode[]>([]);

const props = defineProps<{
	files: string[];
}>();

const emit = defineEmits(['file-clicked']);

const selectedKey = ref<TreeSelectionKeys>();
onMounted(() => {
	directoryTree.value = buildDirectoryTree(props.files);
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

const onNodeSelect = (node: TreeNode) => {
	emit('file-clicked', node.data.fullPath);
};
</script>

<style scoped></style>
