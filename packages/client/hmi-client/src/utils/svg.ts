/* Collection of SVG utility functions */

/**
 * Given a svg:path element, calculate point position relative to percentage.
 * Assume percent is within [0, 1]
 */
export const pointOnPath = (pathEl: SVGPathElement, percent: number) => {
	const total = pathEl.getTotalLength();
	const point = pathEl.getPointAtLength(total * percent);
	return point;
};

// Note: Being evaluated for now, not in use
// @ts-ignore
// eslint-disable-next-line
const embedExternalStyles = (svgElement: SVGElement) => {
	const styleSheets = Array.from(document.styleSheets);
	const styleContent = styleSheets
		.map((sheet) => {
			try {
				return Array.from(sheet.cssRules || [])
					.map((rule) => rule.cssText)
					.join('\n');
			} catch (e) {
				return '';
			}
		})
		.join('\n');
	const styleElement = document.createElement('style');
	styleElement.textContent = styleContent;
	svgElement.insertBefore(styleElement, svgElement.firstChild);
	return svgElement;
};

/**
 * Converts an SVG to Canvas to Image
 * Note: css-variables doesn't seem to work very well here
 * */
export const svgToImage = (svgElement: SVGElement): Promise<HTMLImageElement> => {
	const serializer = new XMLSerializer();
	// embedExternalStyles(svgElement);

	const svgString = serializer.serializeToString(svgElement);
	const svgBlob = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
	const url = URL.createObjectURL(svgBlob);
	const img = new Image(svgElement.clientWidth, svgElement.clientHeight);
	const finalImg = new Image(svgElement.clientWidth, svgElement.clientHeight);

	return new Promise((resolve, reject) => {
		img.addEventListener('load', (e: any) => {
			URL.revokeObjectURL(e.target.src);
			const canvas = document.createElement('canvas');
			canvas.width = img.width;
			canvas.height = img.height;
			const ctx = canvas.getContext('2d');
			ctx?.drawImage(img, 0, 0);
			finalImg.src = canvas.toDataURL('image/png');
			resolve(finalImg);
		});
		img.onerror = () => {
			URL.revokeObjectURL(url);
			reject(new Error('Failed to load intermediate image'));
		};
		img.src = url;
	});
};
