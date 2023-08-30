import * as amr from '@/examples/mira-petri.json';
import { describe, expect, it } from 'vitest';
import { getMiraAMRPresentationData } from '@/model-representation/petrinet/mira-petri';

describe('mira petrinet ', () => {
	it('parse mira amr', () => {
		const res = getMiraAMRPresentationData(amr as any);

		// SEIRD * { y, m, o }  => 15
		// I{y, m, o} * { d, u } => 15 - 3 + 6
		expect(res.stateMatrixData.length).to.eq(18);
		expect(res.transitionMatrixData.length).to.eq(42);
	});

	it('compact graph', () => {
		const res = getMiraAMRPresentationData(amr as any);

		// SEIRD
		expect(res.compactModel.model.states.length).to.eq(5);
	});
	/*
	it('generating factored table', () => {
		const result = getMiraAMRPresentationData(amr as unkown as Model);

		function factorsTable(data: any[]) {
			console.log('!!', data);
			const table: { [key: string]: string[] } = {};
			for (let i = 0; i < data.length; i++) {
				const obj = data[i];
				const keys = Object.keys(obj).filter(d => d !== 'id' && d !== 'base');

				for (let j = 0; j < keys.length; j++) {
					const key = keys[i];
					if (!obj[key]) continue;

					if (!table[key]) table[key] = [];
					table[key].push(obj[key]);
				}
			}
			console.log(table);
			return table;
		}

		factorsTable(result.transitionMatrixData);

		expect(1).to.eq(1);
	})
  */
});
