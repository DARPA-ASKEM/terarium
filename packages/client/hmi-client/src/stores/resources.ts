import { defineStore } from 'pinia';
import { Project, ProjectAssets } from '@/types/Project';

/**
 * Mainly to store all data resources in this project, e.g. datasets, documents, models, etc.
 */
const useResourcesStore = defineStore('resources', {
	state: () => ({
		xddDataset: null as string | null,
		activeProject: null as null | Project,
		activeProjectAssets: null as ProjectAssets | null
	}),
	actions: {
		setXDDDataset(dataset: string | null) {
			this.xddDataset = dataset;
		},
		setActiveProject(project: null | Project) {
			this.activeProject = project;
		}
	}
});

export default useResourcesStore;
