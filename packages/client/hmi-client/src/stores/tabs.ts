import { Tab } from '@/types/common';
import { defineStore } from 'pinia';

export const useTabStore = defineStore('tabs', {
	state: () => ({
		tabMap: new Map<String, Tab[]>() as Map<String, Tab[]>,
		activeTabIndexMap: new Map<String, Number>() as Map<String, Number>
	}),
	actions: {
		getTabs(context: string): Tab[] | undefined {
			return this.tabMap.get(context);
		},
		setTabs(context: string, newTabs: Tab[]) {
			this.tabMap.set(context, newTabs);
		},
		getActiveTabIndex(context: string): number {
			const activeTabIndex = this.activeTabIndexMap.get(context);
			return activeTabIndex === undefined ? 0 : activeTabIndex.valueOf();
		},
		setActiveTabIndex(context: string, index: number) {
			this.activeTabIndexMap.set(context, index);
		}
	}
});
