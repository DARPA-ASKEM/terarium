import { CONCEPT_FACETS_DISPLAY_NAME, CONCEPT_FACETS_FIELD } from './Concept';
import { Filters } from './Filter';

export type DatasetAnnotatedField = {
	name: string;
	display_name: string;
	description: string;
	type: string;
	qualifies: string[];
	aliases: {};
};

export type DatasetAnnotatedFeature = DatasetAnnotatedField & {
	feature_type: string;
	units: string;
	units_description: string;
	primary_ontology_id: string;
	qualifierrole: string;
};

export type DatasetAnnotatedDate = DatasetAnnotatedField & {
	date_type: string;
	primary_date: boolean;
	time_format: string;
};

export type DatasetAnnotatedGeo = DatasetAnnotatedField & {
	geo_type: string;
	primary_geo: boolean;
	resolve_to_gadm: boolean;
	is_geo_pair: string;
	coord_format: string;
	gadm_level: string;
};

export type DatasetAnnotations = {
	data_paths: string[];
	annotations: {
		geo: DatasetAnnotatedGeo[];
		date: DatasetAnnotatedDate[];
		feature: DatasetAnnotatedFeature[];
	};
};

export type Dataset = {
	id: string | number;
	name: string;
	url: string;
	description: string;
	timestamp: string | Date;
	deprecated: boolean;
	sensitivity: string;
	quality: string;
	temporalResolution: string; // TEMP: mapped from temporal_resolution
	temporal_resolution: string;
	geospatialResolution: string; // TEMP: mapped from geospatial_resolution
	geospatial_resolution: string;
	annotations: DatasetAnnotations;
	maintainer: string | number;
	simulation_run: boolean;
	simulationRun: boolean; // TEMP: mapped from simulation_run

	type: string;
};

export type DatasetSearchParams = {
	filters?: Filters;
};

//
// Dataset Field names
//
export const ID = 'id';
export const NAME = 'name';
export const DESCRIPTION = 'description';
export const SIMULATION_RUN = 'simulation_run';

export const DISPLAY_NAMES: { [key: string]: string } = {
	[NAME]: 'Dataset Name',
	[SIMULATION_RUN]: 'Simulation Run',
	[CONCEPT_FACETS_FIELD]: CONCEPT_FACETS_DISPLAY_NAME
};

export const FACET_FIELDS: string[] = [ID, SIMULATION_RUN]; // fields to show facets for
export const DATASET_FILTER_FIELDS: string[] = [NAME, DESCRIPTION]; // when applying non-facet filters, search these fields
