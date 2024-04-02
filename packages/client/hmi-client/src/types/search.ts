export enum DocumentSource {
	XDD = 'xDD',
	TERARIUM = 'Terarium'
}

export enum DatasetSource {
	TERARIUM = 'Terarium',
	ESGF = 'ESGF'
}

export type Source = DocumentSource | DatasetSource;
