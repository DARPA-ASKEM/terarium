interface DSVEntry {
	rowIdx: number;
	rowLabel: string | null;
	colIdx: number;
	colLabel: string | null;
	value: number;
}

/**
 * A custom delimiter-separated value parser where we may expect
 * to have any or none of row/column labels.
 *
 * Returns a list of extracted values with their coordinate (0-based) and
 * row/column names if available.
 * */
export const dsvParse = (text: string) => {
	const hasTabs = text.includes('\t');
	const hasCommas = text.includes(',');

	let delimeter = ',';
	let hasRowLabels = false;
	let hasColLabels = false;

	if (hasTabs && !hasCommas) {
		delimeter = '\t';
	} else if (hasCommas && !hasTabs) {
		delimeter = ',';
	}

	const lines = text
		.split(/\n/)
		.map((d) => d.trim())
		.filter((d) => d !== '');
	const lineOne = lines[0].split(delimeter);

	// Trying to determine if there are col-labels and row-labels
	// Let H = label, V = value, then we have these formats
	//
	// VV   V    VV
	//      V    VV
	//
	// HV   H     H
	//      V    HV
	if (lineOne.length === 1) {
		// Single column
		if (Number.isNaN(+lineOne[0])) {
			hasColLabels = true;
			hasRowLabels = false;
		} else {
			hasColLabels = false;
			hasRowLabels = false;
		}
	} else if (lines.length === 1) {
		// Single row
		if (Number.isNaN(+lineOne[0])) {
			hasColLabels = false;
			hasRowLabels = true;
		} else {
			hasColLabels = false;
			hasRowLabels = false;
		}
	} else if (lines.length > 1 && lineOne.length > 1) {
		if (Number.isNaN(+lineOne[1])) {
			hasColLabels = true;
		}
		const line2 = lines[1];
		if (Number.isNaN(+line2.split(delimeter)[0])) {
			hasRowLabels = true;
		}
	} else {
		throw Error('unable to parse information');
	}

	const entries: DSVEntry[] = [];
	for (let i = hasColLabels ? 1 : 0; i < lines.length; i++) {
		const line = lines[i];
		const tokens = line.split(delimeter);

		for (let j = hasRowLabels ? 1 : 0; j < tokens.length; j++) {
			entries.push({
				colIdx: hasColLabels ? j - 1 : j,
				colLabel: hasColLabels ? lineOne[j] : null,
				rowIdx: hasColLabels ? i - 1 : i,
				rowLabel: hasRowLabels ? tokens[0] : null,
				value: +tokens[j]
			});
		}
	}

	return {
		delimeter,
		hasColLabels,
		hasRowLabels,
		entries
	};
};
