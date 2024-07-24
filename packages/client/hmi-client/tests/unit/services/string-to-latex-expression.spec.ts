import { describe, expect, it } from 'vitest';
import { stringToLatexExpression } from '@/services/model';

describe('stringToLatexExpression', () => {
	it('should wrap variables after underscores in {} and escape subsequent underscores', () => {
		const expression = 'x_variable';
		const expected = 'x_{variable}';
		const result = stringToLatexExpression(expression);
		expect(result).toEqual(expected);
	});

	it('should convert ^ to LaTeX superscript notation', () => {
		const expression = 'x^2';
		const expected = 'x^{2}';
		const result = stringToLatexExpression(expression);
		expect(result).toEqual(expected);
	});

	it('should detect and convert fractions a/b to \\frac{a}{b}', () => {
		const expression = '1/2';
		const expected = '\\frac{1}{2}';
		const result = stringToLatexExpression(expression);
		expect(result).toEqual(expected);
	});

	it('should handle the expression I_unvaccinated^2*NPI_mult*S_unvaccinated*beta/N', () => {
		const expression = 'I_unvaccinated^2*NPI_mult*S_unvaccinated*beta/N';
		const expected = 'I_{unvaccinated}^{2}*NPI_{mult}*S_{unvaccinated}*\\frac{beta}{N}';
		const result = stringToLatexExpression(expression);
		expect(result).toEqual(expected);
	});
});
