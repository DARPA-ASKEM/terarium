import { computed, shallowRef } from 'vue';
import type { Project } from '@/types/Types';

export const activeProject = shallowRef<Project | null>(null);
export const activeProjectId = computed<string>(() => activeProject.value?.id ?? '');
