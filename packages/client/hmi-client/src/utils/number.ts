/**
 * Converts a number string from its exponential form to normal form.
 *
 * @param {string} num - The number string in exponential form to convert.
 * @returns {string} The number in normal form.
 */
export function exponentialToNumber(num: string): string {
	return parseFloat(num).toLocaleString('fullwide', { useGrouping: false });
}

/**
 * Converts a number string to its NIST form.
 *
 * @param {string} num - The number string to convert.
 * @returns {string} The number in NIST form.
 */
export function numberToNist(num: string) {
	num = num.replace(/\s/g, '');

	if (Number.isNaN(Number(num))) return '';

	// Split the input by decimal point
	let [integerPart, decimalPart] = num.split('.');

	// Format the integer part
	if (integerPart.length > 4 || decimalPart?.length > 4) {
		integerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

		if (num.includes('.') && decimalPart) {
			decimalPart = decimalPart.replace(/(\d{3})/g, '$1 ').trim();
		}
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
export function nistToNumber(numStr: string): number {
	// Remove any spaces from the formatted number
	numStr = numStr.replace(/\s/g, '');
	return parseFloat(numStr);
}

/**
 * Displays a number string in either exponential form or NIST form, depending on its length.
 *
 * @param {string} num - The number string to display.
 * @returns {string} The number in either exponential form or NIST form.
 */
export function displayNumber(num: string): string {
	// Remove negative sign if present
	let digitString = parseFloat(num).toString();
	if (num.startsWith('-')) {
		digitString = digitString.substring(1);
	}

	// Remove decimal point if present
	digitString = digitString.replace('.', '');

	if (digitString.length > 6) return parseFloat(num).toExponential(3);
	return numberToNist(parseFloat(num).toString());
}

/**
 * Scrubs the input string by removing all whitespace and checks if it can be parsed as a number.
 *
 * @param {string} v - The input string to scrub and parse.
 * @returns {boolean} - Returns true if the scrubbed string can be parsed as a number (either in decimal or exponential form), otherwise returns false.
 *
 * The function performs the following checks:
 * 1. If the last character is a decimal point, it returns false.
 * 2. If the scrubbed string can be parsed as a number, it returns true.
 * 3. If the scrubbed string is in exponential form (i.e., contains exactly one 'e' or 'E'), it checks if the part before the 'e' or 'E' can be parsed as a number and if the part after the 'e' or 'E' is an integer. If both conditions are met, it returns true.
 */
export function scrubAndParse(v) {
	// remove all whitespace
	const scrubbed = v.replace(/\s/g, '');

	// check if last digit is a decimal point
	if (scrubbed[scrubbed.length - 1] === '.') return false;

	// check if the string is a number
	if (!Number.isNaN(Number(scrubbed))) return true;

	// check if the string is in exponential form and if there's only one 'e'/'E'
	const parts = scrubbed.split(/[eE]/);
	if (parts.length > 2) return false;

	// check if the string is in exponential form and if the second part is an integer
	if (parts.length === 2) {
		if (Number(parts[0])) {
			if (Number.isInteger(parts[1])) return true;
		}
	} else if (Number(parts[0])) return true;
	return false;
}
