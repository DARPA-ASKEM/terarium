export interface MiraConcept {
	name: string;
	display_name: string | null;
	description: string | null;
	identifiers: any;
	context: any;
	units: any;
}

export interface MiraParameter {
	name: string;
	display_name: string | null;
	description: string | null;
	identifiers: any;
	context: any;
	units: any;
	value: any;
	distribution: any;
}

export interface MiraInitial {
	concept: MiraConcept;
	expression: string;
}

export interface MiraTemplate {
	rate_law: string;
	name: string;
	display_name: string | null;
	type: string;
	controller?: MiraConcept;
	controllers?: MiraConcept[];
	subject: MiraConcept;
	outcome: MiraConcept;
	provenance: any[];
}

export interface MiraModel {
	templates: MiraTemplate[];
	parameters: { [key: string]: MiraParameter };
	initials: { [key: string]: MiraInitial };
	observables: { [key: string]: any };
	annotations: any;
	time: any;
}

export interface MiraTemplateParams {
	[key: string]: {
		name: string;
		params: string[];
		subject: string;
		outcome: string;
		controllers: string[];
	};
}

// Terarium MIRA types
export type MiraMatrixEntry = {
	content: {
		id: any;
		value: any;
	};
	row: number;
	col: number;
	rowCriteria: string;
	colCriteria: string;
};
export type MiraMatrix = MiraMatrixEntry[][];

export interface TemplateSummary {
	name: string;
	expression?: string;
	subject: string;
	outcome: string;
	controllers: string[];
}

export interface ObservableSummary {
	[key: string]: {
		name: string;
		display_name: string;
		description: string;
		expression: string;
		references: string[];
	};
}

export interface MMT {
	mmt: MiraModel;
	template_params: MiraTemplateParams;
	observable_summary: ObservableSummary;
}
