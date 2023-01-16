export enum ProvenanceType {
	Dataset = 'Dataset',
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
