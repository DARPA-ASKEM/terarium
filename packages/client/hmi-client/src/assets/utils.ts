function getImage(fileName: string) {
	try {
		const modules = import.meta.glob('@/assets/images/**/*.{png,svg}', {
			eager: true,
			import: 'default'
		});
		const moduleKeys = Object.keys(modules);
		const fileSrc = moduleKeys.find((key) => key.endsWith(fileName));
		return fileSrc ? (modules[fileSrc] as string) : null;
	} catch (err) {
		console.debug('getImage', err);
		return null;
	}
}

export default getImage;
