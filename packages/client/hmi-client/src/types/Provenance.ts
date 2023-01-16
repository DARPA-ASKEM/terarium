import { Dataset } from './Dataset';
import { Model } from './Model';
import { ProjectAssetTypes } from './Project';
import { XDDArticle } from './XDD';

export enum ProvenanceType {
	// eslint-disable-next-line @typescript-eslint/no-shadow
	Dataset = 'Dataset',
	// eslint-disable-next-line @typescript-eslint/no-shadow
	Model = 'Model',
	ModelParameter = 'ModelParameter',
	Plan = 'Plan',
	PlanParameter = 'PlanParameter',
	ModelRevision = 'ModelRevision',
	Intermediate = 'Intermediate',
	Publication = 'Publication',
	SimulationRun = 'SimulationRun',
	Project = 'Project',
	Concept = 'Concept'
}

export type ProvenanceQueryParam = {
	root_id?: number;
	root_type?: ProvenanceType;
	user_id?: number;
	curie?: string;
};

// @FIXME: note that each provenance artifact result should have a field that highlights
//         how that artifact relates to the root type
export type ProvenanceArtifacts = {
	[ProjectAssetTypes.PUBLICATIONS]?: XDDArticle[];
	[ProjectAssetTypes.MODELS]?: Model[] | string[];
	[ProjectAssetTypes.DATASETS]?: Dataset[] | string[];
	// FIXME: add more types of artifacts that can be returned as part of the provenance search
};

export type ProvenanceResult = {
	artifacts: ProvenanceArtifacts;
	raw: any; // return the raw graph DB response
};
