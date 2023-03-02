import { Tab } from '@/types/common';
import { defineStore } from 'pinia';

export const useTabStore = defineStore('tabs', {
	state: () => ({
		tabMap: new Map<string, Tab[]>() as Map<string, Tab[]>,
		activeTabIndexMap: new Map<string, number>() as Map<string, number>
	}),
	actions: {
		getTabs(context: string): Tab[] {
			return this.tabMap.get(context) ?? [];
		},
		setTabs(context: string, newTabs: Tab[]) {
			this.tabMap.set(context, newTabs);
		},
		addTab(context: string, newTab: Tab) {
			this.tabMap.get(context)?.push(newTab);
			const tabIndex = this.getTabs(context).length - 1;
			this.setActiveTabIndex(context, tabIndex);
		},
		removeTab(context: string, indexToRemove: number) {
			const activeTabIndex = this.getActiveTabIndex(context);
			const lastTabIndex = this.getTabs(context).length - 1;
			this.tabMap.get(context)?.splice(indexToRemove, 1);
			// If the tab that is closed is to the left of the active tab, decrement the active tab index by one, so that the active tab preserves its position.
			// E.g. if the active tab is the last tab, it will remain the last tab. If the active tab is second last, it will remain second last.
			// If the tab that is closed is to the right of the active tab, no special logic is needed to preserve the active tab's position.
			// If the active tab is closed, the next tab to the right becomes the active tab.
			// This replicates the tab behaviour in Chrome.
			if (
				activeTabIndex !== 0 &&
				(activeTabIndex > indexToRemove || activeTabIndex === lastTabIndex)
			) {
				this.activeTabIndexMap.set(context, activeTabIndex - 1);
			}
		},
		getActiveTabIndex(context: string): number {
			return this.activeTabIndexMap.get(context) ?? 0;
		},
		setActiveTabIndex(context: string, index: number) {
			this.activeTabIndexMap.set(context, index);
		}
	}
});
