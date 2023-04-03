import API from '@/api/api';
import { logger } from '@/utils/logger';
import { toQueryString } from '@/utils/query-string';

export async function generatePdfDownloadLink(doi: string) {
	if (!doi) return null;

	const query = { doi };
	const URL = `/download?${toQueryString(query)}`;

	try {
		const response = await API.get(URL, { responseType: 'arraybuffer' });
		const blob = new Blob([response?.data], { type: 'application/pdf' });
		const pdfLink = window.URL.createObjectURL(blob);
		return pdfLink ?? null;
	} catch (error) {
		logger.error(`Error: Unable to download pdf for doi ${doi}: ${error}`);
		return null;
	}
}
