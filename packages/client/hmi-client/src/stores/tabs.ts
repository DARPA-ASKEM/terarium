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
		addTab(context: string, newTab: Tab) {
			this.tabMap.get(context)?.push(newTab);
			const tabIndex = this.activeTabIndexMap.get(context) ?? 0;
			this.activeTabIndexMap.set(context, tabIndex.valueOf() + 1);
		},
		removeTab(context: string, indexToRemove: number) {
			const tabs = this.tabMap.get(context);
			const activeTabIndex = this.activeTabIndexMap.get(context) ?? 0;
			const lastTabIndex = tabs ? tabs.length - 1 : 0;
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
				this.activeTabIndexMap.set(context, activeTabIndex.valueOf() - 1);
			}
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
