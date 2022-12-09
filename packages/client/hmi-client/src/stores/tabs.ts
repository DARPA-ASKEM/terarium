import { Tab } from '@/types/common';
import { defineStore } from 'pinia';

export const useTabStore = defineStore('tabs', {
	state: () => ({
		tabMap: new Map<String, Tab[]>() as Map<String, Tab[]>,
		activeTabIndex: 0 as number
	}),
	actions: {
		get(context: string): Tab[] | undefined {
			return this.tabMap.get(context);
		},
		set(context: string, newTabs: Tab[]) {
			this.tabMap.set(context, newTabs);
		}
	}
});
