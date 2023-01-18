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

export type ProvenanceResult = {
	result: {
		edges: {
			relationship: string;
			left: { type: string; id: number };
			right: { type: string; id: number };
		}[];
		nodes: {
			type: string;
			id: number;
			uuid: string;
		}[];
	};
};
