<template>
	<section>
		<div id="experiment" style="width: 1200px; height: 800px"></div>
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

const palette = [
	'#89BEFF',
	'#B0E6D9',
	'#FDE267',
	'#FF90A9',
	'#B49CFD',
	'#FEAB79',
	'#78DDED',
	'#78E3AF'
];

let g: IGraph<NodeData, EdgeData> = {
	width: 800,
	height: 400,
	nodes: [],
	edges: []
};

const dataStr =
	'{ "template_models": { "0": { "templates": [ { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] } ], "parameters": { "alpha": { "name": "alpha", "identifiers": {}, "context": {}, "value": 0.57 }, "beta": { "name": "beta", "identifiers": {}, "context": {}, "value": 0.011 }, "gamma": { "name": "gamma", "identifiers": {}, "context": {}, "value": 0.456 }, "delta": { "name": "delta", "identifiers": {}, "context": {}, "value": 0.011 }, "epsilon": { "name": "epsilon", "identifiers": {}, "context": {}, "value": 0.171 }, "theta": { "name": "theta", "identifiers": {}, "context": {}, "value": 0.371 }, "zeta": { "name": "zeta", "identifiers": {}, "context": {}, "value": 0.125 }, "eta": { "name": "eta", "identifiers": {}, "context": {}, "value": 0.125 }, "mu": { "name": "mu", "identifiers": {}, "context": {}, "value": 0.017 }, "nu": { "name": "nu", "identifiers": {}, "context": {}, "value": 0.027 }, "tau": { "name": "tau", "identifiers": {}, "context": {}, "value": 0.01 }, "kappa": { "name": "kappa", "identifiers": {}, "context": {}, "value": 0.017 }, "rho": { "name": "rho", "identifiers": {}, "context": {}, "value": 0.034 }, "sigma": { "name": "sigma", "identifiers": {}, "context": {}, "value": 0.017 }, "xi": { "name": "xi", "identifiers": {}, "context": {}, "value": 0.017 }, "Event_trigger_Fig3b": { "name": "Event_trigger_Fig3b", "identifiers": {}, "context": {}, "value": 0.0 }, "Event_trigger_Fig3d": { "name": "Event_trigger_Fig3d", "identifiers": {}, "context": {}, "value": 0.0 }, "Event_trigger_Fig4b": { "name": "Event_trigger_Fig4b", "identifiers": {}, "context": {}, "value": 0.0 }, "Event_trigger_Fig4d": { "name": "Event_trigger_Fig4d", "identifiers": {}, "context": {}, "value": 0.0 }, "epsilon_modifier": { "name": "epsilon_modifier", "identifiers": {}, "context": {}, "value": 1.0 }, "alpha_modifier": { "name": "alpha_modifier", "identifiers": {}, "context": {}, "value": 1.0 }, "ModelValue_16": { "name": "ModelValue_16", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_17": { "name": "ModelValue_17", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_18": { "name": "ModelValue_18", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_19": { "name": "ModelValue_19", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_21": { "name": "ModelValue_21", "identifiers": {}, "context": {}, "value": 1.0 }, "ModelValue_20": { "name": "ModelValue_20", "identifiers": {}, "context": {}, "value": 1.0 }, "Italy": { "name": "Italy", "identifiers": {}, "context": {}, "value": 1.0 }, "XXlambdaXX": { "name": "XXlambdaXX", "identifiers": {}, "context": {}, "value": 0.034 } }, "initials": { "Susceptible": { "concept": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "value": 0.9999963 }, "Infected": { "concept": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "value": 3.33333333e-06 }, "Diagnosed": { "concept": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "value": 3.33333333e-07 }, "Ailing": { "concept": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "value": 1.66666666e-08 }, "Recognized": { "concept": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "value": 3.33333333e-08 }, "Threatened": { "concept": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "value": 0.0 }, "Healed": { "concept": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "value": 0.0 }, "Extinct": { "concept": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} }, "value": 0.0 } } }, "1": { "templates": [ { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "vaccinated", "identifiers": { "vo": "0001376" }, "context": {} }, "provenance": [] } ], "parameters": { "alpha": { "name": "alpha", "identifiers": {}, "context": {}, "value": 0.57 }, "beta": { "name": "beta", "identifiers": {}, "context": {}, "value": 0.011 }, "gamma": { "name": "gamma", "identifiers": {}, "context": {}, "value": 0.456 }, "delta": { "name": "delta", "identifiers": {}, "context": {}, "value": 0.011 }, "epsilon": { "name": "epsilon", "identifiers": {}, "context": {}, "value": 0.171 }, "theta": { "name": "theta", "identifiers": {}, "context": {}, "value": 0.371 }, "zeta": { "name": "zeta", "identifiers": {}, "context": {}, "value": 0.125 }, "eta": { "name": "eta", "identifiers": {}, "context": {}, "value": 0.125 }, "mu": { "name": "mu", "identifiers": {}, "context": {}, "value": 0.017 }, "nu": { "name": "nu", "identifiers": {}, "context": {}, "value": 0.027 }, "tau": { "name": "tau", "identifiers": {}, "context": {}, "value": 0.01 }, "kappa": { "name": "kappa", "identifiers": {}, "context": {}, "value": 0.017 }, "rho": { "name": "rho", "identifiers": {}, "context": {}, "value": 0.034 }, "sigma": { "name": "sigma", "identifiers": {}, "context": {}, "value": 0.017 }, "xi": { "name": "xi", "identifiers": {}, "context": {}, "value": 0.017 }, "Event_trigger_Fig3b": { "name": "Event_trigger_Fig3b", "identifiers": {}, "context": {}, "value": 0.0 }, "Event_trigger_Fig3d": { "name": "Event_trigger_Fig3d", "identifiers": {}, "context": {}, "value": 0.0 }, "Event_trigger_Fig4b": { "name": "Event_trigger_Fig4b", "identifiers": {}, "context": {}, "value": 0.0 }, "Event_trigger_Fig4d": { "name": "Event_trigger_Fig4d", "identifiers": {}, "context": {}, "value": 0.0 }, "epsilon_modifier": { "name": "epsilon_modifier", "identifiers": {}, "context": {}, "value": 1.0 }, "alpha_modifier": { "name": "alpha_modifier", "identifiers": {}, "context": {}, "value": 1.0 }, "ModelValue_16": { "name": "ModelValue_16", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_17": { "name": "ModelValue_17", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_18": { "name": "ModelValue_18", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_19": { "name": "ModelValue_19", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_21": { "name": "ModelValue_21", "identifiers": {}, "context": {}, "value": 1.0 }, "ModelValue_20": { "name": "ModelValue_20", "identifiers": {}, "context": {}, "value": 1.0 }, "Italy": { "name": "Italy", "identifiers": {}, "context": {}, "value": 1.0 }, "XXlambdaXX": { "name": "XXlambdaXX", "identifiers": {}, "context": {}, "value": 0.034 }, "phi": { "name": "phi", "identifiers": {}, "context": {}, "value": 1.0 } }, "initials": { "Susceptible": { "concept": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "value": 0.9999963 }, "Infected": { "concept": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "value": 3.33333333e-06 }, "Diagnosed": { "concept": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "value": 3.33333333e-07 }, "Ailing": { "concept": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "value": 1.66666666e-08 }, "Recognized": { "concept": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "value": 3.33333333e-08 }, "Threatened": { "concept": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "value": 0.0 }, "Healed": { "concept": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "value": 0.0 }, "Extinct": { "concept": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} }, "value": 0.0 } } } }, "concept_nodes": { "0": { "16": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "17": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "18": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "19": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "20": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "21": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "22": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "23": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} } }, "1": { "17": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "18": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "19": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "20": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "21": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "22": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "23": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "24": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} }, "25": { "name": "vaccinated", "identifiers": { "vo": "0001376" }, "context": {} } } }, "template_nodes": { "0": { "0": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "1": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "2": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "3": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "4": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "provenance": [] }, "5": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "provenance": [] }, "6": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "7": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, "8": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "9": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, "10": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "11": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, "12": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, "13": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "14": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] }, "15": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] } }, "1": { "0": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "1": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "2": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "3": { "rate_law": {}, "type": "ControlledConversion", "controller": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "provenance": [] }, "4": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "provenance": [] }, "5": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "provenance": [] }, "6": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infected", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C3833" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "7": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, "8": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Diagnosed", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "9": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "provenance": [] }, "10": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "11": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Ailing", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C113725", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, "12": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "provenance": [] }, "13": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Recognized", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C25587", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "14": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Extinct", "identifiers": { "ncit": "C28554" }, "context": {} }, "provenance": [] }, "15": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Threatened", "identifiers": { "ido": "0000511" }, "context": { "property": "ncit:C15220", "disease_status": "ncit:C25269" } }, "outcome": { "name": "Healed", "identifiers": { "ido": "0000592" }, "context": {} }, "provenance": [] }, "16": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Susceptible", "identifiers": { "ido": "0000514" }, "context": {} }, "outcome": { "name": "vaccinated", "identifiers": { "vo": "0001376" }, "context": {} }, "provenance": [] } } }, "inter_model_edges": [ [ [ 0, 0 ], [ 1, 0 ], "is_equal" ], [ [ 0, 1 ], [ 1, 1 ], "is_equal" ], [ [ 0, 2 ], [ 1, 2 ], "is_equal" ], [ [ 0, 3 ], [ 1, 3 ], "is_equal" ], [ [ 0, 4 ], [ 1, 4 ], "is_equal" ], [ [ 0, 5 ], [ 1, 5 ], "is_equal" ], [ [ 0, 6 ], [ 1, 6 ], "is_equal" ], [ [ 0, 7 ], [ 1, 7 ], "is_equal" ], [ [ 0, 8 ], [ 1, 8 ], "is_equal" ], [ [ 1, 15 ], [ 0, 8 ], "refinement_of" ], [ [ 0, 9 ], [ 1, 9 ], "is_equal" ], [ [ 0, 10 ], [ 1, 10 ], "is_equal" ], [ [ 0, 11 ], [ 1, 11 ], "is_equal" ], [ [ 0, 12 ], [ 1, 12 ], "is_equal" ], [ [ 0, 13 ], [ 1, 13 ], "is_equal" ], [ [ 0, 14 ], [ 1, 14 ], "is_equal" ], [ [ 0, 15 ], [ 1, 8 ], "refinement_of" ], [ [ 0, 15 ], [ 1, 15 ], "is_equal" ], [ [ 0, 22 ], [ 0, 16 ], "refinement_of" ], [ [ 0, 16 ], [ 1, 17 ], "is_equal" ], [ [ 1, 23 ], [ 0, 16 ], "refinement_of" ], [ [ 0, 17 ], [ 1, 18 ], "is_equal" ], [ [ 0, 18 ], [ 1, 19 ], "is_equal" ], [ [ 0, 19 ], [ 1, 20 ], "is_equal" ], [ [ 0, 20 ], [ 1, 21 ], "is_equal" ], [ [ 0, 21 ], [ 1, 22 ], "is_equal" ], [ [ 0, 22 ], [ 1, 17 ], "refinement_of" ], [ [ 0, 22 ], [ 1, 23 ], "is_equal" ], [ [ 0, 23 ], [ 1, 24 ], "is_equal" ], [ [ 1, 23 ], [ 1, 17 ], "refinement_of" ] ], "intra_model_edges": [ [ [ 0, 16 ], [ 0, 0 ], "controller" ], [ [ 0, 17 ], [ 0, 0 ], "subject" ], [ [ 0, 0 ], [ 0, 18 ], "outcome" ], [ [ 0, 19 ], [ 0, 1 ], "controller" ], [ [ 0, 17 ], [ 0, 1 ], "subject" ], [ [ 0, 1 ], [ 0, 18 ], "outcome" ], [ [ 0, 20 ], [ 0, 2 ], "controller" ], [ [ 0, 17 ], [ 0, 2 ], "subject" ], [ [ 0, 2 ], [ 0, 18 ], "outcome" ], [ [ 0, 18 ], [ 0, 3 ], "controller" ], [ [ 0, 17 ], [ 0, 3 ], "subject" ], [ [ 0, 3 ], [ 0, 18 ], "outcome" ], [ [ 0, 18 ], [ 0, 4 ], "subject" ], [ [ 0, 4 ], [ 0, 16 ], "outcome" ], [ [ 0, 18 ], [ 0, 5 ], "subject" ], [ [ 0, 5 ], [ 0, 19 ], "outcome" ], [ [ 0, 18 ], [ 0, 6 ], "subject" ], [ [ 0, 6 ], [ 0, 21 ], "outcome" ], [ [ 0, 16 ], [ 0, 7 ], "subject" ], [ [ 0, 7 ], [ 0, 20 ], "outcome" ], [ [ 0, 16 ], [ 0, 8 ], "subject" ], [ [ 0, 8 ], [ 0, 21 ], "outcome" ], [ [ 0, 19 ], [ 0, 9 ], "subject" ], [ [ 0, 9 ], [ 0, 20 ], "outcome" ], [ [ 0, 19 ], [ 0, 10 ], "subject" ], [ [ 0, 10 ], [ 0, 21 ], "outcome" ], [ [ 0, 19 ], [ 0, 11 ], "subject" ], [ [ 0, 11 ], [ 0, 22 ], "outcome" ], [ [ 0, 20 ], [ 0, 12 ], "subject" ], [ [ 0, 12 ], [ 0, 22 ], "outcome" ], [ [ 0, 20 ], [ 0, 13 ], "subject" ], [ [ 0, 13 ], [ 0, 21 ], "outcome" ], [ [ 0, 22 ], [ 0, 14 ], "subject" ], [ [ 0, 14 ], [ 0, 23 ], "outcome" ], [ [ 0, 22 ], [ 0, 15 ], "subject" ], [ [ 0, 15 ], [ 0, 21 ], "outcome" ], [ [ 1, 17 ], [ 1, 0 ], "controller" ], [ [ 1, 18 ], [ 1, 0 ], "subject" ], [ [ 1, 0 ], [ 1, 19 ], "outcome" ], [ [ 1, 20 ], [ 1, 1 ], "controller" ], [ [ 1, 18 ], [ 1, 1 ], "subject" ], [ [ 1, 1 ], [ 1, 19 ], "outcome" ], [ [ 1, 21 ], [ 1, 2 ], "controller" ], [ [ 1, 18 ], [ 1, 2 ], "subject" ], [ [ 1, 2 ], [ 1, 19 ], "outcome" ], [ [ 1, 19 ], [ 1, 3 ], "controller" ], [ [ 1, 18 ], [ 1, 3 ], "subject" ], [ [ 1, 3 ], [ 1, 19 ], "outcome" ], [ [ 1, 19 ], [ 1, 4 ], "subject" ], [ [ 1, 4 ], [ 1, 17 ], "outcome" ], [ [ 1, 19 ], [ 1, 5 ], "subject" ], [ [ 1, 5 ], [ 1, 20 ], "outcome" ], [ [ 1, 19 ], [ 1, 6 ], "subject" ], [ [ 1, 6 ], [ 1, 22 ], "outcome" ], [ [ 1, 17 ], [ 1, 7 ], "subject" ], [ [ 1, 7 ], [ 1, 21 ], "outcome" ], [ [ 1, 17 ], [ 1, 8 ], "subject" ], [ [ 1, 8 ], [ 1, 22 ], "outcome" ], [ [ 1, 20 ], [ 1, 9 ], "subject" ], [ [ 1, 9 ], [ 1, 21 ], "outcome" ], [ [ 1, 20 ], [ 1, 10 ], "subject" ], [ [ 1, 10 ], [ 1, 22 ], "outcome" ], [ [ 1, 20 ], [ 1, 11 ], "subject" ], [ [ 1, 11 ], [ 1, 23 ], "outcome" ], [ [ 1, 21 ], [ 1, 12 ], "subject" ], [ [ 1, 12 ], [ 1, 23 ], "outcome" ], [ [ 1, 21 ], [ 1, 13 ], "subject" ], [ [ 1, 13 ], [ 1, 22 ], "outcome" ], [ [ 1, 23 ], [ 1, 14 ], "subject" ], [ [ 1, 14 ], [ 1, 24 ], "outcome" ], [ [ 1, 23 ], [ 1, 15 ], "subject" ], [ [ 1, 15 ], [ 1, 22 ], "outcome" ], [ [ 1, 18 ], [ 1, 16 ], "subject" ], [ [ 1, 16 ], [ 1, 25 ], "outcome" ] ] }';

const data = JSON.parse(dataStr);

// Create modelMap
const modelsMap = {}; // data.nodes;

const templateKeys = Object.keys(data.template_nodes);
templateKeys.forEach((key) => {
	if (modelsMap[key]) {
		const nodeKeys = Object.keys(data.template_nodes[key]);
		nodeKeys.forEach((key2) => {
			modelsMap[key][key2] = data.template_nodes[key][key2];
		});
	} else {
		modelsMap[key] = data.template_nodes[key];
	}
});
const conceptKeys = Object.keys(data.concept_nodes);
conceptKeys.forEach((key) => {
	if (modelsMap[key]) {
		const nodeKeys = Object.keys(data.concept_nodes[key]);
		nodeKeys.forEach((key2) => {
			modelsMap[key][key2] = data.concept_nodes[key][key2];
		});
	} else {
		modelsMap[key] = data.concept_nodes[key];
	}
});

const interModelEdges = data.inter_model_edges;
const intraModelEdges = data.intra_model_edges;

const equalPools: string[][] = [];

// Find equality nodes, create a list-of-list of all nodes thare are equivalent, note we will
// double count, if a = b and b = c, we will get [a, b, b, c]
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

// Create nodes, segment nodes into nodes that are specific to a model, and nodes that are shared across models
const modelKeys = Object.keys(modelsMap);
modelKeys.forEach((modelKey) => {
	const nodeKeys = Object.keys(modelsMap[modelKey]);
	const modelGroup = {
		id: modelKey,
		label: 'p',
		data: { name: modelKey, models: [] },
		nodes: []
	};

	nodeKeys.forEach((nodeKey) => {
		const hash = `${modelKey}:${nodeKey}`;
		const pool = equalPools.find((p) => p.includes(hash));

		if (!pool) {
			modelGroup.nodes.push({
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
	if (modelGroup.nodes.length > 0) {
		g.nodes.push(modelGroup);
	}
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
		const children = selection.filter((d) => d.nodes.length === 0);

		children
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', (d) => {
				if (d.data.models.length > 1) {
					return '#DDD';
				}
				return palette[d.data.models[0]];
			});

		const cat10 = palette;
		children.each((d, i, g2) => {
			if (d.data.models.length === 2) {
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
						.attr('fill', palette[d.data.models[idx]])
						.attr('stroke', '#888')
						.attr('d', arcExample);
				}
			} else if (d.data.models.length === 3) {
				for (let idx = 0; idx < 3; idx++) {
					const arcExample = d3
						.arc()
						.innerRadius(d.width * 0.5 + 5)
						.outerRadius(d.width * 0.5 + 25)
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
		children
			.append('text')
			.attr('y', 30)
			.attr('x', 60)
			.text((d) => `[${d.id}]`);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000')
			.style('stroke-width', 3)
			.attr('marker-end', `url(#${this.EDGE_ARROW_ID})`);
	}
}

// Entry
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
</script>
