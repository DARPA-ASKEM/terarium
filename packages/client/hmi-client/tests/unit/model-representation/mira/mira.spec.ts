import * as mmt from '@/examples/mmt.json';
import { describe, expect, it } from 'vitest';
import { collapseParameters, collapseTemplates, createParameterMatrix } from '@/model-representation/mira/mira';

const miraTemplateParams = {
	t1: {
		name: 't1',
		params: ['kappa_0_0', 'beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_middle_aged',
		outcome: 'E_middle_aged',
		controllers: ['I_middle_aged_diagnosed']
	},
	t2: {
		name: 't2',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 't', 'beta_nc', 'kappa_0_1'],
		subject: 'S_middle_aged',
		outcome: 'E_middle_aged',
		controllers: ['I_middle_aged_undiagnosed']
	},
	t3: {
		name: 't3',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 'kappa_1_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_middle_aged',
		outcome: 'E_middle_aged',
		controllers: ['I_old_diagnosed']
	},
	t4: {
		name: 't4',
		params: ['beta_c', 'k_1', 'k_2', 'kappa_1_1', 'N', 't_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_middle_aged',
		outcome: 'E_middle_aged',
		controllers: ['I_old_undiagnosed']
	},
	t5: {
		name: 't5',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 'kappa_2_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_middle_aged',
		outcome: 'E_middle_aged',
		controllers: ['I_young_diagnosed']
	},
	t6: {
		name: 't6',
		params: ['beta_c', 'k_1', 'k_2', 'kappa_2_1', 'N', 't_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_middle_aged',
		outcome: 'E_middle_aged',
		controllers: ['I_young_undiagnosed']
	},
	t7: {
		name: 't7',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 'kappa_3_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_old',
		outcome: 'E_old',
		controllers: ['I_old_diagnosed']
	},
	t8: {
		name: 't8',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 'kappa_3_1', 't', 'beta_nc'],
		subject: 'S_old',
		outcome: 'E_old',
		controllers: ['I_old_undiagnosed']
	},
	t9: {
		name: 't9',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 't', 'kappa_4_0', 'beta_nc'],
		subject: 'S_old',
		outcome: 'E_old',
		controllers: ['I_middle_aged_diagnosed']
	},
	t10: {
		name: 't10',
		params: ['kappa_4_1', 'beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_old',
		outcome: 'E_old',
		controllers: ['I_middle_aged_undiagnosed']
	},
	t11: {
		name: 't11',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 't', 'beta_nc', 'kappa_5_0'],
		subject: 'S_old',
		outcome: 'E_old',
		controllers: ['I_young_diagnosed']
	},
	t12: {
		name: 't12',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 'kappa_5_1', 't', 'beta_nc'],
		subject: 'S_old',
		outcome: 'E_old',
		controllers: ['I_young_undiagnosed']
	},
	t13: {
		name: 't13',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 'kappa_6_0', 't', 'beta_nc'],
		subject: 'S_young',
		outcome: 'E_young',
		controllers: ['I_young_diagnosed']
	},
	t14: {
		name: 't14',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 'kappa_6_1', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_young',
		outcome: 'E_young',
		controllers: ['I_young_undiagnosed']
	},
	t15: {
		name: 't15',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 'kappa_7_0', 't', 'beta_nc'],
		subject: 'S_young',
		outcome: 'E_young',
		controllers: ['I_middle_aged_diagnosed']
	},
	t16: {
		name: 't16',
		params: ['beta_c', 'k_2', 'k_1', 'N', 'kappa_7_1', 't_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_young',
		outcome: 'E_young',
		controllers: ['I_middle_aged_undiagnosed']
	},
	t17: {
		name: 't17',
		params: ['kappa_8_0', 'beta_c', 'k_2', 'k_1', 'N', 't_0', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_young',
		outcome: 'E_young',
		controllers: ['I_old_diagnosed']
	},
	t18: {
		name: 't18',
		params: ['beta_c', 'k_2', 'k_1', 'N', 't_0', 'kappa_8_1', 't_1', 'beta_s', 't', 'beta_nc'],
		subject: 'S_young',
		outcome: 'E_young',
		controllers: ['I_old_undiagnosed']
	},
	t19: {
		name: 't19',
		params: ['delta'],
		subject: 'E_middle_aged',
		outcome: 'I_middle_aged_diagnosed',
		controllers: []
	},
	t20: {
		name: 't20',
		params: ['delta'],
		subject: 'E_middle_aged',
		outcome: 'I_middle_aged_undiagnosed',
		controllers: []
	},
	t21: {
		name: 't21',
		params: ['delta'],
		subject: 'E_old',
		outcome: 'I_old_diagnosed',
		controllers: []
	},
	t22: {
		name: 't22',
		params: ['delta'],
		subject: 'E_old',
		outcome: 'I_old_undiagnosed',
		controllers: []
	},
	t23: {
		name: 't23',
		params: ['delta'],
		subject: 'E_young',
		outcome: 'I_young_diagnosed',
		controllers: []
	},
	t24: {
		name: 't24',
		params: ['delta'],
		subject: 'E_young',
		outcome: 'I_young_undiagnosed',
		controllers: []
	},
	t25: {
		name: 't25',
		params: ['gamma', 'alpha'],
		subject: 'I_middle_aged_diagnosed',
		outcome: 'R_middle_aged',
		controllers: []
	},
	t26: {
		name: 't26',
		params: ['gamma', 'alpha'],
		subject: 'I_middle_aged_undiagnosed',
		outcome: 'R_middle_aged',
		controllers: []
	},
	t27: {
		name: 't27',
		params: ['gamma', 'alpha'],
		subject: 'I_old_diagnosed',
		outcome: 'R_old',
		controllers: []
	},
	t28: {
		name: 't28',
		params: ['gamma', 'alpha'],
		subject: 'I_old_undiagnosed',
		outcome: 'R_old',
		controllers: []
	},
	t29: {
		name: 't29',
		params: ['gamma', 'alpha'],
		subject: 'I_young_diagnosed',
		outcome: 'R_young',
		controllers: []
	},
	t30: {
		name: 't30',
		params: ['gamma', 'alpha'],
		subject: 'I_young_undiagnosed',
		outcome: 'R_young',
		controllers: []
	},
	t31: {
		name: 't31',
		params: ['alpha', 'rho'],
		subject: 'I_middle_aged_diagnosed',
		outcome: 'D_middle_aged',
		controllers: []
	},
	t32: {
		name: 't32',
		params: ['alpha', 'rho'],
		subject: 'I_middle_aged_undiagnosed',
		outcome: 'D_middle_aged',
		controllers: []
	},
	t33: {
		name: 't33',
		params: ['alpha', 'rho'],
		subject: 'I_old_diagnosed',
		outcome: 'D_old',
		controllers: []
	},
	t34: {
		name: 't34',
		params: ['alpha', 'rho'],
		subject: 'I_old_undiagnosed',
		outcome: 'D_old',
		controllers: []
	},
	t35: {
		name: 't35',
		params: ['alpha', 'rho'],
		subject: 'I_young_diagnosed',
		outcome: 'D_young',
		controllers: []
	},
	t36: {
		name: 't36',
		params: ['alpha', 'rho'],
		subject: 'I_young_undiagnosed',
		outcome: 'D_young',
		controllers: []
	},
	t37: {
		name: 't37',
		params: ['epsilon'],
		subject: 'R_middle_aged',
		outcome: 'S_middle_aged',
		controllers: []
	},
	t38: { name: 't38', params: ['epsilon'], subject: 'R_old', outcome: 'S_old', controllers: [] },
	t39: {
		name: 't39',
		params: ['epsilon'],
		subject: 'R_young',
		outcome: 'S_young',
		controllers: []
	},
	t40: {
		name: 't40',
		params: ['p_undiagnosed_diagnosed'],
		subject: 'I_middle_aged_undiagnosed',
		outcome: 'I_middle_aged_diagnosed',
		controllers: []
	},
	t41: {
		name: 't41',
		params: ['p_undiagnosed_diagnosed'],
		subject: 'I_old_undiagnosed',
		outcome: 'I_old_diagnosed',
		controllers: []
	},
	t42: {
		name: 't42',
		params: ['p_undiagnosed_diagnosed'],
		subject: 'I_young_undiagnosed',
		outcome: 'I_young_diagnosed',
		controllers: []
	}
};

describe('mira MMT', () => {
	it('parse mira parameters', () => {
		const paramsMap = collapseParameters(mmt, miraTemplateParams);
		const rawParametersLength = Object.keys(mmt.parameters).length;
		const collapsedParametersLength = paramsMap.size;

		expect(rawParametersLength).to.eq(32);
		expect(collapsedParametersLength).to.eq(15);
	});

	it('parse mira templates', () => {
		const templates = collapseTemplates(mmt).templatesSummary;
		const rawParametersLength = mmt.templates.length;
		const collapsedTemplatesLength = templates.length;

		expect(rawParametersLength).to.eq(42);
		expect(collapsedTemplatesLength).to.eq(6);
	});

	it('matrix test 123', () => {
		const { subjectOutcome } = createParameterMatrix(mmt, miraTemplateParams, 'kappa');
		expect(subjectOutcome.matrix.length).to.eq(3);
		expect(subjectOutcome.matrix[0].length).to.eq(3);
	});
});
