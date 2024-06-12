import _ from 'lodash';
import { runDagreLayout } from '@/services/graph';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { extractNestedStratas } from '@/model-representation/petrinet/mira-petri';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import type { Initial, Model, ModelParameter } from '@/types/Types';
import { getModelType } from '@/services/model';
import { AMRSchemaNames } from '@/types/common';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';
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
			zoomModifier: 'ctrlKey',
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
		zoomModifier: 'ctrlKey',
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

// Gets states, vertices, stocks
export function getInitialsAlt(model: Model): any[] {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.vertices ?? [];
		case AMRSchemaNames.PETRINET:
			return model.model?.states ?? [];
		case AMRSchemaNames.STOCKFLOW:
			return model.model?.stocks ?? [];
		default:
			return [];
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

export function updateParameter(model: Model, parameterId: string, key: string, value: any) {
	function updateProperty(obj: ModelParameter | any /** There is no auxiliary type yet */) {
		// TODO: Add support for editing concept/grounding
		if (key === 'units') {
			if (!obj.units) obj.units = { expression: '', expression_mathml: '' };
			obj.units.expression = value;
			obj.units.expression_mathml = `<ci>${value}</ci>`;
		} else {
			obj[key] = value;
		}
	}

	const parameters = getParameters(model);
	const parameter = parameters.find((p: ModelParameter) => p.id === parameterId);
	if (!parameter) return;
	updateProperty(parameter);

	// FIXME: (For stockflow) Sometimes auxiliaries can share the same ids as parameters so for now both are be updated in that case
	const auxiliaries = model.model?.auxiliaries ?? [];
	const auxiliary = auxiliaries.find((a) => a.id === parameterId);
	if (!auxiliary) return;
	updateProperty(auxiliary);
}

export function updateInitial(model: Model, id: string, key: string, value: any) {
	function updateProperty(obj: ModelParameter | any /** There is no auxiliary type yet */) {
		// TODO: Add support for editing concept/grounding
		if (key === 'initial') {
			if (!obj.initial) obj.initial = { expression: '', expression_mathml: '' };
			obj.initial.expression = value;
			obj.initial.expression_mathml = `<ci>${value}</ci>`;
		} else {
			obj[key] = value;
		}
	}
	const initials = getInitials(model);
	const initial = initials.find((i: any) => i.id === id);
	if (!initial) return;
	updateProperty(initial);
}

/**
 * Retrieves the metadata for a specific initial in the model.
 * @param {Model} model - The model object.
 * @param {string} target - The target of the initial.
 * @returns {any} - The metadata for the specified initial or undefined if not found.
 */
export function getInitialMetadata(model: Model, target: string) {
	return model.metadata?.initials?.[target];
}

export function getInitialName(model: Model, target: string): string {
	return getInitialMetadata(model, target)?.name ?? '';
}

export function getInitialDescription(model: Model, target: string): string {
	return getInitialMetadata(model, target)?.description ?? '';
}

export function getInitialUnits(model: Model, target: string): string {
	return getInitialMetadata(model, target)?.units?.expression ?? '';
}

export function getInitialConcept(model: Model, target: string): string {
	const identifiers = getInitialMetadata(model, target)?.concept?.grounding?.identifiers;
	if (!identifiers) return '';
	return getNameOfCurieCached(
		new Map<string, string>(),
		getCurieFromGroundingIdentifier(identifiers)
	);
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
 * @param {string} target - The target of the initial.
 * @returns {Initial | null} - The model initial or null if not found.
 */
export function getInitial(model: Model, target: string): Initial | undefined {
	const modelType = getModelType(model);
	switch (modelType) {
		case AMRSchemaNames.REGNET:
			return model.model?.vertices.find((i) => i.id === target);
		case AMRSchemaNames.PETRINET:
		case AMRSchemaNames.STOCKFLOW:
		default:
			return model.semantics?.ode?.initials?.find((i) => i.target === target);
	}
}

// TODO: These updateMetadata functions and even the updateParameter function share similar logic and can be refactored
/**
 * Updates the metadata for a specific parameter in the model.
 * @param {Model} model - The model object.
 * @param {string} parameterId - The ID of the parameter.
 * @param {string} key - The key of the metadata to update.
 * @param {any} value - The new value for the metadata.
 */
export function updateParameterMetadata(
	model: Model,
	parameterId: string,
	key: string,
	value: any
) {
	if (!model.metadata?.parameters?.[parameterId]) {
		model.metadata ??= {};
		model.metadata.parameters ??= {};
		model.metadata.parameters[parameterId] ??= {};
		model.metadata.parameters[parameterId].id = parameterId;
	}
	const parameterMetadata = model.metadata.parameters[parameterId];

	// TODO: Add support for editing concept metadata
	if (key === 'units') {
		if (!parameterMetadata.units)
			parameterMetadata.units = { expression: '', expression_mathml: '' };
		parameterMetadata.units.expression = value;
		parameterMetadata.units.expression_mathml = `<ci>${value}</ci>`;
	} else {
		parameterMetadata[key] = value;
	}
}

/**
 * Updates the metadata for a specific initial in the model.
 * @param {Model} model - The model object.
 * @param {string} target - The target of the initial.
 * @param {string} key - The key of the metadata to update.
 * @param {any} value - The new value for the metadata.
 */
export function updateInitialMetadata(model: Model, target: string, key: string, value: any) {
	if (!model.metadata?.initials?.[target]) {
		model.metadata ??= {};
		model.metadata.initials ??= {};
		model.metadata.initials[target] ??= {};
	}
	const initialMetadata = model.metadata.initials[target];

	// TODO: Add support for editing concept metadata
	if (key === 'units') {
		if (!initialMetadata.units) initialMetadata.units = { expression: '', expression_mathml: '' };
		initialMetadata.units.expression = value;
		initialMetadata.units.expression_mathml = `<ci>${value}</ci>`;
	} else {
		initialMetadata[key] = value;
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

export function isModelMissingMetadata(model: Model): boolean {
	const parameters: ModelParameter[] = getParameters(model);
	const initials: Initial[] = getInitials(model);

	const initialsCheck = initials.some((i) => {
		const initialMetadata = getInitialMetadata(model, i.target);
		return !initialMetadata?.name || !initialMetadata?.description || !initialMetadata?.units;
	});

	const parametersCheck = parameters.some((p) => !p.name || !p.description || !p.units?.expression);

	return initialsCheck || parametersCheck;
}
