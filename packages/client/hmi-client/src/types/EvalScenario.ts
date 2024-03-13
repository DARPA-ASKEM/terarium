export interface Question {
	task: string;
	description: string;
}

export interface Scenario {
	name: string;
	questions: Question[];
}

export interface EvalScenario {
	scenarios: Scenario[];
}
