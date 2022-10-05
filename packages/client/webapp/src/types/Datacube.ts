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
