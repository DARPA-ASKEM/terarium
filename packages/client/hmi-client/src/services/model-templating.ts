import { v4 as uuidv4 } from 'uuid';
import { cloneDeep, uniq } from 'lodash';
import type { Position } from '@/types/common';
import type { ModelTemplateCard, ModelTemplates } from '@/types/model-templating';
import { DecomposedModelTemplateTypes } from '@/types/model-templating';
import { KernelSessionManager } from '@/services/jupyter';
import { Model } from '@/types/Types';
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
	modelTemplate.metadata.templateCard = {
		id: modelTemplate.header.name,
		name: modelTemplate.header.name,
		x: 0,
		y: 0
	} as ModelTemplateCard;
	return modelTemplate;
});

export function initializeModelTemplates() {
	const modelTemplates: ModelTemplates = {
		id: uuidv4(),
		transform: { x: 0, y: 0, k: 1 },
		models: [],
		junctions: []
	};
	return modelTemplates;
}

export function findCardIndexById(modelTemplates: ModelTemplates, id: string) {
	return modelTemplates.models.findIndex(({ metadata }) => metadata.templateCard.id === id);
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

// Designed to work with the decomposed model templates
function appendNumberToModelVariables(modelTemplate: any, models: any[]) {
	// Helper function - gets last number in the string and compare it to the highest number
	function updateHighestNumber(str: string, highestNumber: number) {
		const lastNumber = parseInt(str.slice(-1), 10) ? parseInt(str.slice(-1), 10) : 0;
		if (lastNumber >= highestNumber) return lastNumber + 1;
		return highestNumber;
	}

	// Determine the number to append to the model variables
	let number = 0;
	models.forEach((model) => {
		const { states, transitions } = model.model;
		const { rates, initials, parameters, observables } = model.semantics.ode;

		// Use reduce on both of these?

		[...states, ...transitions, ...rates, ...initials, ...parameters, ...observables].forEach(
			(variable) => {
				const { id, name, target } = variable;

				[id, name, target].forEach((str) => {
					if (str) number = updateHighestNumber(str, number);
				});
			}
		);
	});

	const templateWithNumber = cloneDeep(modelTemplate);

	// Append the number to the model variables
	const { states, transitions } = templateWithNumber.model;
	const { rates, initials, parameters, observables } = templateWithNumber.semantics.ode;

	templateWithNumber.header.name += number;
	templateWithNumber.metadata.templateCard.name += number;

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

	initials.forEach((initial) => {
		initial.target += number;
	});

	parameters.forEach((parameter) => {
		parameter.id += number;
	});

	rates.forEach((rate) => {
		rate.target += number;
		// Within the expression attributes update the targets to match the new ones
		rate.expression = rate.expression.replace(/([a-zA-Z])/g, (match) => match + number);
		rate.expression_mathml = rate.expression_mathml.replace(
			/<ci>([a-zA-Z])<\/ci>/g,
			(_, letter: string) => `<ci>${letter + number}</ci>`
		);
	});

	observables.forEach((observable) => {
		observable.id += number;
		observable.name += number;
	});

	return templateWithNumber;
}

export function addCard(modelTemplates: ModelTemplates, modelTemplate: any) {
	modelTemplate.metadata.templateCard.id = uuidv4();
	modelTemplates.models.push(modelTemplate);
}

export function prepareAddingTemplate(modelTemplates: ModelTemplates, modelTemplate: any) {
	const templateType = modelTemplate.header.name;

	let modelTemplateToAdd = modelTemplate; // CLEAN
	const addTemplateArguments: AddTemplateArguments = {};

	// If a decomposed card is added, add it to the kernel
	if (Object.values(DecomposedModelTemplateTypes).includes(templateType)) {
		// Append a number to the model variables to avoid conflicts
		modelTemplateToAdd = appendNumberToModelVariables(modelTemplate, modelTemplates.models);

		if (templateType !== DecomposedModelTemplateTypes.Observable) {
			const uniqueName = modelTemplateToAdd.header.name; // Now save a version of the name with the number appended
			const { transitions } = modelTemplateToAdd.model;
			const { rates, initials, parameters } = cloneDeep(modelTemplateToAdd.semantics.ode); // Clone to avoid mutation on initials when splitting controllers

			// Add parameters to the arguments
			addTemplateArguments.parameter_name = parameters[0].id;
			addTemplateArguments.parameter_value = parameters[0].value;
			addTemplateArguments.parameter_units = parameters[0].units;
			addTemplateArguments.parameter_description = parameters[0].description;
			addTemplateArguments.template_expression = rates[0].expression;
			addTemplateArguments.template_name = uniqueName;

			// Extract controller from initials and add it to the arguments
			if (
				templateType === DecomposedModelTemplateTypes.ControlledConversion ||
				templateType === DecomposedModelTemplateTypes.ControlledDegradation ||
				templateType === DecomposedModelTemplateTypes.ControlledProduction
			) {
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
		} else {
			const { observables } = modelTemplateToAdd.semantics.ode;
			addTemplateArguments.new_id = observables[0].id;
			addTemplateArguments.new_name = modelTemplateToAdd.header.name;
			addTemplateArguments.new_expression = observables[0].expression_mathml;
		}
	}
	return { addTemplateArguments, modelTemplateToAdd };
}

export function updateDecomposedCardName(model: any, name: string) {
	model.header.name = name;
	model.metadata.templateCard.name = name;
	if (model.model.transitions[0] && model.semantics.ode.rates[0]) {
		model.model.transitions[0].id = name;
		model.model.transitions[0].properties.name = name;
		model.semantics.ode.rates[0].target = name;
	}
}

export function removeCard(modelTemplates: ModelTemplates, id: string, index: number) {
	// Remove edges connected to the card
	modelTemplates.junctions.forEach((junction) => {
		junction.edges = junction.edges.filter((edge) => edge.target.cardId !== id);
	});
	junctionCleanUp(modelTemplates);
	// Remove card
	modelTemplates.models.splice(index, 1);
}

export function addEdge2(
	modelTemplates: ModelTemplates,
	target: { cardId: string; portId: string },
	junctionId: string,
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
	}
}

export function addEdge(
	modelTemplates: ModelTemplates,
	kernelManager: KernelSessionManager,
	junctionId: string,
	target: { cardId: string; portId: string },
	outputCode?: Function // This is not used within flattenedToDecomposed
) {
	const templateName = modelTemplates.models.find(
		(model) => model.metadata.templateCard.id === target.cardId
	).metadata.templateCard.name;

	const junctionToDrawFrom = modelTemplates.junctions.find(({ id }) => id === junctionId);

	if (junctionToDrawFrom && junctionToDrawFrom.edges.length >= 1 && outputCode) {
		kernelManager
			.sendMessage('replace_state_name_request', {
				template_name: templateName,
				old_name: target.portId,
				new_name: junctionToDrawFrom.edges[0].target.portId
			})
			.register('replace_state_name_response', (d) => {
				outputCode(d);
			});
	}
}

// TODO: There isn't a way to remove edges in the UI yet
// export function removeEdge(modelTemplates: ModelTemplates) {}

export function flattenedToDecomposed(
	decomposedTemplates: ModelTemplates,
	templatesToAdd: Model[],
	interpolatePointsFn?: Function
) {
	// Insert template card data into decomposed models
	let yPos = 100;
	const allInitals: any[] = [];

	templatesToAdd.forEach((modelTemplate: any) => {
		modelTemplate.metadata.templateCard = {
			id: modelTemplate.header.name,
			name: modelTemplate.header.name,
			x: 100,
			y: yPos
		} as ModelTemplateCard;
		yPos += 200;

		addCard(decomposedTemplates, modelTemplate);
		allInitals.push(...modelTemplate.semantics.ode.initials);
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
			model.semantics.ode.initials.some((initial) => initial.target !== repeatedInitialTargets[i])
		);

		templatesWithRepeatedInitial.forEach((modelTemplate: any) => {
			const templateCard = modelTemplate.metadata.templateCard;
			const junctionId = decomposedTemplates.junctions[decomposedTemplates.junctions.length - 1].id;
			const target = {
				cardId: templateCard.id,
				portId: repeatedInitialTargets[i]
			};

			// Port position is now card position + 168 (width of the card)
			const portPosition = { x: templateCard.x + 168, y: templateCard.y };
			addEdge2(decomposedTemplates, target, junctionId, portPosition, interpolatePointsFn);
		});
	}
}

export function updateFlattenedTemplate(model: Model, flattenedTemplates: ModelTemplates) {
	const flattenedModel: any = cloneDeep(model);
	flattenedModel.metadata.templateCard = {
		id: model.id,
		name: model.header.name,
		x: 100,
		y: 100
	};

	addCard(flattenedTemplates, flattenedModel);
}
