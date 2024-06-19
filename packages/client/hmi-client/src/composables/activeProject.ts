import { computed, shallowRef, watch } from 'vue';
import type { Project } from '@/types/Types';

export const activeProject = shallowRef<Project | null>(null);
export const activeProjectId = computed<string>(
	() => activeProject.value?.id ?? localStorage.getItem('activeProjectId') ?? ''
);

watch(
	activeProjectId,
	async (projectId) => {
		if (projectId !== activeProjectId.value || localStorage.getItem('activeProjectId') == null) {
			localStorage.setItem('activeProjectId', projectId);
		}
	},
	{ immediate: true }
);
