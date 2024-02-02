import { v4 as uuidv4 } from 'uuid';
import type { Position } from '@/types/common';
import type { Model } from '@/types/Types';
import type { ModelTemplateCard, ModelTemplates } from '@/types/model-templating';
import naturalConversion from './model-templates/natural-conversion.json';
import naturalProduction from './model-templates/natural-production.json';
import naturalDegredation from './model-templates/natural-degradation.json';
import controlledConversion from './model-templates/controlled-conversion.json';
import controlledProduction from './model-templates/controlled-production.json';
import controlledDegredation from './model-templates/controlled-degradation.json';
import observable from './model-templates/observable.json';

export const modelTemplateOptions = [
	naturalConversion,
	naturalProduction,
	naturalDegredation,
	controlledConversion,
	controlledProduction,
	controlledDegredation,
	observable
].map((modelTemplate: any) => {
	// TODO: Add templateCard attribute to Model later
	modelTemplate.metadata.templateCard = {
		id: modelTemplate.header.name,
		name: modelTemplate.header.name,
		x: 0,
		y: 0
	} as ModelTemplateCard;
	return modelTemplate;
});

export function initializeModelTemplates(model: Model | null = null) {
	const name = model?.header?.name ?? 'name';
	const description = model?.header?.description ?? 'description';

	const modelTemplates: ModelTemplates = {
		id: uuidv4(),
		name,
		description,
		transform: { x: 0, y: 0, k: 1 },
		models: [],
		junctions: []
	};
	return modelTemplates;
}

function findCardIndexById(modelTemplates: ModelTemplates, id: string) {
	return modelTemplates.models.findIndex(({ metadata }) => metadata.templateCard.id === id);
}

export function addCard(modelTemplates: ModelTemplates, modelTemplate: any) {
	modelTemplate.metadata.templateCard.id = uuidv4();
	modelTemplates.models.push(modelTemplate);
}

export function updateCardName(modelTemplates: ModelTemplates, name: string, id: string) {
	const index = findCardIndexById(modelTemplates, id);
	modelTemplates.models[index].metadata.templateCard.name = name;
}

export function removeCard(modelTemplates: ModelTemplates, id: string) {
	// Remove edges connected to the card
	modelTemplates.junctions.forEach((junction) => {
		junction.edges = junction.edges.filter((edge) => edge.target.cardId !== id);
	});
	junctionCleanUp(modelTemplates);

	// Remove card
	modelTemplates.models = modelTemplates.models.filter(
		(model) => model.metadata.templateCard.id !== id
	);
}

export function addJunction(modelTemplates: ModelTemplates, portPosition: Position) {
	modelTemplates.junctions.push({
		id: uuidv4(),
		x: portPosition.x + 500,
		y: portPosition.y - 10,
		edges: []
	});
}

export function junctionCleanUp(modelTemplates: ModelTemplates) {
	// If a junction ends up having one edge coming out of it, remove it
	modelTemplates.junctions = modelTemplates.junctions.filter(({ edges }) => edges.length > 1);
}

export function addEdge(
	modelTemplates: ModelTemplates,
	junctionId: string,
	target: { cardId: string; portId: string },
	portPosition: Position,
	interpolatePointsFn?: Function
) {
	const index = modelTemplates.junctions.findIndex(({ id }) => id === junctionId);
	const junctionToDrawFrom = modelTemplates.junctions[index];

	const points: Position[] = [
		{ x: junctionToDrawFrom.x + 10, y: junctionToDrawFrom.y + 10 },
		{ x: portPosition.x, y: portPosition.y }
	];

	modelTemplates.junctions[index].edges.push({
		id: uuidv4(),
		target,
		points: interpolatePointsFn ? interpolatePointsFn(...points) : points
	});
}

// TODO: There isn't a way to do this in the UI yet
// export function removeEdge(modelTemplates: ModelTemplates) {}
