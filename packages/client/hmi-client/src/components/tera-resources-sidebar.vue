<template>
	<nav>
		<div @click="pp">
			<Button icon="pi pi-file-edit" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button icon="pi pi-folder" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button
				icon="pi pi-sort-amount-down"
				class="p-button-icon-only p-button-text p-button-rounded"
			/>
			<Button icon="pi pi-arrows-v" class="p-button-icon-only p-button-text p-button-rounded" />
		</div>
		<Tree :value="resources" />
	</nav>
</template>

<script setup lang="ts">
import { ProjectType } from '@/types/Project';
import Tree from 'primevue/tree';
import Button from 'primevue/button';

const props = defineProps<{
	project: ProjectType | null;
}>();

const resources = [
	{
		key: '0',
		label: 'Documents',
		data: 'Documents Folder',
		icon: 'pi pi- fw pi-inbox',
		children: [
			{
				key: '0-0',
				label: 'Work',
				data: 'Work Folder',
				icon: 'pi pi-fw pi-cog',
				children: [
					{ key: '0-0-0', label: 'Expenses.doc', data: 'Expenses Document' },
					{ key: '0-0-1', label: 'Resume.doc', icon: 'pi pi-fw pi-file', data: 'Resume Document' }
				]
			},
			{
				key: '0-1',
				label: 'Home',
				data: 'Home Folder',
				icon: 'pi pi-fw pi-home',
				children: [
					{
						key: '0-1-0',
						label: 'Invoices.txt',
						icon: 'pi pi-fw pi-file',
						data: 'Invoices for this month'
					}
				]
			}
		]
	},
	{ key: '1', label: 'Resume.doc', icon: 'pi pi-fw pi-file', data: 'Resume Document' }
];

function pp() {
	console.log(props.project);
}
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	margin: 0 1rem 1rem 1rem;
	gap: 1rem;
}

/* nav div {
    margin-left: -0.5rem;
} */
</style>
