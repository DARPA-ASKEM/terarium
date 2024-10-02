import { computed, ref } from 'vue';
import type { Project } from '@/types/Types';

export const activeProject = ref<Project | null>(null);
export const activeProjectId = computed<string>(() => activeProject.value?.id ?? '');
