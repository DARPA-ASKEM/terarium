import _ from 'lodash';
import { IGraph } from '@graph-scaffolder/index';
import {
	EdgeData,
	NodeData,
	PetrinetRenderer
} from '@/model-representation/petrinet/petrinet-renderer';
import { runDagreLayout } from '@/services/graph';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import {
	convertToIGraph,
	getStratificationType
} from '@/model-representation/petrinet/petrinet-service';
import { extractNestedMap } from '@/model-representation/petrinet/catlab-petri';
import {
	getMiraAMRPresentationData,
	extractNestedStratas
} from '@/model-representation/petrinet/mira-petri';
import { createMatrix2D } from '@/utils/pivot';
import type { Model } from '@/types/Types';

export const getPetrinetRenderer = (
	model: Model,
	graphElement: HTMLDivElement
): PetrinetRenderer | NestedPetrinetRenderer => {
	const strataType = getStratificationType(model);
	if (strataType === 'catlab') {
		return new NestedPetrinetRenderer({
			el: graphElement,
			useAStarRouting: false,
			useStableZoomPan: true,
			runLayout: runDagreLayout,
			dragSelector: 'no-drag',
			nestedMap: extractNestedMap(model)
		});
	}
	if (strataType === 'mira') {
		const presentationData = getMiraAMRPresentationData(model);

		const stateMatrixData = presentationData.stateMatrixData.map((d) => {
			const temp: any = _.cloneDeep(d);
			delete temp.id;
			delete temp.base;
			return Object.keys(temp);
		});
		const dims = _.uniq(_.flatten(stateMatrixData));
		dims.unshift('base');
		const nestedMap = extractNestedStratas(presentationData.stateMatrixData, dims);

		const groupedTransitions = _.groupBy(presentationData.transitionMatrixData, 'base');
		const transitionMatrixMap = {};
		Object.entries(groupedTransitions).forEach(([key, value]) => {
			const dimensions = Object.keys(value[0]).filter((k) => !['id', 'base'].includes(k));
			const matrixAttributes = createMatrix2D(value, dimensions, dimensions);
			transitionMatrixMap[key] = matrixAttributes.matrix;
		});

		return new NestedPetrinetRenderer({
			el: graphElement,
			useAStarRouting: false,
			useStableZoomPan: true,
			runLayout: runDagreLayout,
			dragSelector: 'no-drag',
			nestedMap,
			transitionMatrices: transitionMatrixMap,
			dims
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

export const getGraphData = (model: Model, isCollapsed: boolean): IGraph<NodeData, EdgeData> => {
	const strataType = getStratificationType(model);
	if (strataType && isCollapsed) {
		if (strataType === 'mira') {
			const presentationData = getMiraAMRPresentationData(model);
			return convertToIGraph(presentationData.compactModel as any);
		}
		return convertToIGraph(model.semantics?.span?.[0].system);
	}
	return convertToIGraph(model);
};
