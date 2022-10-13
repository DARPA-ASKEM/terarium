import { defineStore } from 'pinia';

type MsgCancel = {
	message: string;
	cancelFn: Function;
};

/**
 * Main store used for app-level state
 */
// eslint-disable-next-line import/prefer-default-export
export const useAppStore = defineStore('app', {
	state: () => ({
		overlayActivated: false as boolean,
		overlayMessage: 'Loading...' as string,
		overlayMessageSecondary: '' as string,
		overlayCancelFn: null as Function | null,
		dataExplorerActivated: false as boolean
	}),
	actions: {
		enableOverlayWithCancel(payload: MsgCancel) {
			this.enableOverlay(payload.message);
			if (payload.cancelFn) {
				this.setOverlayCancelFn(payload.cancelFn);
			}
		},
		enableOverlay(message?: string) {
			this.overlayActivated = true;
			if (message !== undefined) {
				this.overlayMessage = message;
			}
		},
		setOverlaySecondaryMessage(message) {
			this.overlayMessageSecondary = message;
		},
		disableOverlay() {
			this.overlayActivated = false;
			this.overlayCancelFn = null;
		},
		setOverlayCancelFn(fn) {
			this.overlayCancelFn = fn;
		},
		showDataExplorer() {
			this.dataExplorerActivated = true;
		},
		hideDataExplorer() {
			this.dataExplorerActivated = false;
		}
	}
});
