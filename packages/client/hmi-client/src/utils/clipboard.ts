import { Ref, ref } from 'vue';

export const getClipboardText = async () => {
	// Workaround quirky situation if the devtools is open, it seems to execute from
	// the devtool's context rather than the document's context
	if (!document.hasFocus()) return '';

	const text = await navigator.clipboard.readText();
	return text;
};

export const setClipboardText = async (text: string) => {
	await navigator.clipboard.writeText(text);
};

export const pasteEventGenerator =
	(callback: (dti: DataTransferItem) => Promise<void>) => async (event: ClipboardEvent) => {
		const clipboardData = event.clipboardData;
		if (!clipboardData) return;
		const item = clipboardData.items[0];
		await callback(item);
	};

interface CopyTextToClipBoard {
	btnCopyLabel: Ref<string>;
	setCopyClipboard: (text: string) => void;
}

/**
 * Copy text to clipboard
 * Creates two element, a ref for a button label, and a onclick function to copy text to clipboard
 * @return CopyTextToClipBoard
 */
export function createCopyTextToClipboard(): CopyTextToClipBoard {
	const btnCopyLabel = ref('Copy to Clipboard');

	function setCopyClipboard(text: string) {
		btnCopyLabel.value = 'Copying';
		setClipboardText(text)
			.then(() => {
				btnCopyLabel.value = 'Copied to clipboard';
			})
			.catch(() => {
				btnCopyLabel.value = 'Failed to copy to clipboard';
			})
			.finally(() => {
				setTimeout(() => {
					btnCopyLabel.value = 'Copy to Clipboard';
				}, 2000);
			});
	}

	return {
		btnCopyLabel,
		setCopyClipboard
	};
}
