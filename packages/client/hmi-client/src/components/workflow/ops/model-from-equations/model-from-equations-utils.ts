import { AssetBlock } from '@/types/workflow';
import { v4 as uuidv4 } from 'uuid';
import { Model } from '@/types/Types';
import { b64EncodeUnicode } from '@/utils/binary';
import { createModelMap } from '@/model-representation/service';
import _ from 'lodash';
import { formatTitle } from '@/utils/text';
import { EnrichmentBlock, EnrichmentType } from './model-from-equations-operation';

interface ModelEnrichmentResponse {
	modelCard: any;
	modelEnrichment: {
		states: any[];
		parameters: any[];
		observables: any[];
		transitions: any[];
	};
}

export const createEnrichmentCards = (enrichments: ModelEnrichmentResponse) => {
	const cards: AssetBlock<EnrichmentBlock>[] = [];

	function hasNestedObjects(value: any): boolean {
		return !(typeof value !== 'object' || Array.isArray(value) || value === null);
	}

	function processModelCardSection(content: any, parentPath: string[] = []) {
		if (!hasNestedObjects(content)) return;

		Object.entries(content).forEach(([key, value]) => {
			const currentPath = [...parentPath, key];

			// Only create a card if this is a leaf object (no nested objects)
			// or if it's not an object at all
			if (!hasNestedObjects(value)) {
				cards.push({
					id: uuidv4(),
					name: `Description > ${formatTitle(key)}`,
					asset: {
						content: value,
						path: currentPath,
						include: false,
						type: EnrichmentType.DESCRIPTION
					}
				});
			}

			// Continue recursion for objects
			if (hasNestedObjects(value)) {
				processModelCardSection(value, currentPath);
			}
		});
	}

	function processModelEnrichment(enrichment: ModelEnrichmentResponse['modelEnrichment']) {
		// Process states
		enrichment.states?.forEach((state) => {
			cards.push({
				id: uuidv4(),
				name: `State > ${state.id}`,
				asset: {
					content: state,
					path: ['modelEnrichment', 'states', state.id],
					include: false,
					type: EnrichmentType.STATE
				}
			});
		});

		// Process parameters
		enrichment.parameters?.forEach((param) => {
			cards.push({
				id: uuidv4(),
				name: `Parameter > ${param.id}`,
				asset: {
					content: param,
					path: ['modelEnrichment', 'parameters', param.id],
					include: false,
					type: EnrichmentType.PARAMTER
				}
			});
		});

		// Process observables
		enrichment.observables?.forEach((observable) => {
			cards.push({
				id: uuidv4(),
				name: `Observable > ${observable.id}`,
				asset: {
					content: observable,
					path: ['modelEnrichment', 'observables', observable.id],
					include: false,
					type: EnrichmentType.OBSERVABLE
				}
			});
		});

		// Process transitions
		enrichment.transitions?.forEach((transition) => {
			cards.push({
				id: uuidv4(),
				name: `Transition > ${transition.id}`,
				asset: {
					content: transition,
					path: ['modelEnrichment', 'transitions', transition.id],
					include: false,
					type: EnrichmentType.TRANSITION
				}
			});
		});
	}

	// Start processing from modelCard
	processModelCardSection(enrichments.modelCard, ['modelCard']);
	processModelEnrichment(enrichments.modelEnrichment);

	return cards;
};

function buildHTMLDescriptionFromEnrichments(enrichments: AssetBlock<EnrichmentBlock>[]): string {
	let description = '';
	enrichments
		.filter((e) => e.asset.type === EnrichmentType.DESCRIPTION)
		.forEach((enrichment) => {
			description += `<h3>${formatTitle(enrichment.asset.path[enrichment.asset.path.length - 1])}</h3>\n`;
			if (typeof enrichment.asset.content === 'string') {
				description += `<p>${enrichment.asset.content}</p>\n\n`;
			} else if (Array.isArray(enrichment.asset.content)) {
				description += `<ul>\n`;
				description += enrichment.asset.content.map((item) => `<li>${item}</li>`).join('\n');
				description += `</ul>\n\n`;
			}
		});
	return b64EncodeUnicode(description);
}

export function updateModelWithEnrichments(model: Model, enrichments: AssetBlock<EnrichmentBlock>[]) {
	const modelMap = createModelMap(model);
	const includedEnrichments = enrichments.filter((enrichment) => enrichment.asset.include);
	const description = buildHTMLDescriptionFromEnrichments(includedEnrichments);
	if (model.metadata) {
		model.metadata.description = description;
	}

	includedEnrichments.forEach((enrichment) => {
		const semanticId = enrichment.asset.content.id;

		if (enrichment.asset.type === EnrichmentType.STATE) {
			const foundState = modelMap.states.get(semanticId);
			if (foundState) {
				foundState.description = enrichment.asset.content.description;
				foundState.units = enrichment.asset.content.units;
			} else {
				console.error('State not found');
			}
		} else if (enrichment.asset.type === EnrichmentType.TRANSITION) {
			const foundTransition = modelMap.transitions.get(semanticId);
			if (foundTransition) {
				foundTransition.description = enrichment.asset.content.description;
			} else {
				console.error('Transition not found');
			}
		} else if (enrichment.asset.type === EnrichmentType.PARAMTER) {
			const foundParameter = modelMap.parameters.get(semanticId);
			if (foundParameter) {
				foundParameter.description = enrichment.asset.content.description;
				foundParameter.units = enrichment.asset.content.units;
			} else {
				console.error('Parameter not found');
			}
		} else if (enrichment.asset.type === EnrichmentType.OBSERVABLE) {
			const foundObservable = modelMap.observables.get(semanticId);
			if (foundObservable) {
				foundObservable.description = enrichment.asset.content.description;
				foundObservable.units = enrichment.asset.content.units;
			} else {
				console.error('Observable not found');
			}
		}
	});
}
