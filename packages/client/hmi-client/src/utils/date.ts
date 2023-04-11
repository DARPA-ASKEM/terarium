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

export function formatLocalTime(timestamp): string {
	return new Date(timestamp).toLocaleTimeString('en-US', { timeStyle: 'short' });
}

export function formatMillisToDate(millis: number) {
	return new Date(millis).toLocaleDateString('en-US');
}

export function isDateToday(timestamp): boolean {
	const today = new Date();
	const someDate = new Date(timestamp);
	return (
		someDate.getDate() === today.getDate() &&
		someDate.getMonth() === today.getMonth() &&
		someDate.getFullYear() === today.getFullYear()
	);
}
