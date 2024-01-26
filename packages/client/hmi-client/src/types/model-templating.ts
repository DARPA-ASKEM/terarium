import type { Position } from '@/types/common';

export interface ModelTemplateEditor {
	id: string;
	name: string;
	description: string;
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
	name: string;
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
