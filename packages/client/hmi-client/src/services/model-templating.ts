import { v4 as uuidv4 } from 'uuid';
import { nextTick } from 'vue';
import { cloneDeep, uniq, isEmpty, snakeCase, isEqual } from 'lodash';
import type { Position } from '@/types/common';
import type {
	ModelTemplateCard,
	ModelTemplateEdge,
	ModelTemplateJunction,
	ModelTemplateCanvas,
	OffsetValues
} from '@/types/model-templating';
import { DecomposedModelTemplateTypes } from '@/types/model-templating';
import { KernelSessionManager } from '@/services/jupyter';
import { Model, Initial } from '@/types/Types';
import { logger } from '@/utils/logger';
import naturalConversionTemplate from './model-templates/natural-conversion.json';
import naturalProductionTemplate from './model-templates/natural-production.json';
import naturalDegredationTemplate from './model-templates/natural-degradation.json';
import controlledConversionTemplate from './model-templates/controlled-conversion.json';
import controlledProductionTemplate from './model-templates/controlled-production.json';
import controlledDegredationTemplate from './model-templates/controlled-degradation.json';
import observableTemplate from './model-templates/observable.json';

interface AddTemplateArguments {
	subject_name?: string;
	subject_initial_value?: string;
	outcome_name?: string;
	outcome_initial_value?: string;
	controller_name?: string;
	controller_initial_value?: string;
	parameter_name?: string;
	parameter_value?: number;
	parameter_units?: string;
	parameter_description?: string;
	template_expression?: string;
	template_name?: string;
	new_id?: string;
	new_name?: string;
	new_expression?: string;
}

export const modelTemplateCardOptions: ModelTemplateCard[] = [
	naturalConversionTemplate,
	naturalProductionTemplate,
	naturalDegredationTemplate,
	controlledConversionTemplate,
	controlledProductionTemplate,
	controlledDegredationTemplate,
	observableTemplate
].map((model: Model) => ({
	id: '',
	model,
	templateType: model.header.name as DecomposedModelTemplateTypes,
	x: 0,
	y: 0
}));

function findCardIndexById(canvas: ModelTemplateCanvas, id: string) {
	return canvas.cards.findIndex((card) => card.id === id);
}

export function initializeCanvas() {
	const canvas: ModelTemplateCanvas = {
		id: uuidv4(),
		transform: { x: 0, y: 0, k: 1 },
		cards: [],
		junctions: []
	};
	return canvas;
}

/**
 * Add junction (exclusive to UI)
 */
export function addJunction(canvas: ModelTemplateCanvas, portPosition: Position) {
	canvas.junctions.push({
		id: uuidv4(),
		x: portPosition.x + 500,
		y: portPosition.y - 10,
		edges: []
	});
}

export function junctionCleanUp(canvas: ModelTemplateCanvas) {
	// If a junction ends up having one edge coming out of it, remove it
	canvas.junctions = canvas.junctions.filter(({ edges }) => edges.length > 1);
}

/**
 * Add/remove template cards and edges in the UI (view) and kernel are separate functions
 * The view functions are always called at the end of the kernel functions
 * There are cases where the view functions can be called without the kernel functions
 */

/**
 * Add template card
 */

// Helper functions
function determineNumberToAppend(models: Model[]) {
	// Collect values to check for numbers
	const valuesToCheck: Set<string> = new Set();
	models.forEach((model) => {
		valuesToCheck.add(model.header.name);

		const { states, transitions } = model.model;
		[...states, ...transitions].forEach((variable) => {
			const { id, name, target, properties, input, output } = variable;

			// Common properties
			if (id) valuesToCheck.add(id);
			if (name) valuesToCheck.add(name);
			if (target) valuesToCheck.add(target);
			// Transition properties
			if (properties?.name) valuesToCheck.add(properties.name);
			if (input) input.forEach((value: string) => valuesToCheck.add(value));
			if (output) output.forEach((value: string) => valuesToCheck.add(value));
		});

		if (model?.semantics?.ode) {
			const { rates, initials, parameters, observables } = model.semantics.ode;

			const ode: any[] = [...rates];
			if (initials) ode.push(...initials);
			if (parameters) ode.push(...parameters);
			if (observables) ode.push(...observables);

			ode.forEach((variable) => {
				const { id, name, target } = variable;

				if (id) valuesToCheck.add(id);
				if (name) valuesToCheck.add(name);
				if (target) valuesToCheck.add(target);
			});
		}
	});
	// Extract the numbers from the values
	const lastNumbers = uniq(
		Array.from(valuesToCheck)
			.map((str: string) => parseInt(str.slice(-1), 10))
			.filter((num) => !Number.isNaN(num))
	);
	const number = !isEmpty(lastNumbers) ? Math.max(...lastNumbers) + 1 : 0;
	return number;
}

function appendNumberToModelVariables(model: Model, number: number) {
	const templateWithNumber = cloneDeep(model);

	const { states, transitions } = templateWithNumber.model;

	templateWithNumber.header.name += number;

	states.forEach((state) => {
		state.id += number;
		state.name += number;
	});

	transitions.forEach((transition) => {
		transition.id += number;
		transition.properties.name += number;
		transition.input = transition.input.map((input) => input + number);
		transition.output = transition.output.map((output) => output + number);
	});

	if (templateWithNumber?.semantics?.ode) {
		const { rates, initials, parameters, observables } = templateWithNumber.semantics.ode;

		initials?.forEach((initial) => {
			initial.target += number;
		});
		parameters?.forEach((parameter) => {
			parameter.id += number;
		});

		rates.forEach((rate) => {
			rate.target += number;
			// Within the expression attributes update the targets to match the new ones
			rate.expression = rate.expression.replace(/([a-zA-Z])/g, (match) => match + number);
			if (rate.expression_mathml && !isEmpty(rate.expression_mathml)) {
				rate.expression_mathml = rate.expression_mathml.replace(
					/<ci>([a-zA-Z])<\/ci>/g,
					(_, letter: string) => `<ci>${letter + number}</ci>`
				);
			}
		});

		observables?.forEach((observable) => {
			observable.id += number;
			if (observable.name) observable.name += number;
		});
	}
	return templateWithNumber;
}

export function prepareDecomposedTemplateAddition(canvas: ModelTemplateCanvas, card: ModelTemplateCard) {
	// Append a number to the model variables to avoid conflicts
	const decomposedTemplateToAdd = appendNumberToModelVariables(
		card.model,
		determineNumberToAppend(canvas.cards.map((c) => c.model))
	);
	card.model = decomposedTemplateToAdd;
	return decomposedTemplateToAdd;
}

export function addTemplateInView(canvas: ModelTemplateCanvas, card: ModelTemplateCard) {
	if (isEmpty(card.id)) card.id = uuidv4(); // Assign an ID if it doesn't exist (decomposed sidebar ones have none)
	canvas.cards.push(card);
}

export function addDecomposedTemplateInKernel(
	kernelManager: KernelSessionManager,
	canvas: ModelTemplateCanvas,
	card: ModelTemplateCard,
	outputCode: Function,
	syncWithMiraModel: Function,
	isTemplatePrepared = false // True when decomposed template in flattened view is being added
) {
	const { model, templateType } = card;
	// Confirm a decomposed card is being added
	if (!templateType || !Object.values(DecomposedModelTemplateTypes).includes(templateType)) {
		logger.error('Only decomposed templates can be added.');
		return;
	}

	const decomposedTemplateToAdd = isTemplatePrepared ? model : prepareDecomposedTemplateAddition(canvas, card);
	if (!decomposedTemplateToAdd) return;

	const addTemplateArguments: AddTemplateArguments = {};

	if (templateType !== DecomposedModelTemplateTypes.Observable) {
		const uniqueName = decomposedTemplateToAdd.header.name; // Now save a version of the name with the number appended
		addTemplateArguments.template_name = uniqueName;

		if (decomposedTemplateToAdd?.semantics?.ode) {
			const { rates, initials, parameters } = cloneDeep(decomposedTemplateToAdd.semantics.ode); // Clone to avoid mutation on initials when splitting controllers

			// Add rate to the arguments
			addTemplateArguments.template_expression = rates[0].expression;

			// Add parameter to the arguments
			if (parameters) {
				addTemplateArguments.parameter_name = parameters[0].id;
				addTemplateArguments.parameter_value = parameters[0].value;
				addTemplateArguments.parameter_units = parameters[0]?.units?.expression;
				addTemplateArguments.parameter_description = parameters[0].description;
			}

			// Add intial related arguments
			if (initials) {
				const { transitions } = decomposedTemplateToAdd.model;
				if (
					templateType === DecomposedModelTemplateTypes.ControlledConversion ||
					templateType === DecomposedModelTemplateTypes.ControlledDegradation ||
					templateType === DecomposedModelTemplateTypes.ControlledProduction
				) {
					// Extract controller from initials and add it to the arguments
					const { input, output } = transitions[0];
					if (input?.[0] === output?.[0]) {
						const index = initials.findIndex((initial) => initial.target === input[0]);
						const controller = initials[index];

						// Add controller to the arguments
						addTemplateArguments.controller_name = controller.target;
						addTemplateArguments.controller_initial_value = controller.expression;

						// Remove controller from initials
						initials.splice(index, 1);
					}
				}

				// Now that there are no controllers in initals we can add subject/outcome to the arguments
				if (
					templateType === DecomposedModelTemplateTypes.NaturalConversion ||
					templateType === DecomposedModelTemplateTypes.ControlledConversion
				) {
					// If it's a conversion template, the first two initials are the subject then outcome
					addTemplateArguments.subject_name = initials[0].target;
					addTemplateArguments.subject_initial_value = initials[0].expression;
					addTemplateArguments.outcome_name = initials[1].target;
					addTemplateArguments.outcome_initial_value = initials[1].expression;
				} else if (
					templateType === DecomposedModelTemplateTypes.NaturalProduction ||
					templateType === DecomposedModelTemplateTypes.ControlledProduction
				) {
					// If it's a production template, the first initial is the outcome
					addTemplateArguments.outcome_name = initials[0].target;
					addTemplateArguments.outcome_initial_value = initials[0].expression;
				} else {
					// If it's a degradation template, the first initial is the subject
					addTemplateArguments.subject_name = initials[0].target;
					addTemplateArguments.subject_initial_value = initials[0].expression;
				}
			}
		}
	} else if (decomposedTemplateToAdd?.semantics?.ode?.observables) {
		const { observables } = decomposedTemplateToAdd.semantics.ode;
		addTemplateArguments.new_id = observables[0].id;
		addTemplateArguments.new_name = decomposedTemplateToAdd.header.name;
		addTemplateArguments.new_expression = observables[0].expression_mathml;
	}

	kernelManager
		.sendMessage(`add_${snakeCase(templateType)}_template_request`, addTemplateArguments)
		.register(`add_${snakeCase(templateType)}_template_response`, (d) => {
			outputCode(d);
		})
		.register('model_preview', (d) => {
			syncWithMiraModel(d);
		});

	addTemplateInView(canvas, card);
}

/**
 * Remove template card
 */
export function removeTemplateInView(canvas: ModelTemplateCanvas, id: string) {
	const index = findCardIndexById(canvas, id);
	// Remove edges connected to the card
	canvas.junctions.forEach((junction) => {
		junction.edges = junction.edges.filter((edge) => edge.target.cardId !== id);
	});
	junctionCleanUp(canvas);
	canvas.cards.splice(index, 1); // Remove card
}

export function removeTemplateInKernel(
	kernelManager: KernelSessionManager,
	canvas: ModelTemplateCanvas,
	id: string,
	outputCode: Function,
	syncWithMiraModel: Function
) {
	const index = findCardIndexById(canvas, id);
	const templateName = canvas.cards[index]?.model.header.name;

	if (!templateName) {
		logger.error('Template name not found.');
		return;
	}

	kernelManager
		.sendMessage('remove_template_request', {
			template_name: templateName
		})
		.register('remove_template_response', (d) => {
			removeTemplateInView(canvas, id);
			outputCode(d);
		})
		.register('model_preview', (d) => {
			syncWithMiraModel(d);
		});
}

/**
 * Update decomposed template name
 */
export function updateDecomposedTemplateNameInView(decomposedModel: Model, newName: string) {
	decomposedModel.header.name = newName;
	if (decomposedModel.model.transitions[0] && decomposedModel?.semantics?.ode?.rates[0]) {
		decomposedModel.model.transitions[0].id = newName;
		decomposedModel.model.transitions[0].properties.name = newName;
		decomposedModel.semantics.ode.rates[0].target = newName;
	}
}

export function updateDecomposedTemplateNameInKernel(
	kernelManager: KernelSessionManager,
	decomposedModel: Model,
	flattenedModel: Model,
	newName: string,
	outputCode: Function,
	syncWithMiraModel: Function
) {
	// Return if old and new name are the same
	if (decomposedModel.header.name === newName) return;

	// Sanity check: Make sure the new template name is unique
	const transitionIds = flattenedModel.model.transitions.map(({ id }) => id);
	const rateTargets = flattenedModel.semantics?.ode?.rates
		? flattenedModel.semantics.ode.rates.map(({ target }) => target)
		: [];

	if (flattenedModel.header.name === newName || transitionIds.includes(newName) || rateTargets.includes(newName)) {
		logger.error('Your template name needs to be unique. Please try again.');
		return;
	}

	// Apply name change
	kernelManager
		.sendMessage('replace_template_name_request', {
			old_name: decomposedModel.header.name,
			new_name: newName
		})
		.register('replace_template_name_response', (d) => {
			updateDecomposedTemplateNameInView(decomposedModel, newName);
			outputCode(d);
		})
		.register('model_preview', (d) => {
			syncWithMiraModel(d);
		});
}

/**
 * Add edge to model template
 */
export function addEdgeInView(
	canvas: ModelTemplateCanvas,
	junctionId: string,
	target: { cardId: string; portId: string },
	portPosition: Position,
	interpolatePointsFn?: Function
) {
	const junctionToDrawFrom = canvas.junctions.find(({ id }) => id === junctionId);

	if (junctionToDrawFrom) {
		const points: Position[] = [
			{ x: junctionToDrawFrom.x + 10, y: junctionToDrawFrom.y + 10 },
			{ x: portPosition.x, y: portPosition.y }
		];

		junctionToDrawFrom.edges.push({
			id: uuidv4(),
			target,
			points: interpolatePointsFn ? interpolatePointsFn(...points) : points
		});
	}
}

export function addEdgeInKernel(
	kernelManager: KernelSessionManager,
	canvas: ModelTemplateCanvas,
	junctionId: string,
	target: { cardId: string; portId: string },
	altTarget: { cardId: string; portId: string },
	portPosition: Position,
	outputCode: Function,
	syncWithMiraModel: Function,
	interpolatePointsFn?: Function
) {
	const templateName = canvas.cards.find((card) => card.id === target.cardId)?.model.header.name;

	// Maybe make this its own function to be used in addEdgeInView
	// Junction ID to draw from can be changed if the latter chosen port is already connected to a junction
	// TODO: The altPortPosition needs to be determined (by the altTarget?) so this case is still WIP
	let altJunctionId: string | null = null;
	canvas.junctions.some(({ edges, id }: ModelTemplateJunction) => {
		const hasMatchingTarget = edges.some((edge: ModelTemplateEdge) => isEqual(edge.target, target));
		if (hasMatchingTarget) altJunctionId = id;
		return hasMatchingTarget;
	});
	if (altJunctionId) junctionId = altJunctionId;

	const junctionToDrawFrom = canvas.junctions.find(({ id }) => id === junctionId);

	// Once ports are connected they share the same state name in the flattened model
	if (junctionToDrawFrom && junctionToDrawFrom.edges.length >= 1 && outputCode) {
		kernelManager
			.sendMessage('replace_state_name_request', {
				template_name: templateName,
				old_name: altJunctionId ? altTarget.portId : target.portId,
				new_name: junctionToDrawFrom.edges[0].target.portId
			})
			.register('replace_state_name_response', (d) => {
				outputCode(d);
			})
			.register('model_preview', (d) => {
				syncWithMiraModel(d);
			});

		addEdgeInView(
			canvas,
			junctionId,
			altJunctionId ? altTarget : target,
			portPosition, // TODO: altPortPosition case needs to be handled
			interpolatePointsFn
		);
	}
}

// TODO: There isn't a way to remove edges in the UI yet
// export function removeEdge(canvas: ModelTemplateCanvas) {}

/**
 * Update/refresh flattened template
 */
export function updateFlattenedTemplateInView(flattenedCanvas: ModelTemplateCanvas, model: Model) {
	const flattenedCard: ModelTemplateCard = {
		id: uuidv4(),
		model,
		templateType: null,
		x: 100,
		y: 100
	};

	addTemplateInView(flattenedCanvas, flattenedCard);
}

export function getElementOffsetValues(element: HTMLElement): OffsetValues {
	const { offsetLeft, offsetTop, offsetWidth, offsetHeight } = element;
	return { offsetLeft, offsetTop, offsetWidth, offsetHeight };
}

// Helper function for finding port position when auto drawing junctions and edges
function getPortPosition(
	card: ModelTemplateCard,
	portId: string,
	potentialPortOffsetValues?: Map<string, OffsetValues>
) {
	const cardWidth = 168;
	const id = `${card.id}-${portId}`;

	// Default to fallback values for port position (top right of the card)
	let x = card.x + cardWidth;
	let y = card.y;
	// Get the offset values of the port element to determine its position
	let portOffsetValues: OffsetValues | null = null;
	if (potentialPortOffsetValues) {
		const potentialPortPosition = potentialPortOffsetValues.get(id);
		if (potentialPortPosition) portOffsetValues = potentialPortPosition;
	} else {
		const portElement = document.getElementById(id);
		if (portElement) portOffsetValues = getElementOffsetValues(portElement);
	}

	if (portOffsetValues) {
		x = card.x + portOffsetValues.offsetLeft + portOffsetValues.offsetWidth - 10;
		y = card.y + portOffsetValues.offsetTop + portOffsetValues.offsetHeight / 2;
	}
	return { x, y };
}

/**
 * Flattened to decomposed
 */
export async function flattenedToDecomposedInView(
	decomposedCanvas: ModelTemplateCanvas,
	templatesToAdd: Model[],
	interpolatePointsFn?: Function
) {
	// Insert template card data into decomposed models
	let yPos = 100;
	const allInitals: Initial[] = [];

	templatesToAdd.forEach((model: Model) => {
		if (model.semantics?.ode?.initials) {
			const card: ModelTemplateCard = {
				id: uuidv4(),
				model,
				x: 100,
				y: yPos,
				templateType: null
			};
			yPos += 200;

			addTemplateInView(decomposedCanvas, card);
			allInitals.push(...model.semantics.ode.initials);
		}
	});

	// Make sure cards are rendered before junctions and edges (this is required to determine port positions)
	await nextTick();

	// Add junctions and edges based on initials
	// If an initial is repeated create a junction and two edges to connect them
	const repeatedInitialTargets = uniq(
		allInitals
			// Remove initials that are not repeated
			.filter((initial) => {
				const repeated = allInitals.filter((i) => i.target === initial.target);
				return repeated.length > 1;
			})
			.map((initial) => initial.target)
	);

	repeatedInitialTargets.forEach((repeatedInitialTarget) => {
		// Find cards that have the repeated initial and add edges to its junction
		const templatesWithRepeatedInitial = decomposedCanvas.cards.filter((card: ModelTemplateCard) =>
			card.model.semantics?.ode?.initials?.some(({ target }) => target === repeatedInitialTarget)
		);

		// Collect port positions that the junction will be connected to
		const portPositions: Position[] = [];
		templatesWithRepeatedInitial.forEach((card: ModelTemplateCard) => {
			portPositions.push(getPortPosition(card, repeatedInitialTarget));
		});

		// Add junction, Y position is the center of the highest and lowest port
		const portPositionsY = portPositions.map(({ y }) => y);
		const lowestPortY = Math.min(...portPositionsY);
		const highestPortY = Math.max(...portPositionsY);
		const junctionPosition = {
			x: 200,
			y: lowestPortY + (highestPortY - lowestPortY) / 2
		};
		addJunction(decomposedCanvas, junctionPosition);
		const junctionId = decomposedCanvas.junctions[decomposedCanvas.junctions.length - 1].id;

		// Once junction is added, add edges
		templatesWithRepeatedInitial.forEach((card: ModelTemplateCard, index: number) => {
			const target = {
				cardId: card.id,
				portId: repeatedInitialTarget
			};
			addEdgeInView(decomposedCanvas, junctionId, target, portPositions[index], interpolatePointsFn);
		});
	});
}

export function flattenedToDecomposedInKernel(
	kernelManager: KernelSessionManager,
	decomposedCanvas: ModelTemplateCanvas,
	interpolatePointsFn?: Function
) {
	kernelManager.sendMessage('amr_to_templates', {}).register('amr_to_templates_response', (d) => {
		flattenedToDecomposedInView(decomposedCanvas, d.content.templates as Model[], interpolatePointsFn);
	});
}

/*
 * Reflect flattened view connections in decomposed view - once done, everything in the flattened view will be "merged"
 */

// Helper returns template card and value of decomposed port that matches what's in the flattened view
function findTemplateCardForNewEdge(
	decomposedCards: ModelTemplateCard[],
	portIdToMatch: string
): ModelTemplateCard | null {
	let foundCard: ModelTemplateCard | null = null;
	decomposedCards.forEach((card: ModelTemplateCard) => {
		const initials = card.model.semantics?.ode?.initials;
		if (initials && !foundCard) {
			const initial = initials.find(({ target }) => target === portIdToMatch);
			if (!initial) return;
			foundCard = card;
		}
	});
	return foundCard;
}

export async function reflectFlattenedEditInDecomposedView(
	kernelManager: KernelSessionManager,
	flattenedCanvas: ModelTemplateCanvas,
	decomposedCanvas: ModelTemplateCanvas,
	decomposedPortOffsetValues: Map<string, OffsetValues>,
	outputCode: Function,
	syncWithMiraModel: Function,
	interpolatePointsFn?: Function
) {
	// Terminate if junctions in flat view are not connected to any ports of the flattened model
	let isReadyToReflect = false;
	const flattenedInitialTargets =
		flattenedCanvas.cards[0].model.semantics?.ode?.initials?.map(({ target }) => target) ?? [];
	flattenedCanvas.junctions.forEach((flatJunction) => {
		isReadyToReflect = flatJunction.edges.some((edge) => flattenedInitialTargets.includes(edge.target.portId));
	});
	if (!isReadyToReflect) return;

	// Add decomposed templates from flattened view to decomposed view
	flattenedCanvas.cards
		.slice(1) // Ignore the first one since it's the previous flattened one, the rest are decomposed
		.forEach((card: ModelTemplateCard) => {
			// Specify position of card for decomposed view (good enough for now)
			card.x = 100;
			card.y = 100 + decomposedCanvas.cards.length * 200;
			addDecomposedTemplateInKernel(kernelManager, decomposedCanvas, card, outputCode, syncWithMiraModel, true);
		});

	// Make sure cards are rendered before junctions and edges (this is required to determine port positions)
	await nextTick();

	// Add edges and potential junctions from flattened view to decomposed view
	flattenedCanvas.junctions.forEach((flatJunction: ModelTemplateJunction) => {
		const sharedPortId = flatJunction.edges[0].target.portId; // The state id shared by all ports is always the first port id

		// Find the junction in the decomposed view that corresponds to the flattened junction
		let decompJunctionId = decomposedCanvas.junctions.find(({ edges }) => edges[0].target.portId === sharedPortId)?.id;
		// If junction isn't found a new one must be created along with the edge that would be drawn to the junction by default
		if (!decompJunctionId) {
			// Finds the decomposed template that has the port used in the flattened view
			const templateCard = findTemplateCardForNewEdge(decomposedCanvas.cards, sharedPortId);
			if (!templateCard) return;
			const portPosition = getPortPosition(templateCard, sharedPortId, decomposedPortOffsetValues); // Pass decomposed port offset values that were saved when we switched to the flattened view

			addJunction(decomposedCanvas, portPosition);
			decompJunctionId = decomposedCanvas.junctions[decomposedCanvas.junctions.length - 1].id;

			addEdgeInView(
				decomposedCanvas,
				decompJunctionId,
				{ cardId: templateCard.id, portId: sharedPortId },
				portPosition,
				interpolatePointsFn
			);
		}

		// Add edges from flattened view to decomposed view
		flatJunction.edges.forEach((flatEdge) => {
			if (!decompJunctionId || flatEdge.target.portId === sharedPortId) return; // The edge connect to the sharedPortId is already created

			// If flat edge port matches an initial in the decomposed template, add an edge to the junction
			const templateCard = findTemplateCardForNewEdge(decomposedCanvas.cards, flatEdge.target.portId);
			if (!templateCard) return;
			const portPosition = getPortPosition(templateCard, flatEdge.target.portId); // This works since the card will exist in both views and the offset values for the ports are the same

			addEdgeInKernel(
				kernelManager,
				decomposedCanvas,
				decompJunctionId,
				{ cardId: templateCard.id, portId: flatEdge.target.portId },
				{ cardId: '', portId: '' },
				portPosition,
				outputCode,
				syncWithMiraModel,
				interpolatePointsFn
			);
		});
	});
}
