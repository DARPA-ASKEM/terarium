import { defineStore } from 'pinia';
import { XDDArticle } from '@/types/XDD';
import { Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { getResourceID, isModel, isXDDArticle } from '@/utils/data-util';
import { omit } from 'lodash';
import { Project } from '@/types/Project';

/**
 * Mainly to store all data resources in this project, e.g. datasets, documents, models, etc.
 */
const useResourcesStore = defineStore('resources', {
	state: () => ({
		documents: {} as { [id: string]: XDDArticle },
		models: {} as { [id: string]: Model },
		xddDataset: null as string | null,
		activeProject: null as null | Project
	}),
	actions: {
		removeResource(resource: ResultType) {
			const resId = getResourceID(resource);
			if (isModel(resource)) {
				this.models = omit(this.models, [resId]);
			}
			if (isXDDArticle(resource)) {
				this.documents = omit(this.documents, [resId]);
			}
		},
		setXDDDataset(dataset: string | null) {
			this.xddDataset = dataset;
		},
		setActiveProject(project: null | Project) {
			this.activeProject = project;
		}
	}
});

export default useResourcesStore;
