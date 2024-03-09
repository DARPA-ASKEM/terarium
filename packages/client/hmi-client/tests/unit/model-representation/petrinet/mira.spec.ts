import * as mmt from '@/examples/mmt.json';
import { describe, expect, it } from 'vitest';
import {
	collapseParameters
	/*
	collapseTemplates,
	collapseTemplates
	*/
} from '@/model-representation/mira/mira';

describe('mira MMT', () => {
	it('parse mira amr', () => {
		const paramsMap = collapseParameters(mmt);

		const rawParametersLength = Object.keys(mmt.parameters).length;
		const collapsedParametersLength = paramsMap.size;

		expect(rawParametersLength).to.eq(32);
		expect(collapsedParametersLength).to.eq(11);
	});
});
