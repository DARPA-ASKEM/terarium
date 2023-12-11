import { IProject } from '@/types/Project';
import { ref } from 'vue';

const isShareDialogVisible = ref(false);
const isRemoveDialogVisible = ref(false);
const isProjectConfigDialogVisible = ref(false);
const menuProject = ref<IProject | null>(null);

export function useProjectMenu() {
	return {
		isShareDialogVisible,
		isRemoveDialogVisible,
		isProjectConfigDialogVisible,
		menuProject
	};
}
