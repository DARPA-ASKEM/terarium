export const translate = (x: number, y: number): string => `translate(${x}, ${y})`;

export const pointOnPath = (
	pathNode: SVGPathElement,
	offsetType: string,
	offsetValue: number
): DOMPoint => {
	let pos = 0;
	const total = pathNode.getTotalLength();
	if (offsetType === 'percentage') {
		pos = offsetValue * total;
	} else {
		pos = offsetValue > 0 ? offsetValue : Math.max(0, total + offsetValue);
	}
	const controlPoint = pathNode.getPointAtLength(pos);
	return controlPoint;
};
