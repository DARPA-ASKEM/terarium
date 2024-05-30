// search construct
export type ClauseField = string;
export type ClauseValue = string | number | Array<number>;
export type ClauseOperand = 'and' | 'or';
export type ClauseNegation = true | false;

export interface Clause {
	field: ClauseField;
	operand?: ClauseOperand;
	isNot?: ClauseNegation;
	values: ClauseValue[];
}

export interface Filters {
	clauses: Clause[];
}

// search options
export interface FiltersOptions {
	from?: number;
	size?: number;
	sort?: { [key: string]: string };
	documents?: boolean; // this is a hack to get around document-context issue: in some places we want them, in others we do not.
}
