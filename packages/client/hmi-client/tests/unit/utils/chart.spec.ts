import { describe, expect, it } from 'vitest';
import { formatSuccessChartData } from '@/services/charts';

const riskResults = {
	S_state: {
		qoi: [0],
		risk: 0
	}
};
describe('chart util tests', () => {
	it('should correctly format success chart data', () => {
		riskResults.S_state.qoi = [0, 0, 0.5, 0.7, 1, 1.2, 2, 3, 3, 4, 5, 5, 5, 5];
		expect(formatSuccessChartData(riskResults, 'S', 2.5, true)).to.deep.equal([
			{ range: '0.0000-1.0000', count: 4, tag: 'in' },
			{ range: '1.0000-2.0000', count: 2, tag: 'in' },
			{ range: '2.0000-3.0000', count: 1, tag: 'out' },
			{ range: '3.0000-4.0000', count: 2, tag: 'out' },
			{ range: '4.0000-5.0000', count: 5, tag: 'out' }
		]);

		expect(formatSuccessChartData(riskResults, 'S', 2.5, false)).to.deep.equal([
			{ range: '0.0000-1.0000', count: 4, tag: 'out' },
			{ range: '1.0000-2.0000', count: 2, tag: 'out' },
			{ range: '2.0000-3.0000', count: 1, tag: 'out' },
			{ range: '3.0000-4.0000', count: 2, tag: 'in' },
			{ range: '4.0000-5.0000', count: 5, tag: 'in' }
		]);
	});

	it('should correctly format success chart data with 1 value', () => {
		riskResults.S_state.qoi = [5];
		expect(formatSuccessChartData(riskResults, 'S', 3, true)).to.deep.equal([
			{ range: '4.0000-6.0000', count: 1, tag: 'out' }
		]);
	});

	it('should correctly format success chart data with all the same values', () => {
		riskResults.S_state.qoi = [5, 5, 5, 5, 5, 5];
		expect(formatSuccessChartData(riskResults, 'S', 3, true)).to.deep.equal([
			{ range: '4.0000-6.0000', count: 6, tag: 'out' }
		]);
	});
});
