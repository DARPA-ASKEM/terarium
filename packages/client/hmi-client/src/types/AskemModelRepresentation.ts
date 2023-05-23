export type AskemModelRepresentationType = {
	name: string;
	schema: string;
	description: string;
	model_version: string;
	model: Model;
	metadata: Metadata;
};

export interface Metadata {
	variable_statements: VariableStatement[];
}

export interface VariableStatement {
	id: string;
	variable: Variable;
	value: VariableValue;
	metadata: VariableMetaData[];
}

export interface VariableMetaData {
	type: string;
	value: string;
}

export interface VariableValue {
	value: string;
	type: string;
	dkg_grounding: null;
}

export interface Variable {
	id: string;
	name: string;
	metadata: VariableMetaData[];
	dkg_groundings: DkgGrounding[];
	column: VariableColumn[];
	paper: ReferencedPaper;
	equations: any[];
}

export interface VariableColumn {
	id: string;
	name: string;
	dataset: Dataset;
}

export interface Dataset {
	id: string;
	name: string;
	metadata: null;
}

export interface DkgGrounding {
	id: string;
	name: string;
	score: number | null;
}

export interface ReferencedPaper {
	id: string;
	name: string;
	md5: string;
	file_directory: string;
	doi: string;
}

export interface Model {
	states: ModelStates[];
	transitions: ModelTransition[];
	parameters: ModelParameters[];
}

export interface ModelParameters {
	id: string;
	value: number;
}

export interface ModelStates {
	id: string;
	name: string;
	grounding: ModelGrounding;
}

export interface ModelGrounding {
	identifiers: {};
	context: {};
}

export interface ModelTransition {
	id: string;
	input: string[];
	output: string[];
	properties: TransitionProperties;
}

export interface TransitionProperties {
	name: string;
	rate: TransitionRate;
}

export interface TransitionRate {
	expression: string;
	expression_mathml: string;
}
