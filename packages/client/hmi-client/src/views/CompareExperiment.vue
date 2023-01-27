<template>
	<section>
		<div id="experiment" style="width: 800px; height: 500px"></div>
	</section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import * as d3 from 'd3';
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge, pathFn } from '@/services/graph';
import * as _ from 'lodash';

interface NodeData {
	name: string;
	models: string[];
}

interface EdgeData {
	val: number;
}

let g: IGraph<NodeData, EdgeData> = {
	width: 800,
	height: 400,
	nodes: [],
	edges: []
};
console.log(g);

// const dataStr =
// 	'{"template_models": {"0": {"templates": [{"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}], "parameters": {}, "initials": {}}, "1": {"templates": [{"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}, {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}], "parameters": {}, "initials": {}}}, "nodes": {"0": {"0": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, "1": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "2": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "3": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, "4": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "5": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "6": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}, "7": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "8": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {}}, "provenance": []}}, "1": {"0": {"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "provenance": []}, "1": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "2": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "vaccinated"}}, "3": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "4": {"rate_law": null, "type": "ControlledConversion", "controller": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "provenance": []}, "5": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "6": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"vaccination_status": "unvaccinated"}}, "7": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "8": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "vaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, "9": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "susceptible_population", "identifiers": {"ido": "0000514"}, "context": {"property": "ido:0000597", "vaccination_status": "unvaccinated"}}, "outcome": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, "10": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "vaccinated"}}, "provenance": []}, "11": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "vaccinated"}}, "12": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "unvaccinated"}}, "provenance": []}, "13": {"name": "recovered", "identifiers": {"ido": "0000592"}, "context": {"vaccination_status": "unvaccinated"}}, "14": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "vaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}, "15": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "16": {"rate_law": null, "type": "NaturalConversion", "subject": {"name": "infected_population", "identifiers": {"ido": "0000511"}, "context": {"vaccination_status": "unvaccinated"}}, "outcome": {"name": "dead", "identifiers": {"ncit": "C28554"}, "context": {}}, "provenance": []}}}, "inter_model_edges": [[[1, 8], [0, 0], "refinement_of"], [[0, 1], [1, 2], "is_equal"], [[1, 3], [0, 1], "refinement_of"], [[0, 2], [1, 1], "is_equal"], [[1, 9], [0, 3], "refinement_of"], [[0, 4], [1, 6], "is_equal"], [[1, 7], [0, 4], "refinement_of"], [[0, 5], [1, 5], "is_equal"], [[1, 10], [0, 6], "refinement_of"], [[1, 11], [0, 7], "refinement_of"], [[1, 13], [0, 7], "refinement_of"], [[1, 12], [0, 8], "refinement_of"]], "intra_model_edges": [[[0, 1], [0, 0], "subject"], [[0, 0], [0, 2], "outcome"], [[0, 4], [0, 3], "subject"], [[0, 3], [0, 5], "outcome"], [[0, 2], [0, 6], "subject"], [[0, 6], [0, 7], "outcome"], [[0, 5], [0, 8], "subject"], [[0, 8], [0, 7], "outcome"], [[1, 1], [1, 0], "controller"], [[1, 2], [1, 0], "subject"], [[1, 0], [1, 3], "outcome"], [[1, 5], [1, 4], "controller"], [[1, 6], [1, 4], "subject"], [[1, 4], [1, 7], "outcome"], [[1, 3], [1, 8], "subject"], [[1, 8], [1, 1], "outcome"], [[1, 7], [1, 9], "subject"], [[1, 9], [1, 5], "outcome"], [[1, 1], [1, 10], "subject"], [[1, 10], [1, 11], "outcome"], [[1, 5], [1, 12], "subject"], [[1, 12], [1, 13], "outcome"], [[1, 1], [1, 14], "subject"], [[1, 14], [1, 15], "outcome"], [[1, 5], [1, 16], "subject"], [[1, 16], [1, 15], "outcome"]]}';

const dataStr =
	'{ "template_models": { "0": { "templates": [ { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } } ], "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } } ], "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] } ], "parameters": {}, "initials": {} }, "1": { "templates": [ { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] } ], "parameters": {}, "initials": {} }, "2": { "templates": [ { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "dead", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] }, { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "dead", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] } ], "parameters": {}, "initials": {} } }, "nodes": { "0": { "0": { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } } ], "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "provenance": [] }, "1": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "2": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "3": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "4": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "5": { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } } ], "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "provenance": [] }, "6": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "7": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "8": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "9": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, "10": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "11": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "12": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, "13": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "14": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "15": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "vaccinated" } }, "16": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, "17": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "unvaccinated" } }, "18": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "19": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "provenance": [] }, "20": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511", "ncit": "C171133" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "21": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Recovered", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "22": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C28554" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] } }, "1": { "0": { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "1": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "2": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "3": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "4": { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, "5": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "6": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "7": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": {} }, "8": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] } }, "2": { "0": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "1": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "2": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "3": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "outcome": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, "4": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "5": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "6": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "provenance": [] }, "7": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "vaccinated" } }, "8": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "provenance": [] }, "9": { "name": "immune_population", "identifiers": { "ido": "0000592" }, "context": { "vaccination_status": "unvaccinated" } }, "10": { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "vaccinated" } }, "provenance": [] }, "11": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "vaccinated" } }, "12": { "rate_law": null, "type": "GroupedControlledConversion", "controllers": [ { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } } ], "subject": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "property": "ido:0000597", "vaccination_status": "unvaccinated" } }, "provenance": [] }, "13": { "name": "susceptible_population", "identifiers": { "ido": "0000514" }, "context": { "vaccination_status": "unvaccinated" } }, "14": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "vaccinated" } }, "outcome": { "name": "dead", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] }, "15": { "name": "dead", "identifiers": { "ncit": "C28554" }, "context": {} }, "16": { "rate_law": null, "type": "NaturalConversion", "subject": { "name": "infected_population", "identifiers": { "ido": "0000511" }, "context": { "vaccination_status": "unvaccinated" } }, "outcome": { "name": "dead", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] } } }, "inter_model_edges": [ [ [ 0, 0 ], [ 2, 10 ], "is_equal" ], [ [ 0, 1 ], [ 1, 1 ], "is_equal" ], [ [ 0, 1 ], [ 2, 2 ], "is_equal" ], [ [ 0, 2 ], [ 1, 2 ], "is_equal" ], [ [ 0, 2 ], [ 2, 5 ], "is_equal" ], [ [ 0, 4 ], [ 0, 3 ], "refinement_of" ], [ [ 0, 3 ], [ 1, 3 ], "is_equal" ], [ [ 2, 1 ], [ 0, 3 ], "refinement_of" ], [ [ 0, 3 ], [ 2, 11 ], "is_equal" ], [ [ 0, 4 ], [ 1, 3 ], "refinement_of" ], [ [ 0, 4 ], [ 2, 1 ], "is_equal" ], [ [ 0, 4 ], [ 2, 11 ], "refinement_of" ], [ [ 0, 5 ], [ 2, 12 ], "is_equal" ], [ [ 0, 7 ], [ 0, 6 ], "refinement_of" ], [ [ 0, 6 ], [ 1, 5 ], "is_equal" ], [ [ 2, 4 ], [ 0, 6 ], "refinement_of" ], [ [ 0, 6 ], [ 2, 13 ], "is_equal" ], [ [ 0, 7 ], [ 1, 5 ], "refinement_of" ], [ [ 0, 7 ], [ 2, 4 ], "is_equal" ], [ [ 0, 7 ], [ 2, 13 ], "refinement_of" ], [ [ 0, 8 ], [ 2, 0 ], "is_equal" ], [ [ 0, 9 ], [ 2, 3 ], "is_equal" ], [ [ 0, 10 ], [ 1, 6 ], "refinement_of" ], [ [ 0, 10 ], [ 2, 6 ], "is_equal" ], [ [ 0, 11 ], [ 1, 7 ], "refinement_of" ], [ [ 0, 11 ], [ 2, 7 ], "is_equal" ], [ [ 0, 12 ], [ 1, 8 ], "refinement_of" ], [ [ 0, 12 ], [ 2, 8 ], "is_equal" ], [ [ 0, 13 ], [ 1, 7 ], "refinement_of" ], [ [ 0, 13 ], [ 2, 9 ], "is_equal" ], [ [ 0, 14 ], [ 2, 14 ], "refinement_of" ], [ [ 0, 15 ], [ 2, 15 ], "refinement_of" ], [ [ 0, 16 ], [ 2, 16 ], "refinement_of" ], [ [ 0, 17 ], [ 2, 15 ], "refinement_of" ], [ [ 1, 1 ], [ 2, 2 ], "is_equal" ], [ [ 1, 2 ], [ 2, 5 ], "is_equal" ], [ [ 2, 1 ], [ 1, 3 ], "refinement_of" ], [ [ 1, 3 ], [ 2, 11 ], "is_equal" ], [ [ 2, 4 ], [ 1, 5 ], "refinement_of" ], [ [ 1, 5 ], [ 2, 13 ], "is_equal" ], [ [ 2, 6 ], [ 1, 6 ], "refinement_of" ], [ [ 2, 7 ], [ 1, 7 ], "refinement_of" ], [ [ 2, 9 ], [ 1, 7 ], "refinement_of" ], [ [ 2, 8 ], [ 1, 8 ], "refinement_of" ] ], "intra_model_edges": [ [ [ 0, 1 ], [ 0, 0 ], "controllers" ], [ [ 0, 2 ], [ 0, 0 ], "controllers" ], [ [ 0, 3 ], [ 0, 0 ], "subject" ], [ [ 0, 0 ], [ 0, 4 ], "outcome" ], [ [ 0, 2 ], [ 0, 5 ], "controllers" ], [ [ 0, 1 ], [ 0, 5 ], "controllers" ], [ [ 0, 6 ], [ 0, 5 ], "subject" ], [ [ 0, 5 ], [ 0, 7 ], "outcome" ], [ [ 0, 4 ], [ 0, 8 ], "subject" ], [ [ 0, 8 ], [ 0, 1 ], "outcome" ], [ [ 0, 7 ], [ 0, 9 ], "subject" ], [ [ 0, 9 ], [ 0, 2 ], "outcome" ], [ [ 0, 1 ], [ 0, 10 ], "subject" ], [ [ 0, 10 ], [ 0, 11 ], "outcome" ], [ [ 0, 2 ], [ 0, 12 ], "subject" ], [ [ 0, 12 ], [ 0, 13 ], "outcome" ], [ [ 0, 1 ], [ 0, 14 ], "subject" ], [ [ 0, 14 ], [ 0, 15 ], "outcome" ], [ [ 0, 2 ], [ 0, 16 ], "subject" ], [ [ 0, 16 ], [ 0, 17 ], "outcome" ], [ [ 0, 6 ], [ 0, 18 ], "subject" ], [ [ 0, 18 ], [ 0, 3 ], "outcome" ], [ [ 0, 7 ], [ 0, 19 ], "subject" ], [ [ 0, 19 ], [ 0, 4 ], "outcome" ], [ [ 0, 2 ], [ 0, 20 ], "subject" ], [ [ 0, 20 ], [ 0, 1 ], "outcome" ], [ [ 0, 13 ], [ 0, 21 ], "subject" ], [ [ 0, 21 ], [ 0, 11 ], "outcome" ], [ [ 0, 17 ], [ 0, 22 ], "subject" ], [ [ 0, 22 ], [ 0, 15 ], "outcome" ], [ [ 1, 1 ], [ 1, 0 ], "controllers" ], [ [ 1, 2 ], [ 1, 0 ], "controllers" ], [ [ 1, 3 ], [ 1, 0 ], "subject" ], [ [ 1, 0 ], [ 1, 1 ], "outcome" ], [ [ 1, 2 ], [ 1, 4 ], "controllers" ], [ [ 1, 1 ], [ 1, 4 ], "controllers" ], [ [ 1, 5 ], [ 1, 4 ], "subject" ], [ [ 1, 4 ], [ 1, 2 ], "outcome" ], [ [ 1, 1 ], [ 1, 6 ], "subject" ], [ [ 1, 6 ], [ 1, 7 ], "outcome" ], [ [ 1, 2 ], [ 1, 8 ], "subject" ], [ [ 1, 8 ], [ 1, 7 ], "outcome" ], [ [ 2, 1 ], [ 2, 0 ], "subject" ], [ [ 2, 0 ], [ 2, 2 ], "outcome" ], [ [ 2, 4 ], [ 2, 3 ], "subject" ], [ [ 2, 3 ], [ 2, 5 ], "outcome" ], [ [ 2, 2 ], [ 2, 6 ], "subject" ], [ [ 2, 6 ], [ 2, 7 ], "outcome" ], [ [ 2, 5 ], [ 2, 8 ], "subject" ], [ [ 2, 8 ], [ 2, 9 ], "outcome" ], [ [ 2, 2 ], [ 2, 10 ], "controllers" ], [ [ 2, 5 ], [ 2, 10 ], "controllers" ], [ [ 2, 11 ], [ 2, 10 ], "subject" ], [ [ 2, 10 ], [ 2, 1 ], "outcome" ], [ [ 2, 5 ], [ 2, 12 ], "controllers" ], [ [ 2, 2 ], [ 2, 12 ], "controllers" ], [ [ 2, 13 ], [ 2, 12 ], "subject" ], [ [ 2, 12 ], [ 2, 4 ], "outcome" ], [ [ 2, 2 ], [ 2, 14 ], "subject" ], [ [ 2, 14 ], [ 2, 15 ], "outcome" ], [ [ 2, 5 ], [ 2, 16 ], "subject" ], [ [ 2, 16 ], [ 2, 15 ], "outcome" ] ] }';
const data = JSON.parse(dataStr);

// Create modelMap
const modelsMap = data.nodes;

const interModelEdges = data.inter_model_edges;
const intraModelEdges = data.intra_model_edges;

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
			// const node = modelsMap[modelKey][nodeKey];
			g.nodes.push({
				id: hash,
				label: hash,
				x: 0,
				y: 0,
				height: 80,
				width: 80,
				data: { name: hash, models: [modelKey] },
				nodes: []
			});
		} else {
			const surrogateId = pool[0];
			if (!g.nodes.find((d) => d.id === surrogateId)) {
				g.nodes.push({
					id: surrogateId,
					label: surrogateId,
					x: 0,
					y: 0,
					height: 80,
					width: 80,
					data: { name: surrogateId, models: [] },
					nodes: []
				});
			}
			g.nodes.find((d) => d.id === surrogateId)?.data.models.push(modelKey);
		}
	});
});

let eCounter = 0;
intraModelEdges.forEach((edge) => {
	const [[sourceModelId, sourceNodeId], [targetModelId, targetNodeId]] = edge;
	let sourceHash = `${sourceModelId}:${sourceNodeId}`;
	let targetHash = `${targetModelId}:${targetNodeId}`;

	const sourcePool = equalPools.find((p) => p.includes(sourceHash));
	const targetPool = equalPools.find((p) => p.includes(targetHash));

	if (sourcePool) sourceHash = sourcePool[0];
	if (targetPool) targetHash = targetPool[0];

	g.edges.push({
		id: `${eCounter}`,
		source: sourceHash,
		target: targetHash,
		points: [],
		data: { val: 1 }
	});
	eCounter++;
});

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW_PATH = 'M 0,-3.25 L 5 ,0 L 0,3.25';

class ComparisonRenderer<V, E> extends graphScaffolder.BasicRenderer<V, E> {
	readonly EDGE_ARROW_ID = 'edge-arrowhead';

	setupDefs() {
		const svg = d3.select(this.svgEl);

		// Clean up
		svg.select('defs').selectAll('.edge-marker-end').remove();

		// Arrow defs
		svg
			.select('defs')
			.append('marker')
			.classed('edge-marker-end', true)
			.attr('id', this.EDGE_ARROW_ID)
			.attr('viewBox', MARKER_VIEWBOX)
			.attr('refX', 2)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 15)
			.attr('markerHeight', 15)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW_PATH)
			.style('fill', '#000')
			.style('stroke', 'none');
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		selection
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', '#CCC');

		selection
			.append('text')
			.attr('y', 30)
			.attr('x', 60)
			.text((d) => `${d.id}`);

		const cat10 = d3.schemeCategory10;
		selection.each((d, i, g2) => {
			if (d.data.models.length > 1) {
				for (let idx = 0; idx < 2; idx++) {
					const arcExample = d3
						.arc()
						.innerRadius(d.width * 0.5 + 5)
						.outerRadius(d.width * 0.5 + 15)
						.startAngle((idx * 2 * Math.PI) / 2)
						.endAngle(((idx + 1) * 2 * Math.PI) / 2)();

					d3.select(g2[i])
						.append('path')
						.attr('transform', `translate(${d.width * 0.5}, ${d.height * 0.5})`)
						.attr('fill', cat10[idx])
						.attr('stroke', '#888')
						.attr('d', arcExample);
				}
			} else if (d.data.models.length > 2) {
				for (let idx = 0; idx < 3; idx++) {
					const arcExample = d3
						.arc()
						.innerRadius(d.width * 0.5 + 5)
						.outerRadius(d.width * 0.5 + 30)
						.startAngle((idx * 2 * Math.PI) / 3)
						.endAngle(((idx + 1) * 2 * Math.PI) / 3)();

					d3.select(g2[i])
						.append('path')
						.attr('transform', `translate(${d.width * 0.5}, ${d.height * 0.5})`)
						.attr('fill', cat10[idx])
						.attr('stroke', '#888')
						.attr('d', arcExample);
				}
			}
		});
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000')
			.style('stroke-width', 5)
			.attr('marker-end', `url(#${this.EDGE_ARROW_ID})`);
	}
}

onMounted(async () => {
	const experimentDiv = document.getElementById('experiment');
	const renderer = new ComparisonRenderer({
		el: experimentDiv as any,
		useAStarRouting: true,
		runLayout: runDagreLayout
	});

	g = runDagreLayout(_.cloneDeep(g));
	await renderer.setData(g);
	await renderer.render();
});

// const modelKeys = Object.keys(models);
// modelKeys.forEach(key => {
// 	const model = models[key];
// 	const nodes =
// });
</script>
