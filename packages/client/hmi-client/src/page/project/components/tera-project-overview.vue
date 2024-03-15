<template>
	<tera-asset
		:name="useProjects().activeProject.value?.name"
		:authors="useProjects().activeProject.value?.authors?.sort().join(', ')"
		:is-naming-asset="isRenamingProject"
		:publisher="`Last updated ${DateUtils.formatLong(
			useProjects().activeProject.value?.updatedOn ?? useProjects().activeProject.value?.createdOn
		)}`"
		:is-loading="useProjects().projectLoading.value"
	>
		<template #edit-buttons>
			<tera-project-menu :project="useProjects().activeProject.value" />
		</template>
		<template #overview-summary>
			<!-- Description & Contributors -->
			<p class="mt-1 mb-1">
				{{ useProjects().activeProject.value?.description }}
			</p>
		</template>
		<template #default>
			<tera-project-overview-editor :project="useProjects().activeProject.value" />
		</template>
	</tera-asset>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import * as DateUtils from '@/utils/date';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { useProjects } from '@/composables/project';
import TeraProjectMenu from '@/components/home/tera-project-menu.vue';
import teraProjectOverviewEditor from '@/components/home/tera-project-overview-editor.vue';

const isRenamingProject = ref(false);
</script>

<style scoped></style>
