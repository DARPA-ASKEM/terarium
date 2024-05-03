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
export function nistToNumber(num: string): string {
	// Remove any spaces from the formatted number
	let numStr = num.replace(/\s/g, '');
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

/**
 * Checks if a number string is in NIST form.
 *
 * @param {string} num - The number string to check.
 * @returns {boolean} True if the number is in NIST form, false otherwise.
 */
export function isNistNumber(num: string): boolean {
	// If the input is blank, return true
	if (num.trim() === '') {
		return true;
	}

	if (num.startsWith('-')) {
		num = num.substring(1);
	}

	// Check if the string only contains digits, 'e', '+', '-', '.'
	if (!/^[0-9e+-. ]+$/.test(num)) {
		return false;
	}

	// Check if there is more than one decimal point
	if ((num.match(/\./g) || []).length > 1) {
		return false;
	}

	// Split the input by decimal point
	const [integerPart, decimalPart] = num.split('.');

	// Check the integer part
	if (integerPart.length > 4 || decimalPart?.length > 4) {
		if (integerPart && !/^(\d{1,3}( \d{3})*)?$/.test(integerPart)) {
			return false;
		}

		// Check the decimal part
		if (decimalPart && !/^(\d{3} )*\d{1,3}$/.test(decimalPart)) {
			return false;
		}
	}

	return true;
}

export function maskToNistNumber(input: string): string {
	// Remove any characters that are not digits, '.', '+', '-', or 'e'
	let maskedInput = input.replace(/[^0-9e+-.]/g, '');

	// If '.' is the first character, replace it with '0.'
	if (maskedInput.startsWith('.')) {
		maskedInput = `0${maskedInput}`;
	}
	// Ensure '.e' is replaced with '.'
	maskedInput = maskedInput.replace(/\.e/g, '.');

	// Ensure '+' is only directly after 'e'
	maskedInput = maskedInput.replace(/([^e])\+/g, '$1');

	// Ensure '-' is only the first character or directly after 'e'
	maskedInput = maskedInput.replace(/(?<!^)(?<!e)-/g, '');

	// Ensure there is only one decimal point
	const decimalIndex = maskedInput.indexOf('.');
	if (decimalIndex !== -1) {
		maskedInput =
			maskedInput.substring(0, decimalIndex + 1) +
			maskedInput.substring(decimalIndex + 1).replace(/\./g, '');
	}

	// Split the input by 'e' if it's present
	let [base, exponent] = maskedInput.split('e');

	// If the base part has a decimal point, ensure that there are no more than two decimal places
	if (base.includes('.')) {
		let [integerPart, decimalPart] = base.split('.');

		if (integerPart.length > 4 || decimalPart?.length > 4) {
			// Group the integer part in groups of 3 digits
			integerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

			// Group the decimal part in groups of 3 digits
			decimalPart = decimalPart.replace(/(\d{3})(?=\d)/g, '$1 ');
		}

		base = `${integerPart}.${decimalPart}`;
	} else if (base.length > 4) {
		// If there's no decimal point, just group the integer part in groups of 3 digits
		base = base.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
	}

	// If an exponent part is present, remove any decimal points from it
	if (exponent !== undefined) {
		exponent = exponent.replace(/\./g, '');
		maskedInput = `${base}e${exponent}`;
	} else {
		maskedInput = base;
	}

	// If the input string represents an exponential number, convert it to a number and then to a string in exponential notation
	if (maskedInput.includes('e')) {
		const num = Number(maskedInput);
		if (!Number.isNaN(num)) {
			maskedInput = num.toExponential();
		}
	}

	return maskedInput;
}
