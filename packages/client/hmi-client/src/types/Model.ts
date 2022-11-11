export type Model = {
	id: string;
	name: string;
	description: string;
	source: string; // author and affailiation
	status: string;
	category: string;
	type: string;
};

export type ModelSearchParams = {
	category?: string;
};

//
// Model Field names
//
export const TYPE = 'type'; // Type of model (model, dataset, etc)
export const SOURCE = 'source'; // model source
export const STATUS = 'status'; // Status of the model, e.g., ready, deprecated, ...
export const CATEGORY = 'category'; // category

export const DISPLAY_NAMES: { [key: string]: string } = {
	[TYPE]: 'Model Type',
	[SOURCE]: 'Model Source',
	[STATUS]: 'Status',
	[CATEGORY]: 'Category'
};

export const FACET_FIELDS: string[] = [TYPE, SOURCE, STATUS, CATEGORY];
