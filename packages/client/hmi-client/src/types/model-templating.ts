import type { Position } from '@/types/common';

export enum DecomposedModelTemplateTypes {
	ControlledConversion = 'Controlled conversion',
	ControlledDegradation = 'Controlled degradation',
	ControlledProduction = 'Controlled production',
	NaturalConversion = 'Natural conversion',
	NaturalDegradation = 'Natural degradation',
	NaturalProduction = 'Natural production',
	Observable = 'Observable'
}

export interface ModelTemplates {
	id: string;
	transform: {
		x: number;
		y: number;
		k: number;
	};
	models: any[];
	junctions: ModelTemplateJunction[];
}

export interface ModelTemplateCard {
	id: string;
	name: string; // FIXME: name may not be necessary if it's already in model.header
	x: number;
	y: number;
	// For collisionFn
	width: number;
	height: number;
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
