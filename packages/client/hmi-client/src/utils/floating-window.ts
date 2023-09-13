import { logger } from '@/utils/logger';

interface FloatingWindowOptions {
	width?: number;
	height?: number;
	left?: number;
	top?: number;
}
function open(url: string, options?: FloatingWindowOptions) {
	// Define other window features as a comma-separated string (optional)
	const features: string = `
		width=${options?.width ?? 700},
		height=${options?.height ?? 700},
		left=${options?.left ?? 200},
		top=${options?.top ?? 200},
		resizable=yes,
		scrollbars=yes
	`;
	const floatingWindow = window.open(url, 'FloatingWindow', features);
	// Check if the window was blocked by a popup blocker
	if (!floatingWindow) {
		logger.info('Please allow popups for this site.', { toastTitle: 'Popup blocked' });
	} else {
		floatingWindow.focus();
	}
}

export default {
	open
};
