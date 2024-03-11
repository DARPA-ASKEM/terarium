export interface MiraTemplate {
	rate_law: string;
	name: string;
	display_name: string | null;
	type: string;
	controller?: any;
	subject: any;
	outcome: any;
	provenance: any[];
}

export interface MiraModel {
	templates: MiraTemplate[];
	parameters: { [key: string]: any };
	initials: { [key: string]: any };
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

export type MiraMatrixEntry = { id: any; value: any };
export type MiraMatrix = MiraMatrixEntry[][];
