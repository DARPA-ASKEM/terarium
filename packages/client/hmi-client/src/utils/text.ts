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

export { highlightText as highlight };
