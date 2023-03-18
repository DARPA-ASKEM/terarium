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
