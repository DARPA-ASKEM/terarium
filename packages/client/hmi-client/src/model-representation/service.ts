import _ from 'lodash';
import { runDagreLayout } from '@/services/graph';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { extractNestedStratas } from '@/model-representation/petrinet/mira-petri';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import type { Model } from '@/types/Types';
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
