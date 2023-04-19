/* Collection of MATH/MATH-EDITOR utility functions */

export enum MathEditorModes {
	LIVE = 'mathLIVE',
	JAX = 'mathJAX'
}

// simple check to test if it's a mathml string based on opening and closing tags
export const isMathML = (val: string): boolean => {
	const regex = /<math[>\s][\s\S]*?<\/math>/s;
	return regex.test(val);
};

export const removeRedundantMrow = (element: Element | null): void => {
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
				mrowElement.remove();
			}
		});
	}
};

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

export function separateEquations(mathMLString: string): string[] {
	if (!mathMLString) return [''];

	const parser = new DOMParser();
	const mathMLDocument = parser.parseFromString(mathMLString, 'application/xml');
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
				equations.push(serializer.serializeToString(newMathElement));
			}
		});
	}

	return equations;
}
