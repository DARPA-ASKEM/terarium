export const extractDynamicRows = (range: string) => {
	const match = range.match(/L(\d+)-L(\d+)/) || [];
	const startRow = parseInt(match[1], 10) - 1;
	const endRow = parseInt(match[2], 10) - 1;
	return { startRow, endRow };
};
