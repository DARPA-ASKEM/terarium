import { updateModelConfiguration } from '@/services/model-configurations';
import { Model, ModelConfiguration } from '@/types/Types';

const replaceExactString = (str: string, wordToReplace: string, replacementWord: string): string =>
	str.trim() === wordToReplace.trim() ? str.replace(wordToReplace, replacementWord) : str;

const replaceValuesInExpression = (
	expression: string,
	wordToReplace: string,
	replaceWord: string
): string => {
	let expressionBuilder = '';
	let isOperator = false;
	let content = '';

	[...expression].forEach((c) => {
		// not sure if this is an exhaustive list of operators or if it includes any operators it shouldn't
		if ([',', '(', ')', '+', '-', '*', '/', '^'].includes(c)) {
			isOperator = true;
		} else {
			isOperator = false;
		}

		if (isOperator) {
			expressionBuilder += replaceExactString(content, wordToReplace, replaceWord);
			content = '';
			expressionBuilder += c;
		}
		if (!isOperator) {
			content += c;
		}
	});

	// if we reach the end of an expression and it doesn't end with an operator, we need to add the updated content
	if (!isOperator) {
		expressionBuilder += replaceExactString(content, wordToReplace, replaceWord);
	}

	return expressionBuilder;
};

// function to replace the content inside the tags of a mathml expression
const replaceValuesInMathML = (
	mathmlExpression: string,
	wordToReplace: string,
	replaceWord: string
): string => {
	let expressionBuilder = '';
	let isTag = false;
	let content = '';

	[...mathmlExpression].forEach((c) => {
		if (!isTag && c === '<') {
			isTag = true;
		}
		if (isTag && c === '>') {
			isTag = false;
		}

		if (isTag) {
			expressionBuilder += replaceExactString(content, wordToReplace, replaceWord);
			content = '';
			expressionBuilder += c;
		}
		if (!isTag) {
			// this only works if there is no '>' literal in the non-tag content
			if (c !== '>') {
				content += c;
			} else {
				expressionBuilder += c;
			}
		}
	});

	return expressionBuilder;
};

export const updateParameterId = (amr: Model, id: string, newId: string) => {
	if (amr.semantics?.ode.parameters) {
		amr.semantics.ode.parameters.forEach((param) => {
			if (param.id === id) {
				param.id = newId;
			}
		});

		// update the expression and expression_mathml fields
		amr.semantics.ode?.rates?.forEach((rate) => {
			rate.expression = replaceValuesInExpression(rate.expression, id, newId);
			if (rate.expression_mathml) {
				rate.expression_mathml = replaceValuesInMathML(rate.expression_mathml, id, newId);
			}
		});

		// if there's a timeseries field with the old parameter id then update it to the new id
		if (amr.metadata?.timeseries && amr.metadata.timeseries[id]) {
			amr.metadata.timeseries[newId] = amr.metadata.timeseries[id];
			delete amr.metadata.timeseries[id];
		}
	}
};

export const updateConfigFields = async (
	modelConfigs: ModelConfiguration[],
	id: string,
	newId: string
) => {
	modelConfigs.forEach((config) => {
		updateParameterId(config.configuration, id, newId);
		// note that this is making an async call but we don't need to wait for it to finish
		// since we don't immediately need the updated configs
		updateModelConfiguration(config);
	});
};

export function newAMR(modelName: string = '') {
	const amr: Model = {
		header: {
			name: modelName,
			description: '',
			schema:
				'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
			schema_name: 'petrinet',
			model_version: '0.1'
		},
		model: {
			states: [],
			transitions: []
		},
		semantics: {
			ode: {
				rates: [],
				initials: [],
				parameters: []
			}
		}
	};
	return amr;
}
