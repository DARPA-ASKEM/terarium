import _ from 'lodash';
import { runDagreLayout } from '@/services/graph';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { extractNestedStratas } from '@/model-representation/petrinet/mira-petri';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NestedPetrinetRenderer } from './petrinet/nested-petrinet-renderer';
import { isStratifiedModel, getContextKeys, collapseTemplates } from './mira/mira';
import { extractTemplateMatrix } from './mira/mira-util';

export const getModelRenderer = (
	miraModel: MiraModel,
	graphElement: HTMLDivElement,
	useNestedRenderer: boolean
): PetrinetRenderer | NestedPetrinetRenderer => {
	const isStratified = isStratifiedModel(miraModel);
	// Debug start
	console.group('mmt info');
	console.log('# templates: ', miraModel.templates.length);
	console.log('# parameters: ', Object.keys(miraModel.parameters).length);
	console.log('stratified model: ', isStratified);
	console.groupEnd();
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
