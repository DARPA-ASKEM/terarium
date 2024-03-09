export const removeModifiers = (v: string, context: { [key: string]: string }) => {
	let result = v;
	const contextKeys = Object.keys(context);
	contextKeys.forEach((key) => {
		result = result.replace(`_${context[key]}`, '');
	});
	return result;
};
