export function formatDdMmmYyyy(timestamp) {
	return new Date(timestamp).toLocaleDateString('en-US', {
		year: 'numeric',
		month: 'short',
		day: 'numeric'
	});
}

export function formatLong(timestamp): string {
	return new Date(timestamp).toLocaleDateString('en-US', {
		weekday: 'long',
		year: 'numeric',
		month: 'long',
		day: 'numeric'
	});
}

export function formatMillisToDate(millis: number) {
	return new Date(millis).toLocaleDateString('en-US');
}
