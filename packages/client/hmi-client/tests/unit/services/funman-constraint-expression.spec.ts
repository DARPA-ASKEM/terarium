import { ConstraintType, Constraint } from '@/components/workflow/ops/funman/funman-operation';
import { generateConstraintExpression } from '@/services/models/funman-service';
import { describe, expect, it } from 'vitest';

describe('generateConstraintExpression', () => {
	it('should generate a valid constraint expression for greater than or equal to', () => {
		const constraint = {
			name: 'Constraint',
			isActive: true,
			timepoints: {
				lb: 0,
				ub: 100
			},
			interval: {
				lb: 0,
				ub: 100
			},
			constraint: Constraint.State,
			variables: ['I', 'R'],
			constraintType: ConstraintType.GreaterThanOrEqualTo,
			weights: [1, 1]
		};
		const expected =
			'I(t)\\geq0 \\ \\forall \\ t \\in [0, 100] \\newline R(t)\\geq0 \\ \\forall \\ t \\in [0, 100] \\newline ';
		const result = generateConstraintExpression(constraint);
		expect(result).toEqual(expected);
	});

	it('should generate a valid constraint expression for less than or equal to', () => {
		const constraint = {
			name: 'Constraint',
			isActive: true,
			timepoints: {
				lb: 0,
				ub: 100
			},
			interval: {
				lb: 0,
				ub: 100
			},
			constraint: Constraint.State,
			variables: ['I', 'R'],
			constraintType: ConstraintType.LessThanOrEqualTo,
			weights: [1, 1]
		};
		const expected =
			'I(t)\\leq100 \\ \\forall \\ t \\in [0, 100] \\newline R(t)\\leq100 \\ \\forall \\ t \\in [0, 100] \\newline ';
		const result = generateConstraintExpression(constraint);
		expect(result).toEqual(expected);
	});

	it('should generate a valid constraint expression for less than', () => {
		const constraint = {
			name: 'Constraint',
			isActive: true,
			timepoints: {
				lb: 0,
				ub: 100
			},
			interval: {
				lb: 0,
				ub: 100
			},
			constraint: Constraint.State,
			variables: ['I', 'R'],
			constraintType: ConstraintType.LessThan,
			weights: [1, 1]
		};
		const expected =
			'I(t)<100 \\ \\forall \\ t \\in [0, 100] \\newline R(t)<100 \\ \\forall \\ t \\in [0, 100] \\newline ';
		const result = generateConstraintExpression(constraint);
		expect(result).toEqual(expected);
	});

	it('should generate a valid constraint expression for increasing', () => {
		const constraint = {
			name: 'Constraint',
			isActive: true,
			timepoints: {
				lb: 0,
				ub: 100
			},
			interval: {
				lb: 0,
				ub: 100
			},
			constraint: Constraint.State,
			variables: ['S', 'I', 'R'],
			weights: [1, 1, 1],
			constraintType: ConstraintType.Increasing
		};
		const expected =
			'd/dt S(t)\\geq0 \\ \\forall \\ t \\in [0, 100] \\newline d/dt I(t)\\geq0 \\ \\forall \\ t \\in [0, 100] \\newline d/dt R(t)\\geq0 \\ \\forall \\ t \\in [0, 100] \\newline ';
		const result = generateConstraintExpression(constraint);
		expect(result).toEqual(expected);
	});

	it('should generate a valid constraint expression for decreasing', () => {
		const constraint = {
			name: 'Constraint',
			isActive: true,
			timepoints: {
				lb: 0,
				ub: 100
			},
			interval: {
				lb: 0,
				ub: 100
			},
			constraint: Constraint.State,
			variables: ['S', 'I', 'R'],
			weights: [1, 1, 1],
			constraintType: ConstraintType.Decreasing
		};
		const expected =
			'd/dt S(t)\\leq0 \\ \\forall \\ t \\in [0, 100] \\newline d/dt I(t)\\leq0 \\ \\forall \\ t \\in [0, 100] \\newline d/dt R(t)\\leq0 \\ \\forall \\ t \\in [0, 100] \\newline ';
		const result = generateConstraintExpression(constraint);
		expect(result).toEqual(expected);
	});
});
