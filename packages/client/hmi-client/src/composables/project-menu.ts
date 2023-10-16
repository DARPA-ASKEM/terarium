import { IProject } from '@/types/Project';
import { ref } from 'vue';

const isShareDialogVisible = ref(false);
const isRemoveDialogVisible = ref(false);
const selectedMenuProject = ref<IProject>();

export function useProjectMenu() {
	return { isShareDialogVisible, isRemoveDialogVisible, selectedMenuProject };
}
