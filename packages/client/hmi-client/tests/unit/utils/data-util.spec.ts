import { pdfNameFromUrl } from '@/utils/data-util';
import { describe, expect, it } from 'vitest';

const pdfUrl = 'https://example.com/documents/document.pdf';
const nonPdfUrl = 'https://example.com/documents/document.notapdf';
const pdfUrlWithParams = 'https://example.com/documents/document.pdf?q=12344e';
describe('data util tests', () => {
	it('gets pdf url name', () => {
		const pdfName = pdfNameFromUrl(pdfUrl);

		expect(pdfName).toBe('document.pdf');
	});

	it('gets pdf url name with params', () => {
		const pdfName = pdfNameFromUrl(pdfUrlWithParams);

		expect(pdfName).toBe('document.pdf');
	});

	it('returns null on bad url', () => {
		const pdfName = pdfNameFromUrl(nonPdfUrl);

		expect(pdfName).toBeNull();
	});
});
