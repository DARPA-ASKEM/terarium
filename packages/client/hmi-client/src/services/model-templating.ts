import { v4 as uuidv4 } from 'uuid';
import type { Position } from '@/types/common';
import type { Model } from '@/types/Types';
import type { ModelTemplateCard, ModelTemplateEditor } from '@/types/model-templating';
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

export function initializeModelTemplateEditor(model: Model | null = null) {
	const name = model?.header?.name ?? 'name';
	const description = model?.header?.description ?? 'description';

	const modelTemplateEditor: ModelTemplateEditor = {
		id: uuidv4(),
		name,
		description,
		transform: { x: 0, y: 0, k: 1 },
		models: [],
		junctions: []
	};
	return modelTemplateEditor;
}

function findCardIndexById(modelTemplateEditor: ModelTemplateEditor, id: string) {
	return modelTemplateEditor.models.findIndex(({ metadata }) => metadata.templateCard.id === id);
}

export function addCard(modelTemplateEditor: ModelTemplateEditor, modelTemplate: any) {
	modelTemplate.metadata.templateCard.id = uuidv4();
	modelTemplateEditor.models.push(modelTemplate);
}

export function updateCardName(modelTemplateEditor: ModelTemplateEditor, name: string, id: string) {
	const index = findCardIndexById(modelTemplateEditor, id);
	modelTemplateEditor.models[index].metadata.templateCard.name = name;
}

export function removeCard(modelTemplateEditor: ModelTemplateEditor, id: string) {
	// Remove edges connected to the card
	modelTemplateEditor.junctions.forEach((junction) => {
		junction.edges = junction.edges.filter((edge) => edge.target.cardId !== id);
	});
	junctionCleanUp(modelTemplateEditor);

	// Remove card
	modelTemplateEditor.models = modelTemplateEditor.models.filter(
		(model) => model.metadata.templateCard.id !== id
	);
}

export function addJunction(modelTemplateEditor: ModelTemplateEditor, portPosition: Position) {
	modelTemplateEditor.junctions.push({
		id: uuidv4(),
		x: portPosition.x + 500,
		y: portPosition.y - 10,
		edges: []
	});
}

export function junctionCleanUp(modelTemplateEditor: ModelTemplateEditor) {
	// If a junction ends up having one edge coming out of it, remove it
	modelTemplateEditor.junctions = modelTemplateEditor.junctions.filter(
		({ edges }) => edges.length > 1
	);
}

export function addEdge(
	modelTemplateEditor: ModelTemplateEditor,
	junctionId: string,
	target: { cardId: string; portId: string },
	portPosition: Position,
	interpolatePointsFn?: Function
) {
	const index = modelTemplateEditor.junctions.findIndex(({ id }) => id === junctionId);
	const junctionToDrawFrom = modelTemplateEditor.junctions[index];

	const points: Position[] = [
		{ x: junctionToDrawFrom.x + 10, y: junctionToDrawFrom.y + 10 },
		{ x: portPosition.x, y: portPosition.y }
	];

	modelTemplateEditor.junctions[index].edges.push({
		id: uuidv4(),
		target,
		points: interpolatePointsFn ? interpolatePointsFn(...points) : points
	});
}

// TODO: There isn't a way to do this in the UI yet
// export function removeEdge(modelTemplateEditor: ModelTemplateEditor) {}
