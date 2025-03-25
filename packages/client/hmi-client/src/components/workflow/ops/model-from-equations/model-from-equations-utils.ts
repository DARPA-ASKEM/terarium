import { Enrichment, EnrichmentTarget, Model } from '@/types/Types';
import { b64EncodeUnicode } from '@/utils/binary';
import { createModelMap } from '@/model-representation/service';
import { formatTitle } from '@/utils/text';

function buildHTMLDescriptionFromEnrichments(enrichments: Enrichment[]): string {
	let description = '';
	enrichments
		.filter((e) => e.target === EnrichmentTarget.ModelCard)
		.forEach((enrichment) => {
			description += `<h3>${formatTitle(enrichment.label)}</h3>\n`;
			if (typeof enrichment.content === 'string') {
				description += `<p>${enrichment.content}</p>\n\n`;
			} else if (Array.isArray(enrichment.content)) {
				description += `<ul>\n`;
				description += enrichment.content.map((item) => `<li>${item}</li>`).join('\n');
				description += `</ul>\n\n`;
			} else if (typeof enrichment.content === 'object') {
				// loop through keys, header of key with h4, then list or string of values
				Object.entries(enrichment.content).forEach(([key, value]) => {
					if (Object.prototype.hasOwnProperty.call(enrichment.content, key)) {
						description += `<h4>${formatTitle(key)}</h4>\n`;
						if (Array.isArray(value)) {
							description += `<ul>\n`;
							description += value.map((item) => `<li>${item}</li>`).join('\n');
							description += `</ul>\n\n`;
						} else {
							description += `<p>${value}</p>\n\n`;
						}
					}
				});
			}
		});
	return b64EncodeUnicode(description);
}

export function updateModelWithEnrichments(model: Model, enrichments: Enrichment[]) {
	const modelMap = createModelMap(model);
	const includedEnrichments = enrichments.filter((enrichment) => enrichment.included);
	// update enrichments in model enrichments map
	if (model.metadata && model.metadata.enrichments) {
		model.metadata.enrichments = enrichments;
	}
	const description = buildHTMLDescriptionFromEnrichments(includedEnrichments);
	if (model.metadata) {
		model.metadata.description = description;
	}

	includedEnrichments.forEach((enrichment) => {
		const semanticId = enrichment.content.id;

		if (enrichment.target === EnrichmentTarget.State) {
			const foundState = modelMap.states.get(semanticId);
			if (foundState) {
				foundState.description = enrichment.content.description;
				foundState.units = enrichment.content.units;
			} else {
				console.error('State not found');
			}
		} else if (enrichment.target === EnrichmentTarget.Transition) {
			const foundTransition = modelMap.transitions.get(semanticId);
			if (foundTransition) {
				foundTransition.description = enrichment.content.description;
			} else {
				console.error('Transition not found');
			}
		} else if (enrichment.target === EnrichmentTarget.Parameter) {
			const foundParameter = modelMap.parameters.get(semanticId);
			if (foundParameter) {
				foundParameter.description = enrichment.content.description;
				foundParameter.units = enrichment.content.units;
			} else {
				console.error('Parameter not found');
			}
		} else if (enrichment.target === EnrichmentTarget.Observable) {
			const foundObservable = modelMap.observables.get(semanticId);
			if (foundObservable) {
				foundObservable.description = enrichment.content.description;
				foundObservable.units = enrichment.content.units;
			} else {
				console.error('Observable not found');
			}
		}
	});
}
