import { ref } from 'vue';
import { Project } from '@/types/Types';

const isShareDialogVisible = ref(false);
const isRemoveDialogVisible = ref(false);
const isProjectConfigDialogVisible = ref(false);
const isMakeSampleDialogVisible = ref(false);
const menuProject = ref<Project | null>(null);

export function useProjectMenu() {
	return {
		isShareDialogVisible,
		isRemoveDialogVisible,
		isProjectConfigDialogVisible,
		isMakeSampleDialogVisible,
		menuProject
	};
}
