import { logger } from '@/utils/logger';

function open(url: string) {
	// Define other window features as a comma-separated string (optional)
	const features: string = 'width=600,height=600,left=200,top=200,resizable=yes,scrollbars=yes';
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
