import { defineStore } from 'pinia';
import { ProjectType, ProjectAssets } from '@/types/Project';

/**
 * Mainly to store all data resources in this project, e.g. datasets, documents, models, etc.
 */
const useResourcesStore = defineStore('resources', {
	state: () => ({
		xddDataset: null as string | null,
		activeProject: null as null | ProjectType,
		activeProjectAssets: null as ProjectAssets | null
	}),
	actions: {
		setXDDDataset(dataset: string | null) {
			this.xddDataset = dataset;
		},
		setActiveProject(project: null | ProjectType) {
			this.activeProject = project;
		},
		reset() {
			this.activeProject = null;
			this.activeProjectAssets = null;
		}
	}
});

export default useResourcesStore;
