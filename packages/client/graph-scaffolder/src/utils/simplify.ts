import { IPoint } from '../types';

const simplifyDPStep = (
	points: IPoint[],
	first: number,
	last: number,
	sqTolerance: number,
	simplified: IPoint[]
) => {
	const sqSegmentDistance = (p: IPoint, p1: IPoint, p2: IPoint) => {
		const sqDifference = (a: number, b: number) => (a - b) * (a - b);
		const sqDistance = (a: IPoint, b: IPoint) => sqDifference(a.x, a.x) + sqDifference(b.y, b.y);

		if (p1.x === p2.x && p1.y === p2.y) return sqDistance(p, p1);

		const t = ((p.x - p1.x) * (p2.x - p1.x) + (p.y - p1.y) * (p2.y - p1.y)) / sqDistance(p2, p1);
		if (t > 1) return sqDistance(p, p2);
		if (t > 0) return sqDistance(p, { x: p1.x + (p2.x - p1.x) * t, y: p1.y + (p2.y - p1.y) * t });
		return sqDistance(p, p1);
	};

	let maxSqDist = sqTolerance;
	let index = -1;

	for (let i = first + 1; i < last; i++) {
		const sqDist = sqSegmentDistance(points[i], points[first], points[last]);
		if (sqDist > maxSqDist) {
			index = i;
			maxSqDist = sqDist;
		}
	}

	if (maxSqDist > sqTolerance) {
		if (index - first > 1) simplifyDPStep(points, first, index, sqTolerance, simplified);
		simplified.push(points[index]);
		if (last - index > 1) simplifyDPStep(points, index, last, sqTolerance, simplified);
	}
};

const simplifyDouglasPeucker = (points: IPoint[], sqTolerance: number) => {
	const last = points.length - 1;
	const simplified = [points[0]];
	simplifyDPStep(points, 0, last, sqTolerance, simplified);
	simplified.push(points[last]);
	return simplified;
};

/**
 * Ramer-Douglas-Peucker shape simplification algorithm
 * https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 */
const simplifyPath = (points: IPoint[], tolerance = 8.0): IPoint[] => {
	if (points.length <= 2) return points;
	return simplifyDouglasPeucker(points, tolerance * tolerance);
};

export default simplifyPath;

// Add ponts along a path
// export const addPoints = (path, minDistance = 10.0) => {
//   const distance = (a, b) => Math.hypot(a.x - b.x, a.y - b.y);
//   const pointsalong = (a, b, n) => Array.from(Array(n + 2), (_, i) => ({ x: (b.x - a.x) * (i / (n + 1)) + a.x, y: (b.y - a.y) * (i / (n + 1)) + a.y }));
//   const slidingwindow = (arr, n, func) => arr.slice(0, arr.length - n + 1).map((_, i) => func(arr.slice(i, i + n)));
//
//   return [].concat(...slidingwindow(path, 2, (pair) => {
//     if (distance(pair[0], pair[1]) < minDistance) {
//       return [pair[0]];
//     } else {
//       return pointsalong(pair[0], pair[1], Math.floor(distance(pair[0], pair[1]) / minDistance));
//     };
//   }), [path[path.length - 1]]);
// };
