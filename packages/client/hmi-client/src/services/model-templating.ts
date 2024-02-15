import { v4 as uuidv4 } from 'uuid';
import { cloneDeep, uniq, isEmpty, snakeCase, isEqual } from 'lodash';
import type { Position } from '@/types/common';
import type {
	ModelTemplateCard,
	ModelTemplateEdge,
	ModelTemplateJunction,
	ModelTemplates
} from '@/types/model-templating';
import { DecomposedModelTemplateTypes } from '@/types/model-templating';
import { KernelSessionManager } from '@/services/jupyter';
import { Model, Initial, ModelUnit } from '@/types/Types';
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
	parameter_units?: ModelUnit;
	parameter_description?: string;
	template_expression?: string;
	template_name?: string;
	new_id?: string;
	new_name?: string;
	new_expression?: string;
}

export const modelTemplateOptions = [
	naturalConversionTemplate,
	naturalProductionTemplate,
	naturalDegredationTemplate,
	controlledConversionTemplate,
	controlledProductionTemplate,
	controlledDegredationTemplate,
	observableTemplate
].map((modelTemplate: any) => {
	// TODO: Add templateCard attribute to Model later
	modelTemplate.metadata = {
		templateCard: {
			id: modelTemplate.header.name,
			name: modelTemplate.header.name,
			templateType: modelTemplate.header.name,
			x: 0,
			y: 0
		} as ModelTemplateCard
	};
	return modelTemplate;
});

function findCardIndexById(modelTemplates: ModelTemplates, id: string) {
	return modelTemplates.models.findIndex(({ metadata }) => metadata.templateCard.id === id);
}

export function initializeModelTemplates() {
	const modelTemplates: ModelTemplates = {
		id: uuidv4(),
		transform: { x: 0, y: 0, k: 1 },
		models: [],
		junctions: []
	};
	return modelTemplates;
}

/**
 * Add junction (exclusive to UI)
 */
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
		if (model.metadata) valuesToCheck.add(model.metadata.templateCard.name);

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

function appendNumberToModelVariables(modelTemplate: Model, number: number) {
	const templateWithNumber = cloneDeep(modelTemplate);

	const { states, transitions } = templateWithNumber.model;

	templateWithNumber.header.name += number;
	if (templateWithNumber.metadata) templateWithNumber.metadata.templateCard.name += number;

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
			if (rate.expression_mathml) {
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

export function prepareDecomposedTemplateAddition(
	modelTemplates: ModelTemplates,
	modelTemplate: Model
) {
	// Confirm a decomposed card is being added
	if (!modelTemplate.metadata?.templateCard) return null;
	const { templateType } = modelTemplate.metadata.templateCard;

	if (!Object.values(DecomposedModelTemplateTypes).includes(templateType)) return null;

	// Append a number to the model variables to avoid conflicts
	const decomposedTemplateToAdd = appendNumberToModelVariables(
		modelTemplate,
		determineNumberToAppend(modelTemplates.models)
	);
	return decomposedTemplateToAdd;
}

export function addTemplateInView(modelTemplates: ModelTemplates, modelTemplate: Model) {
	if (modelTemplate.metadata) modelTemplate.metadata.templateCard.id = uuidv4();
	modelTemplates.models.push(modelTemplate);
}

export function addDecomposedTemplateInKernel(
	kernelManager: KernelSessionManager,
	modelTemplates: ModelTemplates,
	modelTemplate: Model,
	outputCode: Function,
	syncWithMiraModel: Function,
	isTemplatePrepared = false // True when decomposed template in flattened view is being added
) {
	if (!modelTemplate.metadata?.templateCard) return;
	const { templateType } = modelTemplate.metadata.templateCard;

	const decomposedTemplateToAdd = isTemplatePrepared
		? modelTemplate
		: prepareDecomposedTemplateAddition(modelTemplates, modelTemplate);
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
				addTemplateArguments.parameter_units = parameters[0].unit ?? undefined;
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

	addTemplateInView(modelTemplates, decomposedTemplateToAdd);
}

/**
 * Remove template card
 */
export function removeTemplateInView(modelTemplates: ModelTemplates, id: string) {
	const index = findCardIndexById(modelTemplates, id);
	// Remove edges connected to the card
	modelTemplates.junctions.forEach((junction) => {
		junction.edges = junction.edges.filter((edge) => edge.target.cardId !== id);
	});
	junctionCleanUp(modelTemplates);
	modelTemplates.models.splice(index, 1); // Remove card
}

export function removeTemplateInKernel(
	kernelManager: KernelSessionManager,
	modelTemplates: ModelTemplates,
	id: string,
	outputCode: Function,
	syncWithMiraModel: Function
) {
	const index = findCardIndexById(modelTemplates, id);
	kernelManager
		.sendMessage('remove_template_request', {
			template_name: modelTemplates.models[index].metadata.templateCard.name
		})
		.register('remove_template_response', (d) => {
			removeTemplateInView(modelTemplates, id);
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
	if (decomposedModel.metadata) decomposedModel.metadata.templateCard.name = newName;
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

	if (
		flattenedModel.header.name === newName ||
		transitionIds.includes(newName) ||
		rateTargets.includes(newName)
	) {
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
	modelTemplates: ModelTemplates,
	junctionId: string,
	target: { cardId: string; portId: string },
	portPosition: Position,
	interpolatePointsFn?: Function
) {
	const junctionToDrawFrom = modelTemplates.junctions.find(({ id }) => id === junctionId);

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

		// Apply connection done in flattened view to decomposedTemplates
		// if (decomposedTemplates) {
		// 	const targetCardIndex = findCardIndexById(decomposedTemplates, target.cardId);
		// 	const targetCard = decomposedTemplates.models[targetCardIndex];
		// 	const targetPort = targetCard.model.transitions.find(({ id }) => id === target.portId);
		// 	targetPort.position = portPosition;
		// }
	}
}

export function addEdgeInKernel(
	kernelManager: KernelSessionManager,
	modelTemplates: ModelTemplates,
	junctionId: string,
	target: { cardId: string; portId: string },
	altTarget: { cardId: string; portId: string },
	portPosition: Position,
	outputCode: Function,
	syncWithMiraModel: Function,
	interpolatePointsFn?: Function
) {
	const templateName = modelTemplates.models.find(
		(model) => model.metadata.templateCard.id === target.cardId
	).metadata.templateCard.name;

	// Junction ID to draw from can be changed if the latter chosen port is already connected to a junction
	// TODO: The altPortPosition needs to be determined (by the altTarget?) so this case is still WIP
	let altJunctionId: string | null = null;
	modelTemplates.junctions.some(({ edges, id }: ModelTemplateJunction) => {
		const hasMatchingTarget = edges.some((edge: ModelTemplateEdge) => isEqual(edge.target, target));
		if (hasMatchingTarget) altJunctionId = id;
		return hasMatchingTarget;
	});
	if (altJunctionId) junctionId = altJunctionId;

	const junctionToDrawFrom = modelTemplates.junctions.find(({ id }) => id === junctionId);

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
			modelTemplates,
			junctionId,
			altJunctionId ? altTarget : target,
			portPosition, // TODO: altPortPosition case needs to be handled
			interpolatePointsFn
		);
	}
}

// TODO: There isn't a way to remove edges in the UI yet
// export function removeEdge(modelTemplates: ModelTemplates) {}

/**
 * Update/refresh flattened template
 */
export function updateFlattenedTemplateInView(model: Model, flattenedTemplates: ModelTemplates) {
	const flattenedModel = cloneDeep(model);
	flattenedModel.metadata = {
		templateCard: {
			id: model.id,
			name: model.header.name,
			x: 100,
			y: 100
		}
	};
	addTemplateInView(flattenedTemplates, flattenedModel);
}

/**
 * Flattened to decomposed
 */
export function flattenedToDecomposedInView(
	decomposedTemplates: ModelTemplates,
	templatesToAdd: Model[],
	interpolatePointsFn?: Function
) {
	// Insert template card data into decomposed models
	let yPos = 100;
	const allInitals: Initial[] = [];

	templatesToAdd.forEach((modelTemplate: Model) => {
		if (modelTemplate.semantics?.ode?.initials) {
			modelTemplate.metadata = {
				templateCard: {
					id: modelTemplate.header.name,
					name: modelTemplate.header.name,
					x: 100,
					y: yPos
				} as ModelTemplateCard
			};
			yPos += 200;

			addTemplateInView(decomposedTemplates, modelTemplate);
			allInitals.push(...modelTemplate.semantics.ode.initials);
		}
	});

	// Add junctions and edges based on initials
	// If an initial is repeated create a junction and two edges to connect them
	yPos = 100;
	const repeatedInitialTargets = uniq(
		allInitals
			// Remove initials that are not repeated
			.filter((initial) => {
				const repeated = allInitals.filter((i) => i.target === initial.target);
				return repeated.length > 1;
			})
			.map((initial) => initial.target)
	);

	for (let i = 0; i < repeatedInitialTargets.length; i++) {
		const junctionPosition = { x: 100, y: yPos };
		addJunction(decomposedTemplates, junctionPosition);
		yPos += 200;

		// Find cards that have the repeated initial and add edges to its junction
		const templatesWithRepeatedInitial = decomposedTemplates.models.filter((model) =>
			model.semantics.ode.initials.some(({ target }) => target === repeatedInitialTargets[i])
		);

		templatesWithRepeatedInitial.forEach((modelTemplate: Model) => {
			if (modelTemplate.metadata?.templateCard) {
				const templateCard = modelTemplate.metadata.templateCard;
				const junctionId =
					decomposedTemplates.junctions[decomposedTemplates.junctions.length - 1].id;
				const target = {
					cardId: templateCard.id,
					portId: repeatedInitialTargets[i]
				};

				// Port position is now card position + 168 (width of the card)
				const portPosition = { x: templateCard.x + 168, y: templateCard.y }; // FIXME: True port positions should be determined, for now this points to the top of the card
				addEdgeInView(decomposedTemplates, junctionId, target, portPosition, interpolatePointsFn);
			}
		});
	}
}

export function flattenedToDecomposedInKernel(
	kernelManager: KernelSessionManager,
	decomposedTemplates: ModelTemplates,
	interpolatePointsFn?: Function
) {
	kernelManager.sendMessage('amr_to_templates', {}).register('amr_to_templates_response', (d) => {
		flattenedToDecomposedInView(
			decomposedTemplates,
			d.content.templates as Model[],
			interpolatePointsFn
		);
	});
}

// Reflect flattened view connections in decomposed view - once done, everything in the flattened view will be "merged"
export function reflectFlattenedEditInDecomposedView(
	kernelManager: KernelSessionManager,
	flattenedTemplates: ModelTemplates,
	decomposedTemplates: ModelTemplates,
	outputCode: Function,
	syncWithMiraModel: Function,
	interpolatePointsFn?: Function
) {
	const templatesToMerge = flattenedTemplates.models;
	const flattenedJunctions = flattenedTemplates.junctions;

	// Add decomposed templates from flattened view to decomposed view
	templatesToMerge
		.slice(1) // Ignore the first one since it's the previous flattened one, the rest are decomposed
		.forEach((modelTemplate: Model) => {
			addDecomposedTemplateInKernel(
				kernelManager,
				decomposedTemplates,
				modelTemplate,
				outputCode,
				syncWithMiraModel,
				true
			);
		});

	// Add edges and potential junctions from flattened view to decomposed view
	flattenedJunctions.forEach((flatJunction: ModelTemplateJunction) => {
		const sharedPortId = flatJunction.edges[0].target.portId; // The state id shared by all ports is always the first port id

		// Find the junction in the decomposed view that corresponds to the flattened junction
		// let isNewJunction = false;
		let decompJunction = decomposedTemplates.junctions.find(
			({ edges }) => edges[0].target.portId === sharedPortId
		);
		// If one isn't found then create a new one
		if (!decompJunction) {
			// isNewJunction = true;
			addJunction(decomposedTemplates, {
				x: flatJunction.x,
				y: flatJunction.y
			});
			decompJunction = decomposedTemplates.junctions[decomposedTemplates.junctions.length - 1];

			// Draw the edge that would be drawn to the junction by default
			decomposedTemplates.models.forEach((modelTemplate: Model) => {
				if (modelTemplate.semantics?.ode?.initials && modelTemplate.metadata?.templateCard) {
					const initial = modelTemplate.semantics.ode.initials.find(
						({ target }) => target === sharedPortId
					);
					if (initial && decompJunction) {
						const { templateCard } = modelTemplate.metadata;
						addEdgeInView(
							decomposedTemplates,
							decompJunction.id,
							{ cardId: templateCard.id, portId: sharedPortId },
							{ x: templateCard.x + 168, y: templateCard.y }, // FIXME: True port positions should be determined, for now this points to the top of the card
							interpolatePointsFn
						);
					}
				}
			});
		}

		// Add edges from flattened view to decomposed view
		flatJunction.edges.forEach((flatEdge) => {
			if (!decompJunction) return;
			console.log(flatEdge);

			// If this is a new junction
			// Find the decomposed template that has the sharedPortId in the initials
			// const firstDecompTemplate =

			// const decompTemplateToLinkEdge = templatesToMerge.find(
			// 	({ models }) => metadata?.templateCard.id === flatEdge.target.portId
			// )?.id;

			// const newDecompTarget = {
			// 	cardId: flatEdge.target.cardId,
			// 	portId: flatEdge.target.portId
			// };

			// if (!decompEdge) return;console.log(templatesToMerge, flatEdge, decompJunction);

			// console.log(decompEdge);

			// 	addEdgeInKernel(
			// 		props.kernelManager,
			// 		decomposedTemplates,
			// 		newDecompJunction.id,
			// 		edge.target,
			// 		edge.points[0],
			// 		outputCode,
			// 		syncWithMiraModel,
			// 		interpolatePointsForCurve
			// 	);
		});
	});
}
