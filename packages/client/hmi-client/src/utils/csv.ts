export type Record = {
	[key: string]: string;
};

// Convert csv to record objects
export const csvToRecords = (csv: string) => {
	const lines = csv.split('\n').filter((line) => line.trim() !== '');
	const result: Record[] = [];
	const headerRow = lines[0];
	// the app uses data as json structure, and when needed it is written into csv with ; separator
	// but regardless we are able to consume input csv with both ; and , separators
	// as long as a header is provided
	const separator = headerRow.includes(';') ? ';' : ',';
	const headers = headerRow.split(separator);
	for (let i = 1; i < lines.length; i++) {
		const obj: Record = {};
		const currentLine = lines[i].split(separator);
		for (let j = 0; j < headers.length; j++) {
			obj[headers[j]] = currentLine[j];
		}
		result.push(obj);
	}
	return result;
};

export const getColumns = (record: Record[] | Record) => {
	if (record.length && record.length > 0) {
		return Object.keys((record as Record[])[0]);
	}
	return Object.keys(record);
};
