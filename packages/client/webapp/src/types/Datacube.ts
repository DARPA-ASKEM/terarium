export const DatacubeFilterAttributes = ['name', 'description'];

export type Datacube = {
	id: string;
	name: string;
	description: string;
	source: string; // author and affailiation
	status: string;
	category: string;
	type: string;
};

//
// Datacube Field names
//
export const TYPE = 'type'; // Type of datacube (model, dataset, etc)
export const SOURCE = 'source'; // Article title
export const STATUS = 'status'; // Status of the datacube, e.g., ready, deprecated, ...
export const CATEGORY = 'category'; // category

export const DISPLAY_NAMES: { [key: string]: string } = {
	[TYPE]: 'Datacube Type',
	[SOURCE]: 'Datacube Source',
	[STATUS]: 'Status',
	[CATEGORY]: 'Category'
};

export const FACET_FIELDS: string[] = [TYPE, SOURCE, STATUS, CATEGORY];
