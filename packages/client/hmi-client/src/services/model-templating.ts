import { v4 as uuidv4 } from 'uuid';
import type { Position } from '@/types/common';
import type { ModelTemplateEditor } from '@/types/model-templating';
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
		id: -1,
		name: modelTemplate.header.name,
		x: 0,
		y: 0
	};
	return modelTemplate;
});

export function emptyModelTemplateEditor(name = 'name', description = '') {
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

export function removeCard(modelTemplateEditor: ModelTemplateEditor, id: string) {
	const { models, junctions } = modelTemplateEditor;

	const edgesToRemove = junctions.map(({ edges }) =>
		edges.filter(({ target }) => target.cardId === id)
	);

	console.log(edgesToRemove);

	// Find edges to remove

	// Remove edges

	// If a junction ends up having one edge coming out of it, remove it

	modelTemplateEditor.models = models.filter((model) => model.metadata.templateCard.id !== id);
}

export function updateCardName(modelTemplateEditor: ModelTemplateEditor, name: string, id: string) {
	const index = findCardIndexById(modelTemplateEditor, id);
	modelTemplateEditor.models[index].metadata.templateCard.name = name;
}

export function addJunction(modelTemplateEditor: ModelTemplateEditor, position: Position) {
	modelTemplateEditor.junctions.push({
		id: uuidv4(),
		x: position.x,
		y: position.y,
		edges: []
	});

	// Check for edges always?????
}

export function addEdge(
	modelTemplateEditor: ModelTemplateEditor,
	junctionId: string,
	target: { cardId: string; portId: string },
	points: Position[]
) {
	modelTemplateEditor.junctions
		.find(({ id }) => id === junctionId)
		?.edges.push({ id: uuidv4(), target, points });
}
