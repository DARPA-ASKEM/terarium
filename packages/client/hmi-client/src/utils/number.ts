/**
 * Cleans a number string by removing non-numeric characters.
 *
 * @param {string} num - The number string to clean.
 * @returns {string} The cleaned number string.
 */
export function cleanNumberString(num: string): string {
	// remove spaces
	let numStr = num.replace(/ /g, '');

	// Check if the string is a valid number
	if (Number.isNaN(Number(numStr))) return 'NaN';

	// Handle negative numbers
	const isNegative = numStr.startsWith('-');
	if (isNegative) numStr = numStr.slice(1);

	// Split the number into integer and decimal parts
	let [integerPart, decimalPart] = numStr.split('.');

	// Remove leading zeros from the integer part
	integerPart = integerPart.replace(/^0+/, '') || '0';

	// Remove trailing zeros from the decimal part, if it exists
	if (decimalPart) {
		decimalPart = decimalPart.replace(/0+$/, '');
	}

	// Combine the integer and decimal parts back into a number
	numStr = decimalPart ? `${integerPart}.${decimalPart}` : integerPart;

	// Add the '-' sign back if the number is negative
	if (isNegative && numStr !== '0') numStr = `-${numStr}`;

	return numStr;
}

/**
 * Converts a number string to its exponential form.
 *
 * @param {string} num - The number string to convert.
 * @param {number} precision - The number of digits to appear after the decimal point in the resulting string.
 * @returns {string} The number in exponential form.
 */
export function numberToExponential(num: string, precision: number = 3): string {
	if (Number.isNaN(Number(num))) return Number.NaN.toString();
	return Number(num).toExponential(precision);
}

/**
 * Converts a number string from its exponential form to normal form.
 *
 * @param {string} num - The number string in exponential form to convert.
 * @returns {string} The number in normal form.
 */
export function exponentialToNumber(num: string): string {
	// Split the number into base and exponent parts
	const [base, exponent] = num.split(/[eE]/);
	const baseStr = cleanNumberString(base);

	// If there's no exponent, return the base
	if (!exponent) return baseStr;

	// Determine the sign of the number
	const sign = Number(num) < 0 ? '-' : '';

	// Remove the decimal point and negative sign from the base
	const str = baseStr.replace('.', '').replace('-', '');

	// Convert the exponent to a number and increment it
	let mag = Number(exponent) + 1;

	// If the magnitude is less than or equal to zero, move the decimal point to the left
	if (mag <= 0) {
		let result = `${sign}0.`;
		while (mag++) result += '0';
		return result + str;
	}

	// If the magnitude is greater than zero, move the decimal point to the right
	mag -= str.length;
	mag = Math.abs(mag);
	const zeros = '0'.repeat(mag);
	console.log(sign + str + zeros);
	return sign + str + zeros;
}

/**
 * Converts a number string to its NIST form.
 *
 * @param {string} num - The number string to convert.
 * @returns {string} The number in NIST form.
 */
export function numberToNist(num: string) {
	if (Number.isNaN(Number(num))) return Number.NaN.toString();
	num = cleanNumberString(num);

	// Split the input by decimal point
	let [integerPart, decimalPart] = num.split('.');

	// Format the integer part
	integerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

	if (num.includes('.') && decimalPart) {
		decimalPart = decimalPart.replace(/(\d{3})/g, '$1 ').trim();
	}

	// Construct the formatted number
	let formattedNumber = integerPart;
	if (decimalPart) {
		formattedNumber += `.${decimalPart}`;
	}

	return formattedNumber;
}

/**
 * Converts a number string from its NIST form to normal form.
 *
 * @param {string} num - The number string in NIST form to convert.
 * @returns {string} The number in normal form.
 */
export function nistToNumber(num: string): string {
	// Remove any spaces from the formatted number
	const numStr = cleanNumberString(num);
	if (Number.isNaN(Number(numStr))) return Number.NaN.toString();

	// Check if the number has a decimal point
	if (numStr.includes('.')) {
		const [integerPart, decimalPart] = numStr.split('.');
		// Remove spaces from the integer part and combine with the decimal part
		return `${integerPart.replace(/ /g, '')}.${decimalPart}`;
	}
	// Remove spaces from the integer part
	return numStr.replace(/ /g, '');
}

/**
 * Displays a number string in either exponential form or NIST form, depending on its length.
 *
 * @param {string} num - The number string to display.
 * @returns {string} The number in either exponential form or NIST form.
 */
export function displayNumber(num: string): string {
	const numStr = cleanNumberString(num);
	if (Number.isNaN(Number(numStr))) return Number.NaN.toString();

	// Remove negative sign if present
	let numDigits = numStr;
	if (numStr.startsWith('-')) {
		numDigits = numDigits.substring(1);
	}

	// Remove decimal point if present
	numDigits = numDigits.replace('.', '');

	if (numDigits.length > 6) return numberToExponential(numStr);
	return numberToNist(numStr);
}
