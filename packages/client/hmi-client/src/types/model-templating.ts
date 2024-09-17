import type { Position } from '@/types/common';
import type { Model } from '@/types/Types';

export enum DecomposedModelTemplateTypes {
	ControlledConversion = 'Controlled conversion',
	ControlledDegradation = 'Controlled degradation',
	ControlledProduction = 'Controlled production',
	NaturalConversion = 'Natural conversion',
	NaturalDegradation = 'Natural degradation',
	NaturalProduction = 'Natural production',
	Observable = 'Observable'
}

export interface ModelTemplateCanvas {
	id: string;
	transform: {
		x: number;
		y: number;
		k: number;
	};
	cards: ModelTemplateCard[];
	junctions: ModelTemplateJunction[];
}

export interface ModelTemplateCard {
	id: string;
	model: Model;
	templateType: DecomposedModelTemplateTypes | null;
	x: number;
	y: number;
}

export interface ModelTemplateJunction {
	id: string;
	x: number;
	y: number;
	edges: ModelTemplateEdge[];
}

// Edge sources are always junctions so you'd reference the junction id for that
export interface ModelTemplateEdge {
	id: string;
	target: {
		cardId: string;
		portId: string;
	};
	points: Position[];
}

export interface OffsetValues {
	offsetLeft: number;
	offsetTop: number;
	offsetWidth: number;
	offsetHeight: number;
}
