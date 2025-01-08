/* Collection of MATH/MATH-EDITOR utility functions */
import { logger } from '@/utils/logger';
import Mathml2latex from 'mathml-to-latex';
import { cloneDeep } from 'lodash';

export enum MathEditorModes {
	LIVE = 'mathLIVE',
	KATEX = 'katex'
}

// simple check to test if it's a mathml string based on opening and closing tags
export const isMathML = (mathMLString: string): boolean => {
	const regex = /<math[>\s][\s\S]*?<\/math>/s;
	return regex.test(mathMLString);
};

/**
 * Checks if a string is a mathML equation and returns its latex
 */
export const getLatexFromMathML = (mathMLString: string): string => {
	try {
		return Mathml2latex.convert(`<math display="block">${mathMLString}</math>`);
	} catch (error) {
		logger.error(error, { showToast: false, silent: true });
		return '';
	}
};

/**
 * removes redudant <mrow> tags
 */
const removeRedundantMrow = (element: Element | null): void => {
	if (element) {
		const mrowElements = Array.from(element.querySelectorAll('mrow'));
		mrowElements.forEach((mrowElement) => {
			const parentElement = mrowElement.parentElement;
			if (
				mrowElement.childElementCount === 1 &&
				parentElement &&
				!(parentElement.tagName === 'mfrac' && parentElement.children[0] === mrowElement)
			) {
				while (mrowElement.children.length > 0) {
					parentElement.insertBefore(mrowElement.children[0], mrowElement);
				}
				console.log(mrowElement);
				mrowElement.remove();
			}
		});
	}
};

/**
 * removes empty <mo> items
 */
export const removeInvisibleTimes = (element: Element | null): void => {
	if (element) {
		Array.from(element.children).forEach((child) => {
			if (child.tagName === 'mo' && child.textContent === '⁢') {
				element.removeChild(child);
			} else {
				removeInvisibleTimes(child);
			}
		});
	}
};

/**
 * removes any parentheses found in the equations
 */
export function removeParentheses(element: Element | null): void {
	if (element) {
		const moElements = Array.from(element.querySelectorAll('mo'));
		moElements.forEach((moElement) => {
			if (moElement.textContent === '(' || moElement.textContent === ')') {
				moElement.remove();
			}
		});
	}
}

/**
 * removes the "t"/time element in the equations
 */
export function removeTElement(element: Element | null): void {
	if (element) {
		const tElements = Array.from(element.querySelectorAll('mi'));

		tElements.forEach((el) => {
			if (el.textContent === 't') {
				el.remove();
			}
		});
	}
}

/**
 * Flattens the mathML structure that are made up of multiple <mrows>
 */
export function flattenMathMLElement(element: Element | null) {
	if (element) {
		const children = Array.from(element.children);

		children.forEach((child) => {
			if (child.tagName === 'mrow' && child.children.length === 1) {
				element.replaceChild(child.children[0], child);
			} else {
				flattenMathMLElement(child);
			}
		});
	}
}

// Seperates the mathMLString into seperate equations into a list
export function separateEquations(mathMLString: string): string[] {
	if (!mathMLString) return [''];

	const parser = new DOMParser();
	const mathMLDocument = parser.parseFromString(mathMLString, 'application/xml');
	mathMLDocument.querySelector('math')?.removeAttribute('xmlns');
	const mtableElement = mathMLDocument.querySelector('mtable');

	const equations: string[] = [];
	if (mtableElement) {
		const mtrElements = Array.from(mtableElement.querySelectorAll('mtr'));

		mtrElements.forEach((mtrElement) => {
			const mtdElements = Array.from(mtrElement.querySelectorAll('mtd'));
			const mrowElements = mtdElements.map((mtd) => mtd.querySelector('mrow')).filter(Boolean);

			if (mrowElements.length > 0) {
				mrowElements.forEach(removeRedundantMrow);
				mrowElements.forEach(removeInvisibleTimes);
				mrowElements.forEach(removeParentheses);
				mrowElements.forEach(flattenMathMLElement);
				mrowElements.forEach(removeTElement);

				const newMrowElement = mathMLDocument.createElement('mrow');
				mrowElements.forEach((mrow) => {
					while (mrow && mrow.children.length > 0) {
						newMrowElement.appendChild(mrow.children[0]);
					}
				});

				const newMathElement = mathMLDocument.createElement('math');
				newMathElement.appendChild(newMrowElement);
				const serializer = new XMLSerializer();
				const newRowString = serializer
					.serializeToString(newMathElement)
					.replace(/[a-zA-Z]+="[^"]*"/g, '')
					.replaceAll(' ', '')
					.replaceAll('<mrow/>', '');
				equations.push(newRowString);
			}
		});
	}
	const mi = mathMLDocument.querySelector('mi');
	if (mi) {
		equations.push(mi.innerHTML);
	}
	return equations;
}

export enum EquationSide {
	Left = 'left',
	Right = 'right'
}

// extract identifiers in a mathml equation
export const extractVariablesFromMathML = (mathML: string, side?: EquationSide): string[] => {
	const parser = new DOMParser();
	const xmlDoc = parser.parseFromString(mathML, 'text/xml');
	const variables: string[] = [];

	if (!side) {
		const miElements = xmlDoc.getElementsByTagName('mi');
		for (let i = 0; i < miElements.length; i++) {
			const miElement = miElements[i];
			const variable = miElement.textContent?.trim();
			if (variable) {
				variables.push(variable);
			}
		}
	} else {
		const moElements = xmlDoc.getElementsByTagName('mo');
		for (let i = 0; i < moElements.length; i++) {
			const moElement = moElements[i];
			const isEqualsSign = moElement.textContent?.trim() === '=';
			if (isEqualsSign) {
				let currentNode: Element | null = moElement.previousElementSibling;
				while (currentNode) {
					if (currentNode.nodeName === 'mi') {
						const variable = currentNode.textContent?.trim();
						if (variable) {
							variables.unshift(variable);
						}
					} else if (currentNode.nodeName === 'mrow') {
						if (side === EquationSide.Left) {
							break;
						}
						const innerVariables = extractVariablesFromMathML(currentNode.innerHTML, side);
						variables.unshift(...innerVariables);
						if (side === EquationSide.Right) {
							break;
						}
					}
					currentNode = currentNode.previousElementSibling;
				}
				if (side === EquationSide.Left) {
					break;
				}
			}
		}
	}

	return variables;
};

// clean a latex equation
export const cleanLatexEquations = (equations: Array<string>): Array<string> =>
	cloneDeep(equations)
		.filter((equation) => equation !== '')
		.map((equation) =>
			equation
				// Refactor to make those replaceAll one regex change
				.replaceAll('\\begin', '')
				.replaceAll('\\end', '')
				.replaceAll('\\mathrm', '')
				.replaceAll('\\right', '')
				.replaceAll('\\left', '')
				.replaceAll('{align}', '')
				.replaceAll('=&', '=')
				// scientific variables such as \beta and \gamma are not parsed when there is a '*' (with no space) placed after them - other math operaters do work though
				.replaceAll('*', ' *')
				.trim()
		);

export const mergeUnique = (a: string[], b: string[]): string[] => {
	const aUnique: string[] = [...new Set(a)];
	const bSet = new Set(b);

	aUnique.forEach((element) => {
		if (![...bSet].some((item) => item.includes(element))) {
			bSet.add(element);
		}
	});

	return Array.from(bSet);
};

export const extractUniqueParameterStrings = (a: string[], b: string[]): string[] => {
	const aUnique: string[] = [...new Set(a)];

	return aUnique.filter((element) => !b.includes(element) && !b.some((item) => item.includes(element)));
};

// given an array of numbers and a percentile number (0-100), returns the percentile value
export function percentile(values: number[], q: number): number {
	if (q < 0 || q > 100) throw new Error('Percentile must be between 0 and 100');

	values.sort((a, b) => a - b);
	const index = (q / 100) * (values.length - 1);
	const lowerIndex = Math.floor(index);
	const upperIndex = Math.ceil(index);

	if (lowerIndex === upperIndex) {
		return values[lowerIndex];
	}
	const lowerValue = values[lowerIndex];
	const upperValue = values[upperIndex];
	return lowerValue + (upperValue - lowerValue) * (index - lowerIndex);
}

export function calculateUncertaintyRange(value: number, percentage: number): { min: number; max: number } {
	const delta = value * (percentage / 100);
	const min = parseFloat((value - delta).toFixed(8));
	let max = parseFloat((value + delta).toFixed(8));

	// return a [0, 1] range if both min and max are 0
	if (min === 0 && max === 0) max = 1;

	return {
		min,
		max
	};
}
