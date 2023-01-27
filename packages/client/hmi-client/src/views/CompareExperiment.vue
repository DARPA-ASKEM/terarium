<template>
	<div>hi</div>
</template>

<script setup lang="ts">
import { IGraph } from '@graph-scaffolder/index';

interface NodeData {
	name: string;
	models: string[];
}

interface EdgeData {
	val: number;
}

const g: IGraph<NodeData, EdgeData> = {
	width: 800,
	height: 400,
	nodes: [],
	edges: []
};
console.log(g);

const dataStr =
	'{"template_models": {"0": {"templates": [{"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}], "parameters": {}, "initials": {}}, "1": {"templates": [{"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}], "parameters": {}, "initials": {}}}, "nodes": {"0": {"0": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, "1": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "2": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "3": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, "4": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "5": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "6": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}, "7": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "8": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}}, "1": {"0": {"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "provenance": []}, "1": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "2": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "3": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "4": {"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "provenance": []}, "5": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "6": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "7": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "8": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, "9": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, "10": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, "11": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "vaccinated"}}, "12": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, "13": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "unvaccinated"}}, "14": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}, "15": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "16": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}}}, "inter_model_edges": [[[1, 8], [0, 0], "refinement_of"], [[0, 1], [1, 2], "is_equal"], [[1, 3], [0, 1], "refinement_of"], [[0, 2], [1, 1], "is_equal"], [[1, 9], [0, 3], "refinement_of"], [[0, 4], [1, 6], "is_equal"], [[1, 7], [0, 4], "refinement_of"], [[0, 5], [1, 5], "is_equal"], [[1, 10], [0, 6], "refinement_of"], [[1, 11], [0, 7], "refinement_of"], [[1, 13], [0, 7], "refinement_of"], [[1, 12], [0, 8], "refinement_of"]], "intra_model_edges": [[[0, 1], [0, 0], "subject"], [[0, 0], [0, 2], "outcome"], [[0, 4], [0, 3], "subject"], [[0, 3], [0, 5], "outcome"], [[0, 2], [0, 6], "subject"], [[0, 6], [0, 7], "outcome"], [[0, 5], [0, 8], "subject"], [[0, 8], [0, 7], "outcome"], [[1, 1], [1, 0], "controller"], [[1, 2], [1, 0], "subject"], [[1, 0], [1, 3], "outcome"], [[1, 5], [1, 4], "controller"], [[1, 6], [1, 4], "subject"], [[1, 4], [1, 7], "outcome"], [[1, 3], [1, 8], "subject"], [[1, 8], [1, 1], "outcome"], [[1, 7], [1, 9], "subject"], [[1, 9], [1, 5], "outcome"], [[1, 1], [1, 10], "subject"], [[1, 10], [1, 11], "outcome"], [[1, 5], [1, 12], "subject"], [[1, 12], [1, 13], "outcome"], [[1, 1], [1, 14], "subject"], [[1, 14], [1, 15], "outcome"], [[1, 5], [1, 16], "subject"], [[1, 16], [1, 15], "outcome"]]}';
const data = JSON.parse(dataStr);

// Create modelMap
const modelsMap = data.nodes;

const interModelEdges = data.inter_model_edges;

const equalPools: string[][] = [];

// Find equality nodes
interModelEdges.forEach((edge) => {
	const [[sourceModelId, sourceNodeId], [targetModelId, targetNodeId], relation] = edge;
	const sourceHash = `${sourceModelId}:${sourceNodeId}`;
	const targetHash = `${targetModelId}:${targetNodeId}`;

	if (relation === 'is_equal') {
		const pool = equalPools.find((p) => p.includes(sourceHash) || p.includes(targetHash));
		if (!pool) {
			equalPools.push([sourceHash, targetHash]);
		} else {
			pool.push(sourceHash);
			pool.push(targetHash);
		}
	}
});

console.log(equalPools);

// Create nodes
const modelKeys = Object.keys(modelsMap);

modelKeys.forEach((modelKey) => {
	const nodeKeys = Object.keys(modelsMap[modelKey]);
	nodeKeys.forEach((nodeKey) => {
		const hash = `${modelKey}:${nodeKey}`;
		console.log(hash);
		const pool = equalPools.find((p) => p.includes(hash));

		if (!pool) {
			const node = modelsMap[modelKey][nodeKey];
			console.log(node);
		}
	});
});

// const modelKeys = Object.keys(models);
// modelKeys.forEach(key => {
// 	const model = models[key];
// 	const nodes =
// });
</script>
