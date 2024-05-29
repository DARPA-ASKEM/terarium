import { describe, expect, it } from 'vitest';
import { exponentialToNumber, numberToNist, nistToNumber, displayNumber } from '@/utils/number';

describe('number util tests', () => {
	describe('exponentialToNumber', () => {
		it('should correctly convert very large positive exponents', () => {
			expect(exponentialToNumber('2e+100')).to.equal(`2${'0'.repeat(100)}`);
			expect(exponentialToNumber('3.45e+100')).to.equal(`345${'0'.repeat(98)}`);
		});

		it('should correctly convert very large negative exponents', () => {
			expect(exponentialToNumber('-2e+100')).to.equal(`-2${'0'.repeat(100)}`);
			expect(exponentialToNumber('-3.45e+100')).to.equal(`-345${'0'.repeat(98)}`);
		});

		it('should return the original string for non-exponential numbers', () => {
			expect(exponentialToNumber('456')).to.equal('456');
			expect(exponentialToNumber('4.56')).to.equal('4.56');
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(exponentialToNumber('def')).to.equal('NaN');
			expect(exponentialToNumber('4.56def')).to.equal('4.56');
		});

		// Additional test cases
		it('should correctly convert zero exponent', () => {
			expect(exponentialToNumber('2e0')).to.equal('2');
			expect(exponentialToNumber('-2e0')).to.equal('-2');
		});

		it('should correctly convert one exponent', () => {
			expect(exponentialToNumber('2e1')).to.equal('20');
			expect(exponentialToNumber('-2e1')).to.equal('-20');
		});
	});

	describe('numberToNist', () => {
		it('should correctly format numbers without a decimal part', () => {
			expect(numberToNist('1')).to.eq('1');
			expect(numberToNist('12')).to.eq('12');
			expect(numberToNist('123')).to.eq('123');
			expect(numberToNist('1234567')).to.eq('1 234 567');
		});

		it('should correctly format numbers with a decimal part', () => {
			expect(numberToNist('1.1')).to.eq('1.1');
			expect(numberToNist('12.12')).to.eq('12.12');
			expect(numberToNist('123.123')).to.eq('123.123');
			expect(numberToNist('1234567.1234567')).to.eq('1 234 567.123 456 7');
		});

		it('should correctly format negative numbers', () => {
			expect(numberToNist('-1')).to.eq('-1');
			expect(numberToNist('-12')).to.eq('-12');
			expect(numberToNist('-123')).to.eq('-123');
			expect(numberToNist('-1234567.1234567')).to.eq('-1 234 567.123 456 7');
		});

		it('should return "" for non-numeric strings', () => {
			expect(numberToNist('abc')).to.eq('');
			expect(numberToNist('1.23abc')).to.eq('');
		});
	});

	describe('nistToNumber', () => {
		it('should correctly convert numbers without a decimal part', () => {
			expect(nistToNumber('1 234 567')).to.eq(1234567);
		});

		it('should correctly convert numbers with a decimal part', () => {
			expect(nistToNumber('1 234 567.123 456 7')).to.eq(1234567.1234567);
		});

		it('should correctly convert negative numbers', () => {
			expect(nistToNumber('-1 234 567.123 456 7')).to.eq(-1234567.1234567);
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(Number.isNaN(nistToNumber('abc'))).to.equal(true);
			expect(nistToNumber('1.23abc')).to.eq(1.23);
		});

		it('should correctly handle multiple spaces between groups', () => {
			expect(nistToNumber('1 234.123  4')).to.eq(1234.1234);
		});
	});

	describe('displayNumber', () => {
		it('should correctly format large numbers using scientific notation', () => {
			expect(displayNumber('123456789')).to.eq('1.235e+8');
		});

		it('should correctly format numbers without a decimal part', () => {
			expect(displayNumber('123456')).to.eq('123 456');
		});

		it('should correctly format numbers with a decimal part', () => {
			expect(displayNumber('123.456')).to.eq('123.456');
			expect(displayNumber('1234.56')).to.eq('1234.56');
			expect(displayNumber('12345.678')).to.eq('1.235e+4');
		});

		it('should correctly format numbers with leading zeros', () => {
			expect(displayNumber('0001234.56')).to.eq('1234.56');
		});

		it('should correctly format negative numbers', () => {
			expect(displayNumber('-1234.56')).to.eq('-1 234.56');
		});

		it('should return "" for non-numeric strings', () => {
			expect(displayNumber('abc')).to.eq('');
			expect(displayNumber('1.23abc')).to.eq('1.23');
		});
	});
});
