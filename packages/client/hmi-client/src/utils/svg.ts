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

/**
 * Given a path element, create a new point-list from start to end, sampled by number of "steps"
 * */
export const partialPath = (pathEl: SVGPathElement, start: number, end: number, steps: number) => {
	const newPoints: DOMPoint[] = [];
	for (let i = 0; i <= steps; i++) {
		const length = start + ((end - start) * i) / steps;
		const point = pathEl.getPointAtLength(length);
		newPoints.push(point);
	}
	return newPoints;
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

	const w = svgElement.clientWidth || parseFloat(svgElement.getAttribute('width') as string);
	const h = svgElement.clientHeight || parseFloat(svgElement.getAttribute('height') as string);

	const svgString = serializer.serializeToString(svgElement);
	const svgBlob = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
	const url = URL.createObjectURL(svgBlob);

	const img = new Image(w, h);
	const finalImg = new Image(w, h);

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
