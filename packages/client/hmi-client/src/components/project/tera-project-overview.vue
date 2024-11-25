<template>
	<tera-asset
		:id="useProjects().activeProject.value?.id"
		:name="useProjects().activeProject.value?.name"
		:authors="useProjects().activeProject.value?.authors?.sort().join(', ')"
		:publisher="`Last updated ${DateUtils.formatLong(
			useProjects().activeProject.value?.updatedOn ?? useProjects().activeProject.value?.createdOn
		)}`"
		:is-loading="useProjects().projectLoading.value"
	>
		<template #edit-buttons>
			<tera-project-menu :project="useProjects().activeProject.value" />
		</template>
		<template #summary>
			<!-- Description & Contributors -->
			<p class="overview-description">
				{{ useProjects().activeProject.value?.description }}
			</p>
		</template>
		<tera-project-overview-editor />
	</tera-asset>
</template>

<script setup lang="ts">
import * as DateUtils from '@/utils/date';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { useProjects } from '@/composables/project';
import TeraProjectMenu from '@/components/home/tera-project-menu.vue';
import teraProjectOverviewEditor from '@/components/project/tera-project-overview-editor.vue';
</script>
<style scoped>
.overview-description {
	margin-bottom: var(--gap-1);
	color: var(--text-color-primary);
}
</style>
