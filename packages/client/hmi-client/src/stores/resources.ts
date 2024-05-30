import { Store, defineStore } from 'pinia';

export type ResourceType = Store<
	'resources',
	{
		xddDataset: string | null;
	},
	{},
	{
		setXDDDataset(dataset: string | null): void;
	}
>;

/**
 * Mainly to store all data resources in this project, e.g. datasets, documents, models, etc.
 */
const useResourcesStore = defineStore('resources', {
	state: () => ({
		xddDataset: null as string | null
	}),
	getters: {
		getXddDataset: (state) => state.xddDataset
	},
	actions: {
		setXDDDataset(dataset: string | null) {
			this.xddDataset = dataset;
		}
	}
});

export default useResourcesStore;
