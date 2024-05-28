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
	const template = miraModel.templates.find((t) => t.name === variableName);
	if (template) {
		return {
			value: template.rate_law
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
export function getParameters(model: Model): ModelParameter[] {
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
export function getParameter(model: Model, parameterId: string): ModelParameter | undefined {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.parameters.find((p) => p.id === parameterId);
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			return model.semantics?.ode?.parameters?.find((p) => p.id === parameterId);
	}
}

/**
 * Retrieves the metadata for a specific initial in the model.
 * @param {Model} model - The model object.
 * @param {string} initialId - The ID of the initial.
 * @returns {any} - The metadata for the specified initial or undefined if not found.
 */
export function getInitialMetadata(model: Model, parameterId: string) {
	return model.metadata?.initials?.[parameterId];
}

export function getInitialName(model: Model, initialId: string): string {
	return model.metadata?.initials?.[initialId]?.name ?? '';
}

export function getInitialDescription(model: Model, initialId: string): string {
	return model.metadata?.initials?.[initialId]?.description ?? '';
}

export function getInitialUnit(model: Model, initialId: string): string {
	return model.metadata?.initials?.[initialId]?.unit ?? '';
}

export function getInitialConcept(model: Model, initialId: string): string {
	return model.metadata?.initials?.[initialId]?.concept?.grounding ?? '';
}

/**
 * Retrieves the metadata for a specific parameter in the model.
 * @param {Model} model - The model object.
 * @param {string} parameterId - The ID of the parameter.
 * @returns {any} - The metadata for the specified parameter or undefined if not found.
 */
export function getParameterMetadata(model: Model, parameterId: string) {
	return model.metadata?.parameters?.[parameterId];
}

/**
 * Returns the model initials based on the model type.
 * @param {Model} model - The model object.
 * @returns {Initial[]} - The model initials.
 */
export function getInitials(model: Model): Initial[] {
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
export function getInitial(model: Model, initialId: string): Initial | undefined {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.vertices.find((i) => i.id === initialId);
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			return model.semantics?.ode?.initials?.find((i) => i.target === initialId);
	}
}

/**
 * Updates the metadata for a specific parameter in the model.
 * @param {Model} model - The model object.
 * @param {string} parameterId - The ID of the parameter.
 * @param {string} metadataKey - The key of the metadata to update.
 * @param {any} value - The new value for the metadata.
 */
export function updateParameterMetadata(
	model: Model,
	parameterId: string,
	metadataKey: string,
	value: any
) {
	if (!model.metadata?.parameters?.[parameterId]) {
		model.metadata ??= {};
		model.metadata.parameters ??= {};
		model.metadata.parameters[parameterId] ??= {};
	}
	model.metadata.parameters[parameterId][metadataKey] = value;
}

/**
 * Updates the metadata for a specific initial in the model.
 * @param {Model} model - The model object.
 * @param {string} initialId - The ID of the initial.
 * @param {string} metadataKey - The key of the metadata to update.
 * @param {any} value - The new value for the metadata.
 */
export function updateInitialMetadata(
	model: Model,
	initialId: string,
	metadataKey: string,
	value: any
) {
	if (!model.metadata?.initials?.[initialId]) {
		model.metadata ??= {};
		model.metadata.initials ??= {};
		model.metadata.initials[initialId] ??= {};
	}
	model.metadata.initials[initialId][metadataKey] = value;
}

export function setParameters(model: Model, parameters: ModelParameter[]) {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			model.model.parameters = parameters;
			break;
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			if (model.semantics) model.semantics.ode.parameters = parameters;
			break;
	}
}

// cleans a model by removing distributions that are not needed
export function cleanModel(model: Model): void {
	const parameters: ModelParameter[] = getParameters(model);

	parameters.forEach((p) => {
		const max = parseFloat(p.distribution?.parameters.maximum);
		const min = parseFloat(p.distribution?.parameters.minimum);

		// we delete the distribution when there is partial/no distribution

		if (Number.isNaN(max) || Number.isNaN(min)) {
			delete p.distribution;
		}
	});
}
