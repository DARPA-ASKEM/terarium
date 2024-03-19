import _ from 'lodash';
import type { Model } from '@/types/Types';

/**
 * Note "id" and "base" used for building the compact graph, they should not be used as strata dimensions
 */
export const extractNestedStratas = (matrixData: any[], stratas: string[]) => {
	if (stratas.length === 0) {
		return {};
	}
	const strataKey = stratas[0];
	let result: any = _.groupBy(matrixData, stratas[0]);

	const nextStratas = _.clone(stratas);
	nextStratas.shift();

	// Bake in strata-type
	if (!_.isEmpty(result)) {
		result._key = strataKey;
	}

	Object.keys(result).forEach((key) => {
		if (key === '_key') return;

		if (key === 'undefined') {
			// No result, skip and start on the next
			result = extractNestedStratas(matrixData, nextStratas);
		} else {
			// Go down to the next depth
			result[key] = extractNestedStratas(result[key], nextStratas);
		}
	});

	return result;
};

/**
 * Given an amr, find the unstratified/root parameters.
 *
 * This requires some heuristics go backwards, may not work all the time.
 * This works poorly if the parameter ids starts off with underscores.
 *
 * For example "beta_1_1", "beta_1_2" will collapse into "beta": ["beta_1_1", "beta_1_2"]
 */
export const getUnstratifiedInitials = (amr: Model) => {
	const initials = amr.semantics?.ode.initials || [];
	const map = new Map<string, string[]>();
	initials.forEach((i) => {
		const rootName = _.first(i.target.split('_')) as string;
		if (map.has(rootName)) {
			map.get(rootName)?.push(i.target);
		} else {
			map.set(rootName, [i.target]);
		}
	});
	return map;
};
