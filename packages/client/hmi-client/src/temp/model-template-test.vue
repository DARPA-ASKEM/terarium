<template>
	<div>
		<suspense> <tera-model-template-editor :model="model" /></suspense>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getModel } from '@/services/model';
import TeraModelTemplateEditor from '@/components/model-template/tera-model-template-editor.vue';

const model = ref();

onMounted(async () => {
	model.value = (await getModel('1cf59726-00b6-48a9-8db6-b9940a8dde8e')) ?? undefined;
});
</script>
