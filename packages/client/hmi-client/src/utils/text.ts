import { upperFirst, lowerCase, startCase, toLower } from 'lodash';

// Highlight text by search terms
function highlightText(text: string, searchTerms: string): string {
	// Define how we're highlighting text
	const emphasis = (value) => `<em class="highlight">${value}</em>`;

	// Split the search terms by spaces to make a RegEx
	const terms = searchTerms.trim().split(' ').join('|');
	const search = RegExp(`\\b(${terms})\\b`, 'gi');

	// For each term, highlight it
	return text.replace(search, (match) => emphasis(match));
}

function capitalize(string: string) {
	return upperFirst(lowerCase(string));
}

/**
 * Convert a pascal case string to a capital sentence, avoids acronyms.
 * Most useful for converting enum of model framework to human-readable string:
 *  - "GeneralizedAMR" -> "Generalized AMR"
 *  - "MathExpressionTree" -> "Math Expression Tree"
 * @param pascalCaseString
 */
function pascalCaseToCapitalSentence(pascalCaseString: string) {
	// Split the string by capital letters, but avoid acronyms
	const words = pascalCaseString.match(/([A-Z]+[a-z]*)/g);

	// Capitalize the first letter of each word and join them with spaces
	return words?.map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}

function snakeToCapitalized(snakeCaseString: string) {
	const words = snakeCaseString.split('_');
	return upperFirst(lowerCase(words.join(' ')));
}

function snakeToCapitalSentence(snakeCaseString: string) {
	return startCase(toLower(snakeCaseString));
}

function formatListWithConjunction(stringList: string[]) {
	let formattedString = '';
	if (stringList.length > 1) {
		formattedString = `${stringList.slice(0, -1).join(', ')} and ${stringList.slice(-1)}`;
	} else if (stringList[0]) {
		formattedString = stringList[0];
	}
	return formattedString;
}

export {
	highlightText as highlight,
	pascalCaseToCapitalSentence,
	snakeToCapitalized,
	snakeToCapitalSentence,
	formatListWithConjunction,
	capitalize
};
