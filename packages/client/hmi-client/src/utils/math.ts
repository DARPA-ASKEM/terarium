/* Collection of MATH/MATH-EDITOR utility functions */

// simple check to test if it's a mathml string based on opening and closing tags
export const isMathML = (val: string): boolean => {
	const regex = /<math[>\s][\s\S]*?<\/math>/s;
	return regex.test(val);
};
