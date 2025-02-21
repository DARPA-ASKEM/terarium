import _ from 'lodash';

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
