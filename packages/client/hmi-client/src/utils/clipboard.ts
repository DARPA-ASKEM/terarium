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
