/**
 * Convert a file to a JSON object
 * @param file file to convert
 * @returns json object if valid, undefined otherwise
 */
export async function fileToJson(file: File): Promise<Record<string, unknown>> {
	return new Promise((resolve, reject) => {
		const reader = new FileReader();
		reader.onload = () => resolve(JSON.parse(reader.result as string));
		reader.onerror = reject;
		reader.readAsText(file);
	});
}

/**
 * Check if a filename ends with a pdf, txt, or md extension
 */
export function isTextFile(filename: string): boolean {
	return /\.(pdf|txt|md)$/.test(filename.trim());
}
