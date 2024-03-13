import { runDagreLayout } from '@/services/graph';
import { MiraModel } from '@/model-representation/mira/mira-common';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NestedPetrinetRenderer } from './petrinet/nested-petrinet-renderer';

export const getModelRenderer = (
	_mmt: MiraModel,
	graphElement: HTMLDivElement
): PetrinetRenderer | NestedPetrinetRenderer =>
	// FIXME: Need to handle collapsed/full
	new PetrinetRenderer({
		el: graphElement,
		useAStarRouting: false,
		useStableZoomPan: true,
		runLayout: runDagreLayout,
		dragSelector: 'no-drag'
	});
