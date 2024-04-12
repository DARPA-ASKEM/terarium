import _ from 'lodash';
import { runDagreLayout } from '@/services/graph';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { extractNestedStratas } from '@/model-representation/petrinet/mira-petri';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import type { Initial, Model, ModelParameter } from '@/types/Types';
import { getModelType } from '@/services/model';
import { AMRSchemaNames } from '@/types/common';
import { NestedPetrinetRenderer } from './petrinet/nested-petrinet-renderer';
import { isStratifiedModel, getContextKeys, collapseTemplates } from './mira/mira';
import { extractTemplateMatrix } from './mira/mira-util';

export const getVariable = (miraModel: MiraModel, variableName: string) => {
	if (miraModel.initials[variableName]) {
		return {
			value: miraModel.initials[variableName].expression
		};
	}
	if (miraModel.parameters[variableName]) {
		return {
			value: miraModel.parameters[variableName].value
		};
	}
	throw new Error(`${variableName} not found`);
};

export const updateVariable = (
	amr: Model,
	variableType: string,
	variableName: string,
	value: any,
	valueMathML: string
) => {
	const schemaName = amr.header.schema_name;
	console.log('updating regnet variable', variableName, schemaName);

	// ======== PETRINET =======
	if (schemaName === 'petrinet' && amr.semantics?.ode) {
		const ode = amr.semantics.ode;

		if (variableType === 'initials') {
			const obj = ode.initials?.find((d) => d.target === variableName);
			if (obj) {
				obj.expression = value;
				obj.expression_mathml = valueMathML;
			}
		}
		if (variableType === 'parameters') {
			const obj = ode.parameters?.find((d) => d.id === variableName);
			if (obj) {
				obj.value = +value;
			}
		}
		if (variableType === 'rates') {
			const obj = ode.rates?.find((d) => d.target === variableName);
			if (obj) {
				obj.expression = value;
				obj.expression_mathml = valueMathML;
			}
		}
	}

	// ======== REGNET =======
	if (schemaName === 'regnet') {
		if (variableType === 'initials') {
			const obj = amr.model.vertices.find((d) => d.id === variableName);
			if (obj) {
				obj.initial = value;
			}
		}
		if (variableType === 'parameters') {
			const obj = amr.model.parameters.find((d) => d.id === variableName);
			if (obj) {
				obj.value = value;
			}
		}
		if (variableType === 'rates') {
			const obj = amr.semantics?.ode.rates.find((d) => d.target === variableName);
			if (obj) {
				obj.expression = value;
				obj.expression_mathml = valueMathML;
			}
		}
	}

	// FIXME: stocknflow
};

export const getModelRenderer = (
	miraModel: MiraModel,
	graphElement: HTMLDivElement,
	useNestedRenderer: boolean
): PetrinetRenderer | NestedPetrinetRenderer => {
	const isStratified = isStratifiedModel(miraModel);
	// Debug start
	// console.group('mmt info');
	// console.log('# templates: ', miraModel.templates.length);
	// console.log('# parameters: ', Object.keys(miraModel.parameters).length);
	// console.log('stratified model: ', isStratified);
	// console.groupEnd();
	// Debug end

	if (useNestedRenderer && isStratified) {
		// FIXME: Testing, move to mira service
		const processedSet = new Set<string>();
		const conceptData: any = [];
		miraModel.templates.forEach((t) => {
			['subject', 'outcome', 'controller'].forEach((conceptKey) => {
				if (!t[conceptKey]) return;
				const conceptName = t[conceptKey].name;
				if (processedSet.has(conceptName)) return;
				conceptData.push({
					// FIXME: use reverse-lookup to get root concept
					base: _.first(conceptName.split('_')),
					...t[conceptKey].context
				});

				processedSet.add(conceptName);
			});
		});
		const dims = getContextKeys(miraModel);
		dims.unshift('base');

		const { matrixMap } = collapseTemplates(miraModel);
		const transitionMatrixMap = {};
		matrixMap.forEach((value, key) => {
			transitionMatrixMap[key] = extractTemplateMatrix(value).matrix;
		});

		const nestedMap = extractNestedStratas(conceptData, dims);
		return new NestedPetrinetRenderer({
			el: graphElement,
			useAStarRouting: false,
			useStableZoomPan: true,
			runLayout: runDagreLayout,
			dims,
			nestedMap,
			transitionMatrices: transitionMatrixMap
		});
	}

	return new PetrinetRenderer({
		el: graphElement,
		useAStarRouting: false,
		useStableZoomPan: true,
		runLayout: runDagreLayout,
		dragSelector: 'no-drag'
	});
};

/**
 * Returns the model parameters based on the model type.
 * @param {Model} model - The model object.
 * @returns {ModelParameter[]} - The model parameters.
 */
export function getModelParameters(model: Model): ModelParameter[] {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.parameters ?? [];
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			return model.semantics?.ode?.parameters ?? [];
	}
}

/**
 * Returns the model parameter with the specified ID.
 * @param {Model} model - The model object.
 * @param {string} parameterId - The ID of the parameter.
 * @returns {ModelParameter | null} - The model parameter or null if not found.
 */
export function getParameter(model: Model, parameterId: string): ModelParameter | null {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.parameters.find((p) => p.id === parameterId);
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			return model.semantics?.ode?.parameters?.find((p) => p.id === parameterId) ?? null;
	}
}

/**
 * Returns the metadata for the specified semantic type and parameter ID.
 * @param {Model} model - The model object.
 * @param {string} semanticType - The semantic type.
 * @param {string} parameterId - The parameter ID.
 * @returns {any} - The metadata.
 */
export function getMetadata(model: Model, semanticType: string, parameterId: string) {
	return model.metadata?.[semanticType]?.[parameterId];
}

/**
 * Returns the model initials based on the model type.
 * @param {Model} model - The model object.
 * @returns {Initial[]} - The model initials.
 */
export function getModelInitials(model: Model): Initial[] {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.vertices ?? [];
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			return model.semantics?.ode?.initials ?? [];
	}
}

/**
 * Returns the model initial with the specified ID.
 * @param {Model} model - The model object.
 * @param {string} initialId - The ID of the initial.
 * @returns {Initial | null} - The model initial or null if not found.
 */
export function getInitial(model: Model, initialId: string): Initial | null {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.vertices.find((i) => i.id === initialId);
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			return model.semantics?.ode?.initials?.find((i) => i.target === initialId) ?? null;
	}
}

/**
 * Returns the timeseries for the specified semantic ID.
 * @param {Model} model - The model object.
 * @param {string} semanticId - The semantic ID.
 * @returns {any} - The timeseries.
 */
export function getTimeseries(model: Model, semanticId: string) {
	return model.metadata?.timeseries?.[semanticId];
}

/**
 * Updates the metadata for the specified semantic ID, semantic type, and metadata key.
 * @param {Model} model - The model object.
 * @param {string} semanticId - The semantic ID.
 * @param {string} semanticType - The semantic type.
 * @param {string} metadataKey - The metadata key.
 * @param {any} value - The new value.
 */
export function updateMetadata(
	model: Model,
	semanticId: string,
	semanticType: string,
	metadataKey: string,
	value: any
) {
	if (!model.metadata?.[semanticType]?.[semanticId]) {
		model.metadata ??= {};
		model.metadata[semanticType] ??= {};
		model.metadata[semanticType][semanticId] ??= {};
	}
	model.metadata[semanticType][semanticId][metadataKey] = value;
}

/**
 * Validates the time series values.
 * @param {string} values - The time series values.
 * @returns {boolean} - True if the values are valid, false otherwise.
 */
export function validateTimeSeries(values: string) {
	const isPairValid = (pair: string): boolean => /^\d+:\d+(\.\d+)?$/.test(pair.trim());
	const isValid = values.split(',').every(isPairValid);
	return isValid;
}
