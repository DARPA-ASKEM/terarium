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
	num = parseFloat(num).toString();

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
	let numStr = num.replace(/ /g, '');
	numStr = parseFloat(numStr).toString();
	return numStr;
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
	return numberToNist(num);
}
