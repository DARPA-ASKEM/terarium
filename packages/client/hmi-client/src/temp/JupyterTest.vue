<template>
	<h2>Juptyer Tests</h2>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { KernelSessionManager } from '@/services/jupyter-manager';

const context = {
	context: 'mira_model',
	language: 'python3',
	context_info: {
		id: 'sir-model-id'
	}
};

onMounted(async () => {
	const manager = new KernelSessionManager();
	await manager.init('beaker', 'Beaker', context);

	setTimeout(() => {
		const test = manager.sendMessage('stratify_request', {
			stratify_args: {
				key: 'test',
				strata: ['AA', 'BB', 'CC'],
				concepts_to_stratify: ['S']
			}
		});
		if (test) {
			test.on('stratify_response', (data) => {
				console.log('response', data);
			});
		}
	}, 1200);
});
</script>
