<template>
	<div>
		<suspense>
			<tera-model-template-editor :model="model" :kernel-manager="kernelManager"
		/></suspense>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getModel } from '@/services/model';
import TeraModelTemplateEditor from '@/components/model-template/tera-model-template-editor.vue';
import { KernelSessionManager } from '@/services/jupyter';
import { logger } from '@/utils/logger';

const kernelManager = new KernelSessionManager();
const model = ref();

async function initializeBeakerKernel(isModelLoaded = false) {
	if (kernelManager.jupyterSession) kernelManager.shutdown();

	try {
		const context = {
			context: 'mira_model_edit',
			language: 'python3',
			context_info: {
				id: isModelLoaded ? model.value?.id ?? '' : ''
			}
		};
		await kernelManager.init('beaker_kernel', 'Beaker Kernel', context);
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
}

onMounted(async () => {
	model.value = (await getModel('1cf59726-00b6-48a9-8db6-b9940a8dde8e')) ?? undefined;
	initializeBeakerKernel(model.value !== undefined);
});
</script>
