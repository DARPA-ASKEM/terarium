<template>
	<main>
		<h4>clipboard test</h4>
	</main>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';

const csvStringProcessor = async (item: DataTransferItem) => {
	if (item.kind !== 'string') return;
	if (item.type !== 'text/plain') return;

	const buffer: any[] = [];

	// Presume this is a full matrix, with row/column labels
	item.getAsString((text) => {
		// Parse

		const rows = text.split('\n');
		const columns = rows[0].split(',');

		for (let i = 1; i < rows.length; i++) {
			const data = rows[i].split(',');
			for (let j = 1; j < data.length; j++) {
				const rowLabel = data[0];
				const colLabel = columns[j];
				console.log('...', colLabel, rows[0], j);
				buffer.push({
					row: rowLabel,
					col: colLabel,
					value: data[j]
				});
			}
		}
		console.log('buffer', buffer);
	});

	return buffer;
};

const processPasteEvent = async (event: ClipboardEvent) => {
	const clipboardData = event.clipboardData;
	if (!clipboardData) return;
	const item = clipboardData.items[0];

	csvStringProcessor(item);
};

onMounted(() => {
	document.onpaste = processPasteEvent;
});

onUnmounted(() => {
	document.onpaste = null;
});
</script>
