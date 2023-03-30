export function formatDdMmmYyyy(timestamp) {
	return new Date(timestamp).toLocaleDateString(undefined, {
		year: 'numeric',
		month: 'short',
		day: 'numeric'
	});
}

export function formatMillisToDate(millis: number) {
	return new Date(millis).toLocaleDateString();
}
