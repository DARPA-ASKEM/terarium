export const DatacubeFilterAttributes = ['name', 'description'];

export type Datacube = {
	id: string;
	description: string;
	status: string;
	outputs: [];
	display_name: string;
	name: string;
	type: string;
	source: string;
};
