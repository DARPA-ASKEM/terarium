import { describe, expect, it } from 'vitest';
import {
	numberToExponential,
	exponentialToNumber,
	numberToNist,
	nistToNumber,
	displayNumber,
	cleanNumberString
} from '@/utils/number';

describe('number util tests', () => {
	describe('cleanNumberString', () => {
		it('should correctly handle strings with leading zeros', () => {
			expect(cleanNumberString('0001.0000')).to.eq('1');
			expect(cleanNumberString('-0001.0000')).to.eq('-1');
			expect(cleanNumberString('0000')).to.eq('0');
			expect(cleanNumberString('-0000')).to.eq('0');
			expect(cleanNumberString('0001')).to.eq('1');
			expect(cleanNumberString('-0001')).to.eq('-1');
		});

		it('should correctly handle strings with trailing zeros', () => {
			expect(cleanNumberString('1.0000')).to.eq('1');
			expect(cleanNumberString('-1.0000')).to.eq('-1');
		});

		it('should correctly handle strings with leading and trailing zeros', () => {
			expect(cleanNumberString('0101.01010')).to.eq('101.0101');
			expect(cleanNumberString('000.1000')).to.eq('0.1');
			expect(cleanNumberString('-000.1000')).to.eq('-0.1');
		});

		it('should correctly handle zero', () => {
			expect(cleanNumberString('0')).to.eq('0');
			expect(cleanNumberString('-0')).to.eq('0');
		});

		it('should correctly handle negative numbers', () => {
			expect(cleanNumberString('-00001.1000')).to.eq('-1.1');
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(cleanNumberString('asdsdfd')).to.eq('NaN');
		});

		it('should correctly handle strings with leading and trailing spaces', () => {
			expect(cleanNumberString(' 1')).to.eq('1');
			expect(cleanNumberString('1 ')).to.eq('1');
			expect(cleanNumberString(' 1 ')).to.eq('1');
			expect(cleanNumberString(' -1 ')).to.eq('-1');
		});

		it('should correctly handle strings with internal spaces', () => {
			expect(cleanNumberString('1 0 1')).to.eq('101');
			expect(cleanNumberString('-1 0 1')).to.eq('-101');
		});

		it('should correctly handle strings with multiple decimal points', () => {
			expect(cleanNumberString('1.0.1')).to.eq('NaN');
			expect(cleanNumberString('-1.0.1')).to.eq('NaN');
		});

		it('should correctly handle strings with other non-numeric characters', () => {
			expect(cleanNumberString('1a')).to.eq('NaN');
			expect(cleanNumberString('a1')).to.eq('NaN');
			expect(cleanNumberString('1a1')).to.eq('NaN');
			expect(cleanNumberString('-1a1')).to.eq('NaN');
		});
	});

	describe('numberToExponential', () => {
		it('should correctly handle positive numbers', () => {
			expect(numberToExponential('123456789', 2)).to.eq('1.23e+8');
			expect(numberToExponential('3')).to.eq('3.000e+0');
			expect(numberToExponential('0003')).to.eq('3.000e+0');
			expect(numberToExponential(Number.MAX_VALUE.toString())).to.eq('1.798e+308');
		});

		it('should correctly handle small positive numbers', () => {
			expect(numberToExponential('0.3')).to.eq('3.000e-1');
			expect(numberToExponential('0.00000000012349')).to.eq('1.235e-10');
		});

		it('should correctly handle zero', () => {
			expect(numberToExponential('0')).to.eq('0.000e+0');
		});

		it('should correctly handle negative numbers', () => {
			expect(numberToExponential('-123456789')).to.eq('-1.235e+8');
			expect(numberToExponential('-123456789', 2)).to.eq('-1.23e+8');
			expect(numberToExponential('-3')).to.eq('-3.000e+0');
			expect(numberToExponential('-0003')).to.eq('-3.000e+0');
		});

		it('should correctly handle small negative numbers', () => {
			expect(numberToExponential('-0.3')).to.eq('-3.000e-1');
			expect(numberToExponential(`-0.${'0'.repeat(66)}100051`)).to.eq('-1.001e-67');
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(numberToExponential('asdsdfd')).to.eq('NaN');
		});

		// Additional test cases
		it('should correctly handle numbers with no decimal part', () => {
			expect(numberToExponential('100')).to.eq('1.000e+2');
			expect(numberToExponential('-100')).to.eq('-1.000e+2');
		});

		it('should correctly handle numbers with a large decimal part', () => {
			expect(numberToExponential('1.12345678901234567890')).to.eq('1.123e+0');
			expect(numberToExponential('-1.12345678901234567890')).to.eq('-1.123e+0');
		});
	});

	describe('exponentialToNumber', () => {
		it('should correctly convert very large positive exponents', () => {
			expect(exponentialToNumber('2e+100')).to.equal(`2${'0'.repeat(100)}`);
			expect(exponentialToNumber('3.45e+100')).to.equal(`345${'0'.repeat(98)}`);
		});

		it('should correctly convert very small positive exponents', () => {
			expect(exponentialToNumber('2e-100')).to.equal(`0.${'0'.repeat(99)}2`);
			expect(exponentialToNumber('3.45e-100')).to.equal(`0.${'0'.repeat(99)}345`);
		});

		it('should correctly convert very large negative exponents', () => {
			expect(exponentialToNumber('-2e+100')).to.equal(`-2${'0'.repeat(100)}`);
			expect(exponentialToNumber('-3.45e+100')).to.equal(`-345${'0'.repeat(98)}`);
		});

		it('should correctly convert very small negative exponents', () => {
			expect(exponentialToNumber('-2e-100')).to.equal(`-0.${'0'.repeat(99)}2`);
			expect(exponentialToNumber('-3.45e-100')).to.equal(`-0.${'0'.repeat(99)}345`);
		});

		it('should return the original string for non-exponential numbers', () => {
			expect(exponentialToNumber('456')).to.equal('456');
			expect(exponentialToNumber('4.56')).to.equal('4.56');
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(exponentialToNumber('def')).to.equal('NaN');
			expect(exponentialToNumber('4.56def')).to.equal('NaN');
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
			expect(numberToNist('1234')).to.eq('1 234');
			expect(numberToNist('12345')).to.eq('12 345');
			expect(numberToNist('123456')).to.eq('123 456');
			expect(numberToNist('1234567')).to.eq('1 234 567');
		});

		it('should correctly format numbers with a decimal part', () => {
			expect(numberToNist('1.1')).to.eq('1.1');
			expect(numberToNist('12.12')).to.eq('12.12');
			expect(numberToNist('123.123')).to.eq('123.123');
			expect(numberToNist('1234.1234')).to.eq('1 234.123 4');
			expect(numberToNist('12345.12345')).to.eq('12 345.123 45');
			expect(numberToNist('123456.123456')).to.eq('123 456.123 456');
			expect(numberToNist('1234567.1234567')).to.eq('1 234 567.123 456 7');
		});

		it('should correctly format negative numbers', () => {
			expect(numberToNist('-1')).to.eq('-1');
			expect(numberToNist('-12')).to.eq('-12');
			expect(numberToNist('-123')).to.eq('-123');
			expect(numberToNist('-1234')).to.eq('-1 234');
			expect(numberToNist('-12345')).to.eq('-12 345');
			expect(numberToNist('-123456')).to.eq('-123 456');
			expect(numberToNist('-1234567')).to.eq('-1 234 567');
			expect(numberToNist('-1234567.1234567')).to.eq('-1 234 567.123 456 7');
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(numberToNist('abc')).to.eq('NaN');
			expect(numberToNist('1.23abc')).to.eq('NaN');
		});
	});

	describe('nistToNumber', () => {
		it('should correctly convert numbers without a decimal part', () => {
			expect(nistToNumber('1')).to.eq('1');
			expect(nistToNumber('12')).to.eq('12');
			expect(nistToNumber('123')).to.eq('123');
			expect(nistToNumber('1 234')).to.eq('1234');
			expect(nistToNumber('12 345')).to.eq('12345');
			expect(nistToNumber('123 456')).to.eq('123456');
			expect(nistToNumber('1 234 567')).to.eq('1234567');
		});

		it('should correctly convert numbers with a decimal part', () => {
			expect(nistToNumber('1.1')).to.eq('1.1');
			expect(nistToNumber('12.12')).to.eq('12.12');
			expect(nistToNumber('123.123')).to.eq('123.123');
			expect(nistToNumber('1 234.123 4')).to.eq('1234.1234');
			expect(nistToNumber('12 345.123 45')).to.eq('12345.12345');
			expect(nistToNumber('123 456.123 456')).to.eq('123456.123456');
			expect(nistToNumber('1 234 567.123 456 7')).to.eq('1234567.1234567');
		});

		it('should correctly convert negative numbers', () => {
			expect(nistToNumber('-1')).to.eq('-1');
			expect(nistToNumber('-12')).to.eq('-12');
			expect(nistToNumber('-123')).to.eq('-123');
			expect(nistToNumber('-1 234')).to.eq('-1234');
			expect(nistToNumber('-12 345')).to.eq('-12345');
			expect(nistToNumber('-123 456')).to.eq('-123456');
			expect(nistToNumber('-1 234 567')).to.eq('-1234567');
			expect(nistToNumber('-1 234 567.123 456 7')).to.eq('-1234567.1234567');
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(nistToNumber('abc')).to.eq('NaN');
			expect(nistToNumber('1.23abc')).to.eq('NaN');
		});

		it('should correctly handle leading and trailing spaces', () => {
			expect(nistToNumber(' 1 234 ')).to.eq('1234');
			expect(nistToNumber(' 1 234.123 4 ')).to.eq('1234.1234');
		});

		it('should correctly handle multiple spaces between groups', () => {
			expect(nistToNumber('1  234')).to.eq('1234');
			expect(nistToNumber('1 234.123  4')).to.eq('1234.1234');
		});
	});

	describe('displayNumber', () => {
		it('should correctly format large numbers using scientific notation', () => {
			expect(displayNumber('123456789')).to.eq('1.235e+8');
			expect(displayNumber('9876543210')).to.eq('9.877e+9');
		});

		it('should correctly format numbers without a decimal part', () => {
			expect(displayNumber('123456')).to.eq('123 456');
			expect(displayNumber('123')).to.eq('123');
			expect(displayNumber('1234')).to.eq('1 234');
		});

		it('should correctly format numbers with a decimal part', () => {
			expect(displayNumber('123.456')).to.eq('123.456');
			expect(displayNumber('1234.56')).to.eq('1 234.56');
			expect(displayNumber('12345.678')).to.eq('1.235e+4');
		});

		it('should correctly format numbers with leading zeros', () => {
			expect(displayNumber('0001.1')).to.eq('1.1');
			expect(displayNumber('000123')).to.eq('123');
			expect(displayNumber('0001234.56')).to.eq('1 234.56');
		});

		// Additional test cases
		it('should correctly format negative numbers', () => {
			expect(displayNumber('-123456')).to.eq('-123 456');
			expect(displayNumber('-123.456')).to.eq('-123.456');
			expect(displayNumber('-1234.56')).to.eq('-1 234.56');
		});

		it('should return "NaN" for non-numeric strings', () => {
			expect(displayNumber('abc')).to.eq('NaN');
			expect(displayNumber('1.23abc')).to.eq('NaN');
		});
	});
});
