/* Collection of MATH/MATH-EDITOR utility functions */
import { logger } from '@/utils/logger';
import Mathml2latex from 'mathml-to-latex';

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
	if (isMathML(mathMLString)) {
		try {
			return Mathml2latex.convert(mathMLString);
		} catch (error) {
			logger.error(error, { showToast: false, silent: true });
			return '';
		}
	}

	return mathMLString;
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

// Cleans the mathMLString produced by either katex or mathlive for use with mathml to petri endpoint
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
	return equations;
}
