import { defineStore } from 'pinia';
import { XDDArticle } from '@/types/XDD';
import { Model } from '@/types/Model';
import { ResultType } from '@/types/common';
import { getResourceID, isModel, isXDDArticle } from '@/utils/data-util';
import { omit } from 'lodash';

/**
 * Mainly to store all data resources in this project, e.g. datasets, documents, models, etc.
 */
const useResourcesStore = defineStore('resources', {
	state: () => ({
		documents: {} as { [id: string]: XDDArticle },
		models: {} as { [id: string]: Model },
		xddDataset: null as string | null
	}),
	actions: {
		addResource(resource: ResultType) {
			const resId = getResourceID(resource);
			if (isModel(resource)) {
				this.models[resId] = resource as Model;
			}
			if (isXDDArticle(resource)) {
				this.documents[resId] = resource as XDDArticle;
			}
		},
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
		}
	}
});

export default useResourcesStore;
