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
