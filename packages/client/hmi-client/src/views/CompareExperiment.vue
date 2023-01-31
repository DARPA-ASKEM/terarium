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

const dataStr = `{ "graph_comparison_data": { "template_models": { "0": { "templates": [ { "rate_law": {}, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} } ], "subject": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "outcome": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "outcome": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "outcome": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "outcome": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] } ], "parameters": { "beta": { "name": "beta", "identifiers": {}, "context": {}, "value": 2.8 }, "beta_prime": { "name": "beta_prime", "identifiers": {}, "context": {}, "value": 7.65 }, "rho1": { "name": "rho1", "identifiers": {}, "context": {}, "value": 0.58 }, "rho2": { "name": "rho2", "identifiers": {}, "context": {}, "value": 0.001 }, "l": { "name": "l", "identifiers": {}, "context": {}, "value": 1.56 }, "kappa": { "name": "kappa", "identifiers": {}, "context": {}, "value": 0.25 }, "gamma_a": { "name": "gamma_a", "identifiers": {}, "context": {}, "value": 0.94 }, "gamma_i": { "name": "gamma_i", "identifiers": {}, "context": {}, "value": 0.27 }, "gamma_r": { "name": "gamma_r", "identifiers": {}, "context": {}, "value": 0.5 }, "delta_i": { "name": "delta_i", "identifiers": {}, "context": {}, "value": 0.035 }, "delta_p": { "name": "delta_p", "identifiers": {}, "context": {}, "value": 1.0 }, "delta_h": { "name": "delta_h", "identifiers": {}, "context": {}, "value": 0.085 }, "N": { "name": "N", "identifiers": {}, "context": {}, "value": 44000.0 }, "Confimed_cases": { "name": "Confimed_cases", "identifiers": {}, "context": {}, "value": 6.0 }, "Wuhan": { "name": "Wuhan", "identifiers": {}, "context": {}, "value": 1.0 } }, "initials": { "Susceptible": { "concept": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "value": 43994.0 }, "Exposed": { "concept": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "value": 0.0 }, "Infectious": { "concept": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "value": 1.0 }, "Super_spreaders": { "concept": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "value": 5.0 }, "Asymptomatic": { "concept": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "value": 0.0 }, "Hospitalised": { "concept": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "value": 0.0 }, "Recovered": { "concept": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} }, "value": 0.0 }, "Fatalities": { "concept": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "value": 0.0 } } }, "1": { "templates": [ { "rate_law": {}, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} } ], "subject": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "provenance": [] } ], "parameters": { "_1_Trigger_China": { "name": "_1_Trigger_China", "identifiers": {}, "context": {}, "value": 1.0 }, "_2_Trigger_Italy": { "name": "_2_Trigger_Italy", "identifiers": {}, "context": {}, "value": 0.0 }, "_3_Trigger_Spain": { "name": "_3_Trigger_Spain", "identifiers": {}, "context": {}, "value": 0.0 }, "_4_Trigger_France": { "name": "_4_Trigger_France", "identifiers": {}, "context": {}, "value": 0.0 }, "_5_Trigger_Germany": { "name": "_5_Trigger_Germany", "identifiers": {}, "context": {}, "value": 0.0 }, "_6_Trigger_USA": { "name": "_6_Trigger_USA", "identifiers": {}, "context": {}, "value": 0.0 }, "beta_1": { "name": "beta_1", "identifiers": {}, "context": {}, "value": 0.334 }, "l_1": { "name": "l_1", "identifiers": {}, "context": {}, "value": 0.673 }, "l_a_1": { "name": "l_a_1", "identifiers": {}, "context": {}, "value": 8.0 }, "kappa": { "name": "kappa", "identifiers": {}, "context": {}, "value": 0.44 }, "rho": { "name": "rho", "identifiers": {}, "context": {}, "value": 0.053 }, "gamma_a": { "name": "gamma_a", "identifiers": {}, "context": {}, "value": 0.503 }, "gamma_i": { "name": "gamma_i", "identifiers": {}, "context": {}, "value": 0.263 }, "gamma_r": { "name": "gamma_r", "identifiers": {}, "context": {}, "value": 0.141 }, "mu": { "name": "mu", "identifiers": {}, "context": {}, "value": 1.64 }, "delta_A": { "name": "delta_A", "identifiers": {}, "context": {}, "value": 0.0 }, "delta_H": { "name": "delta_H", "identifiers": {}, "context": {}, "value": 0.008 }, "delta_I": { "name": "delta_I", "identifiers": {}, "context": {}, "value": 0.003 }, "Initial_infected_pop": { "name": "Initial_infected_pop", "identifiers": {}, "context": {}, "value": 732.0 }, "kappa_rho": { "name": "kappa_rho", "identifiers": {}, "context": {}, "value": 0.02332 }, "kappa_1_rho": { "name": "kappa_1_rho", "identifiers": {}, "context": {}, "value": 0.41668 }, "mu_delta_A": { "name": "mu_delta_A", "identifiers": {}, "context": {}, "value": 0.0 }, "mu_1_delta_A": { "name": "mu_1_delta_A", "identifiers": {}, "context": {}, "value": 1.64 }, "Time_threshold": { "name": "Time_threshold", "identifiers": {}, "context": {}, "value": 18.0 }, "beta_1_China": { "name": "beta_1_China", "identifiers": {}, "context": {}, "value": 0.334 }, "beta_2_China": { "name": "beta_2_China", "identifiers": {}, "context": {}, "value": 0.14 }, "l_1_China": { "name": "l_1_China", "identifiers": {}, "context": {}, "value": 0.673 }, "l_2_China": { "name": "l_2_China", "identifiers": {}, "context": {}, "value": 0.135 }, "l_a_1_China": { "name": "l_a_1_China", "identifiers": {}, "context": {}, "value": 8.0 }, "l_a_2_China": { "name": "l_a_2_China", "identifiers": {}, "context": {}, "value": 8.0 }, "kappa_China": { "name": "kappa_China", "identifiers": {}, "context": {}, "value": 0.44 }, "rho_China": { "name": "rho_China", "identifiers": {}, "context": {}, "value": 0.053 }, "gamma_a_China": { "name": "gamma_a_China", "identifiers": {}, "context": {}, "value": 0.503 }, "gamma_i_China": { "name": "gamma_i_China", "identifiers": {}, "context": {}, "value": 0.263 }, "gamma_r_China": { "name": "gamma_r_China", "identifiers": {}, "context": {}, "value": 0.141 }, "mu_China": { "name": "mu_China", "identifiers": {}, "context": {}, "value": 1.64 }, "delta_A_China": { "name": "delta_A_China", "identifiers": {}, "context": {}, "value": 0.0 }, "delta_H_China": { "name": "delta_H_China", "identifiers": {}, "context": {}, "value": 0.008 }, "delta_I_China": { "name": "delta_I_China", "identifiers": {}, "context": {}, "value": 0.003 }, "Initial_infected_pop_China": { "name": "Initial_infected_pop_China", "identifiers": {}, "context": {}, "value": 732.0 }, "Time_threshold_China": { "name": "Time_threshold_China", "identifiers": {}, "context": {}, "value": 18.0 }, "beta_1_Italy": { "name": "beta_1_Italy", "identifiers": {}, "context": {}, "value": 0.189 }, "beta_2_Italy": { "name": "beta_2_Italy", "identifiers": {}, "context": {}, "value": 0.081 }, "l_1_Italy": { "name": "l_1_Italy", "identifiers": {}, "context": {}, "value": 8.0 }, "l_2_Italy": { "name": "l_2_Italy", "identifiers": {}, "context": {}, "value": 8.0 }, "l_a_1_Italy": { "name": "l_a_1_Italy", "identifiers": {}, "context": {}, "value": 0.649 }, "l_a_2_Italy": { "name": "l_a_2_Italy", "identifiers": {}, "context": {}, "value": 0.649 }, "kappa_Italy": { "name": "kappa_Italy", "identifiers": {}, "context": {}, "value": 0.284 }, "rho_Italy": { "name": "rho_Italy", "identifiers": {}, "context": {}, "value": 0.27 }, "gamma_a_Italy": { "name": "gamma_a_Italy", "identifiers": {}, "context": {}, "value": 0.224 }, "gamma_i_Italy": { "name": "gamma_i_Italy", "identifiers": {}, "context": {}, "value": 0.04 }, "mu_Italy": { "name": "mu_Italy", "identifiers": {}, "context": {}, "value": 0.146 }, "delta_A_Italy": { "name": "delta_A_Italy", "identifiers": {}, "context": {}, "value": 0.0 }, "delta_H_Italy": { "name": "delta_H_Italy", "identifiers": {}, "context": {}, "value": 0.023 }, "delta_I_Italy": { "name": "delta_I_Italy", "identifiers": {}, "context": {}, "value": 0.023 }, "Initial_infected_pop_Italy": { "name": "Initial_infected_pop_Italy", "identifiers": {}, "context": {}, "value": 648.0 }, "Time_threshold_Italy": { "name": "Time_threshold_Italy", "identifiers": {}, "context": {}, "value": 30.0 }, "beta_1_Spain": { "name": "beta_1_Spain", "identifiers": {}, "context": {}, "value": 0.382 }, "beta_2_Spain": { "name": "beta_2_Spain", "identifiers": {}, "context": {}, "value": 0.16 }, "l_1_Spain": { "name": "l_1_Spain", "identifiers": {}, "context": {}, "value": 7.69 }, "l_2_Spain": { "name": "l_2_Spain", "identifiers": {}, "context": {}, "value": 6.49 }, "l_a_1_Spain": { "name": "l_a_1_Spain", "identifiers": {}, "context": {}, "value": 3.9 }, "l_a_2_Spain": { "name": "l_a_2_Spain", "identifiers": {}, "context": {}, "value": 3.9 }, "kappa_Spain": { "name": "kappa_Spain", "identifiers": {}, "context": {}, "value": 0.362 }, "rho_Spain": { "name": "rho_Spain", "identifiers": {}, "context": {}, "value": 0.102 }, "gamma_a_Spain": { "name": "gamma_a_Spain", "identifiers": {}, "context": {}, "value": 0.116 }, "gamma_i_Spain": { "name": "gamma_i_Spain", "identifiers": {}, "context": {}, "value": 0.063 }, "gamma_r_Spain": { "name": "gamma_r_Spain", "identifiers": {}, "context": {}, "value": 0.281 }, "mu_Spain": { "name": "mu_Spain", "identifiers": {}, "context": {}, "value": 1.03 }, "delta_A_Spain": { "name": "delta_A_Spain", "identifiers": {}, "context": {}, "value": 0.0 }, "delta_H_Spain": { "name": "delta_H_Spain", "identifiers": {}, "context": {}, "value": 0.019 }, "delta_I_Spain": { "name": "delta_I_Spain", "identifiers": {}, "context": {}, "value": 0.016 }, "Initial_infected_pop_Spain": { "name": "Initial_infected_pop_Spain", "identifiers": {}, "context": {}, "value": 500.0 }, "Time_threshold_Spain": { "name": "Time_threshold_Spain", "identifiers": {}, "context": {}, "value": 23.0 }, "beta_1_France": { "name": "beta_1_France", "identifiers": {}, "context": {}, "value": 0.298 }, "beta_2_France": { "name": "beta_2_France", "identifiers": {}, "context": {}, "value": 0.129 }, "l_1_France": { "name": "l_1_France", "identifiers": {}, "context": {}, "value": 8.0 }, "l_2_France": { "name": "l_2_France", "identifiers": {}, "context": {}, "value": 8.0 }, "l_a_1_France": { "name": "l_a_1_France", "identifiers": {}, "context": {}, "value": 8.0 }, "l_a_2_France": { "name": "l_a_2_France", "identifiers": {}, "context": {}, "value": 8.0 }, "kappa_France": { "name": "kappa_France", "identifiers": {}, "context": {}, "value": 0.309 }, "rho_France": { "name": "rho_France", "identifiers": {}, "context": {}, "value": 0.033 }, "gamma_a_France": { "name": "gamma_a_France", "identifiers": {}, "context": {}, "value": 0.3 }, "gamma_i_France": { "name": "gamma_i_France", "identifiers": {}, "context": {}, "value": 0.02 }, "gamma_r_France": { "name": "gamma_r_France", "identifiers": {}, "context": {}, "value": 0.131 }, "mu_France": { "name": "mu_France", "identifiers": {}, "context": {}, "value": 1.53 }, "delta_A_France": { "name": "delta_A_France", "identifiers": {}, "context": {}, "value": 0.0 }, "delta_H_France": { "name": "delta_H_France", "identifiers": {}, "context": {}, "value": 0.029 }, "delta_I_France": { "name": "delta_I_France", "identifiers": {}, "context": {}, "value": 0.018 }, "Initial_infected_pop_France": { "name": "Initial_infected_pop_France", "identifiers": {}, "context": {}, "value": 575.0 }, "Time_threshold_France": { "name": "Time_threshold_France", "identifiers": {}, "context": {}, "value": 26.0 }, "beta_1_Germany": { "name": "beta_1_Germany", "identifiers": {}, "context": {}, "value": 0.135 }, "beta_2_Germany": { "name": "beta_2_Germany", "identifiers": {}, "context": {}, "value": 0.055 }, "l_1_Germany": { "name": "l_1_Germany", "identifiers": {}, "context": {}, "value": 4.8 }, "l_2_Germany": { "name": "l_2_Germany", "identifiers": {}, "context": {}, "value": 1.13 }, "l_a_1_Germany": { "name": "l_a_1_Germany", "identifiers": {}, "context": {}, "value": 4.9 }, "l_a_2_Germany": { "name": "l_a_2_Germany", "identifiers": {}, "context": {}, "value": 4.9 }, "kappa_Germany": { "name": "kappa_Germany", "identifiers": {}, "context": {}, "value": 0.578 }, "rho_Germany": { "name": "rho_Germany", "identifiers": {}, "context": {}, "value": 0.021 }, "gamma_a_Germany": { "name": "gamma_a_Germany", "identifiers": {}, "context": {}, "value": 0.542 }, "gamma_i_Germany": { "name": "gamma_i_Germany", "identifiers": {}, "context": {}, "value": 0.05 }, "gamma_r_Germany": { "name": "gamma_r_Germany", "identifiers": {}, "context": {}, "value": 0.036 }, "mu_Germany": { "name": "mu_Germany", "identifiers": {}, "context": {}, "value": 0.302 }, "delta_A_Germany": { "name": "delta_A_Germany", "identifiers": {}, "context": {}, "value": 0.0 }, "delta_H_Germany": { "name": "delta_H_Germany", "identifiers": {}, "context": {}, "value": 0.003 }, "delta_I_Germany": { "name": "delta_I_Germany", "identifiers": {}, "context": {}, "value": 0.002 }, "Initial_infected_pop_Germany": { "name": "Initial_infected_pop_Germany", "identifiers": {}, "context": {}, "value": 735.0 }, "Time_threshold_Germany": { "name": "Time_threshold_Germany", "identifiers": {}, "context": {}, "value": 24.0 }, "beta_1_USA": { "name": "beta_1_USA", "identifiers": {}, "context": {}, "value": 0.303 }, "beta_2_USA": { "name": "beta_2_USA", "identifiers": {}, "context": {}, "value": 0.13 }, "l_1_USA": { "name": "l_1_USA", "identifiers": {}, "context": {}, "value": 0.851 }, "l_2_USA": { "name": "l_2_USA", "identifiers": {}, "context": {}, "value": 0.851 }, "l_a_1_USA": { "name": "l_a_1_USA", "identifiers": {}, "context": {}, "value": 4.09 }, "l_a_2_USA": { "name": "l_a_2_USA", "identifiers": {}, "context": {}, "value": 0.82 }, "kappa_USA": { "name": "kappa_USA", "identifiers": {}, "context": {}, "value": 1.33 }, "rho_USA": { "name": "rho_USA", "identifiers": {}, "context": {}, "value": 1.01 }, "gamma_a_USA": { "name": "gamma_a_USA", "identifiers": {}, "context": {}, "value": 0.055 }, "gamma_i_USA": { "name": "gamma_i_USA", "identifiers": {}, "context": {}, "value": 0.296 }, "gamma_r_USA": { "name": "gamma_r_USA", "identifiers": {}, "context": {}, "value": 0.018 }, "mu_USA": { "name": "mu_USA", "identifiers": {}, "context": {}, "value": 0.828 }, "delta_A_USA": { "name": "delta_A_USA", "identifiers": {}, "context": {}, "value": 0.0 }, "delta_H_USA": { "name": "delta_H_USA", "identifiers": {}, "context": {}, "value": 0.00029 }, "delta_I_USA": { "name": "delta_I_USA", "identifiers": {}, "context": {}, "value": 0.023 }, "Initial_infected_pop_USA": { "name": "Initial_infected_pop_USA", "identifiers": {}, "context": {}, "value": 576.0 }, "Time_threshold_USA": { "name": "Time_threshold_USA", "identifiers": {}, "context": {}, "value": 51.0 }, "gamma_r_Italy": { "name": "gamma_r_Italy", "identifiers": {}, "context": {}, "value": 0.24 }, "Total_pop": { "name": "Total_pop", "identifiers": {}, "context": {}, "value": 11000000.0 }, "Total_pop_China": { "name": "Total_pop_China", "identifiers": {}, "context": {}, "value": 11000000.0 }, "Total_pop_Italy": { "name": "Total_pop_Italy", "identifiers": {}, "context": {}, "value": 60400000.0 }, "Total_pop_Spain": { "name": "Total_pop_Spain", "identifiers": {}, "context": {}, "value": 46900000.0 }, "Total_pop_France": { "name": "Total_pop_France", "identifiers": {}, "context": {}, "value": 67000000.0 }, "Total_pop_Germany": { "name": "Total_pop_Germany", "identifiers": {}, "context": {}, "value": 83000000.0 }, "Total_pop_USA": { "name": "Total_pop_USA", "identifiers": {}, "context": {}, "value": 328200000.0 }, "ModelValue_0": { "name": "ModelValue_0", "identifiers": {}, "context": {}, "value": 1.0 }, "ModelValue_1": { "name": "ModelValue_1", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_2": { "name": "ModelValue_2", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_3": { "name": "ModelValue_3", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_4": { "name": "ModelValue_4", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_5": { "name": "ModelValue_5", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_42": { "name": "ModelValue_42", "identifiers": {}, "context": {}, "value": 732.0 }, "ModelValue_92": { "name": "ModelValue_92", "identifiers": {}, "context": {}, "value": 575.0 }, "ModelValue_109": { "name": "ModelValue_109", "identifiers": {}, "context": {}, "value": 735.0 }, "ModelValue_58": { "name": "ModelValue_58", "identifiers": {}, "context": {}, "value": 648.0 }, "ModelValue_75": { "name": "ModelValue_75", "identifiers": {}, "context": {}, "value": 500.0 }, "ModelValue_126": { "name": "ModelValue_126", "identifiers": {}, "context": {}, "value": 576.0 }, "ModelValue_26": { "name": "ModelValue_26", "identifiers": {}, "context": {}, "value": 18.0 }, "ModelValue_43": { "name": "ModelValue_43", "identifiers": {}, "context": {}, "value": 18.0 }, "ModelValue_93": { "name": "ModelValue_93", "identifiers": {}, "context": {}, "value": 26.0 }, "ModelValue_110": { "name": "ModelValue_110", "identifiers": {}, "context": {}, "value": 24.0 }, "ModelValue_59": { "name": "ModelValue_59", "identifiers": {}, "context": {}, "value": 30.0 }, "ModelValue_76": { "name": "ModelValue_76", "identifiers": {}, "context": {}, "value": 23.0 }, "ModelValue_127": { "name": "ModelValue_127", "identifiers": {}, "context": {}, "value": 51.0 }, "ModelValue_130": { "name": "ModelValue_130", "identifiers": {}, "context": {}, "value": 11000000.0 }, "ModelValue_133": { "name": "ModelValue_133", "identifiers": {}, "context": {}, "value": 67000000.0 }, "ModelValue_134": { "name": "ModelValue_134", "identifiers": {}, "context": {}, "value": 83000000.0 }, "ModelValue_131": { "name": "ModelValue_131", "identifiers": {}, "context": {}, "value": 60400000.0 }, "ModelValue_132": { "name": "ModelValue_132", "identifiers": {}, "context": {}, "value": 46900000.0 }, "ModelValue_135": { "name": "ModelValue_135", "identifiers": {}, "context": {}, "value": 328200000.0 }, "ModelValue_18": { "name": "ModelValue_18", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_39": { "name": "ModelValue_39", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_89": { "name": "ModelValue_89", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_106": { "name": "ModelValue_106", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_55": { "name": "ModelValue_55", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_72": { "name": "ModelValue_72", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_123": { "name": "ModelValue_123", "identifiers": {}, "context": {}, "value": 0.0 }, "ModelValue_40": { "name": "ModelValue_40", "identifiers": {}, "context": {}, "value": 0.008 }, "ModelValue_90": { "name": "ModelValue_90", "identifiers": {}, "context": {}, "value": 0.029 }, "ModelValue_107": { "name": "ModelValue_107", "identifiers": {}, "context": {}, "value": 0.003 }, "ModelValue_56": { "name": "ModelValue_56", "identifiers": {}, "context": {}, "value": 0.023 }, "ModelValue_73": { "name": "ModelValue_73", "identifiers": {}, "context": {}, "value": 0.019 }, "ModelValue_124": { "name": "ModelValue_124", "identifiers": {}, "context": {}, "value": 0.00029 }, "ModelValue_41": { "name": "ModelValue_41", "identifiers": {}, "context": {}, "value": 0.003 }, "ModelValue_91": { "name": "ModelValue_91", "identifiers": {}, "context": {}, "value": 0.018 }, "ModelValue_108": { "name": "ModelValue_108", "identifiers": {}, "context": {}, "value": 0.002 }, "ModelValue_57": { "name": "ModelValue_57", "identifiers": {}, "context": {}, "value": 0.023 }, "ModelValue_74": { "name": "ModelValue_74", "identifiers": {}, "context": {}, "value": 0.016 }, "ModelValue_125": { "name": "ModelValue_125", "identifiers": {}, "context": {}, "value": 0.023 }, "ModelValue_30": { "name": "ModelValue_30", "identifiers": {}, "context": {}, "value": 0.503 }, "ModelValue_85": { "name": "ModelValue_85", "identifiers": {}, "context": {}, "value": 0.3 }, "ModelValue_102": { "name": "ModelValue_102", "identifiers": {}, "context": {}, "value": 0.542 }, "ModelValue_52": { "name": "ModelValue_52", "identifiers": {}, "context": {}, "value": 0.224 }, "ModelValue_68": { "name": "ModelValue_68", "identifiers": {}, "context": {}, "value": 0.116 }, "ModelValue_119": { "name": "ModelValue_119", "identifiers": {}, "context": {}, "value": 0.055 }, "ModelValue_29": { "name": "ModelValue_29", "identifiers": {}, "context": {}, "value": 0.263 }, "ModelValue_86": { "name": "ModelValue_86", "identifiers": {}, "context": {}, "value": 0.02 }, "ModelValue_103": { "name": "ModelValue_103", "identifiers": {}, "context": {}, "value": 0.05 }, "ModelValue_53": { "name": "ModelValue_53", "identifiers": {}, "context": {}, "value": 0.04 }, "ModelValue_69": { "name": "ModelValue_69", "identifiers": {}, "context": {}, "value": 0.063 }, "ModelValue_120": { "name": "ModelValue_120", "identifiers": {}, "context": {}, "value": 0.296 }, "ModelValue_28": { "name": "ModelValue_28", "identifiers": {}, "context": {}, "value": 0.141 }, "ModelValue_87": { "name": "ModelValue_87", "identifiers": {}, "context": {}, "value": 0.131 }, "ModelValue_104": { "name": "ModelValue_104", "identifiers": {}, "context": {}, "value": 0.036 }, "ModelValue_128": { "name": "ModelValue_128", "identifiers": {}, "context": {}, "value": 0.24 }, "ModelValue_70": { "name": "ModelValue_70", "identifiers": {}, "context": {}, "value": 0.281 }, "ModelValue_121": { "name": "ModelValue_121", "identifiers": {}, "context": {}, "value": 0.018 }, "ModelValue_12": { "name": "ModelValue_12", "identifiers": {}, "context": {}, "value": 0.44 }, "ModelValue_32": { "name": "ModelValue_32", "identifiers": {}, "context": {}, "value": 0.44 }, "ModelValue_83": { "name": "ModelValue_83", "identifiers": {}, "context": {}, "value": 0.309 }, "ModelValue_100": { "name": "ModelValue_100", "identifiers": {}, "context": {}, "value": 0.578 }, "ModelValue_50": { "name": "ModelValue_50", "identifiers": {}, "context": {}, "value": 0.284 }, "ModelValue_66": { "name": "ModelValue_66", "identifiers": {}, "context": {}, "value": 0.362 }, "ModelValue_117": { "name": "ModelValue_117", "identifiers": {}, "context": {}, "value": 1.33 }, "ModelValue_64": { "name": "ModelValue_64", "identifiers": {}, "context": {}, "value": 3.9 }, "ModelValue_17": { "name": "ModelValue_17", "identifiers": {}, "context": {}, "value": 1.64 }, "ModelValue_27": { "name": "ModelValue_27", "identifiers": {}, "context": {}, "value": 1.64 }, "ModelValue_88": { "name": "ModelValue_88", "identifiers": {}, "context": {}, "value": 1.53 }, "ModelValue_105": { "name": "ModelValue_105", "identifiers": {}, "context": {}, "value": 0.302 }, "ModelValue_54": { "name": "ModelValue_54", "identifiers": {}, "context": {}, "value": 0.146 }, "ModelValue_71": { "name": "ModelValue_71", "identifiers": {}, "context": {}, "value": 1.03 }, "ModelValue_122": { "name": "ModelValue_122", "identifiers": {}, "context": {}, "value": 0.828 }, "ModelValue_13": { "name": "ModelValue_13", "identifiers": {}, "context": {}, "value": 0.053 }, "ModelValue_31": { "name": "ModelValue_31", "identifiers": {}, "context": {}, "value": 0.053 }, "ModelValue_84": { "name": "ModelValue_84", "identifiers": {}, "context": {}, "value": 0.033 }, "ModelValue_101": { "name": "ModelValue_101", "identifiers": {}, "context": {}, "value": 0.021 }, "ModelValue_51": { "name": "ModelValue_51", "identifiers": {}, "context": {}, "value": 0.27 }, "ModelValue_67": { "name": "ModelValue_67", "identifiers": {}, "context": {}, "value": 0.102 }, "ModelValue_118": { "name": "ModelValue_118", "identifiers": {}, "context": {}, "value": 1.01 }, "Country": { "name": "Country", "identifiers": {}, "context": {}, "value": 1.0 } }, "initials": { "Susceptible": { "concept": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "value": 9900000.0 }, "Exposed": { "concept": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "value": 36600.0 }, "Infectious": { "concept": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "value": 732.0 }, "Asymptomatic": { "concept": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "value": 2196.0 }, "Hospitalized": { "concept": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "value": 0.0 }, "Recovered": { "concept": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "value": 0.0 }, "Deceased": { "concept": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} }, "value": 0.0 }, "Cumulative_Cases": { "concept": { "name": "Cumulative_Cases", "identifiers": { "ncit": "C171133", "ido": "0000480" }, "context": {} }, "value": 0.0 } } } }, "concept_nodes": { "0": { "12": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "13": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "14": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "15": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "16": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "17": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "18": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "19": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} } }, "1": { "10": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "11": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "12": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "13": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "14": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "15": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "16": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} } } }, "template_nodes": { "0": { "0": { "rate_law": {}, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} } ], "subject": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "provenance": [] }, "1": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "outcome": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "provenance": [] }, "2": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "outcome": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "provenance": [] }, "3": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000597" }, "context": {} }, "outcome": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "provenance": [] }, "4": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "5": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "6": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Super_spreaders", "identifiers": { "ncit": "0000511", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "7": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "8": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "9": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000463" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "10": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "11": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalised", "identifiers": { "ncit": "C171133" }, "context": {} }, "outcome": { "name": "Fatalities", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] } }, "1": { "0": { "rate_law": {}, "type": "GroupedControlledConversion", "controllers": [ { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} } ], "subject": { "name": "Susceptible", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "provenance": [] }, "1": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "provenance": [] }, "2": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Exposed", "identifiers": { "ncit": "C171133", "ido": "0000514" }, "context": {} }, "outcome": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "provenance": [] }, "3": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "provenance": [] }, "4": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "provenance": [] }, "5": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Infectious", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "6": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "provenance": [] }, "7": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Asymptomatic", "identifiers": { "ncit": "C171133", "ido": "0000511" }, "context": { "disease_status": "ncit:C3833" } }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "8": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Deceased", "identifiers": { "ncit": "C171133" }, "context": {} }, "provenance": [] }, "9": { "rate_law": {}, "type": "NaturalConversion", "subject": { "name": "Hospitalized", "identifiers": { "ncit": "C168447", "ido": "0000511" }, "context": {} }, "outcome": { "name": "Recovered", "identifiers": { "ncit": "C171133", "ido": "0000621" }, "context": {} }, "provenance": [] } } }, "inter_model_edges": [ [ [ 0, 12 ], [ 0, 14 ], "is_equal" ], [ [ 0, 13 ], [ 0, 18 ], "is_equal" ], [ [ 0, 13 ], [ 0, 19 ], "is_equal" ], [ [ 0, 13 ], [ 1, 16 ], "is_equal" ], [ [ 0, 15 ], [ 1, 13 ], "is_equal" ], [ [ 0, 15 ], [ 1, 14 ], "is_equal" ], [ [ 0, 17 ], [ 1, 10 ], "refinement_of" ], [ [ 0, 17 ], [ 1, 11 ], "is_equal" ], [ [ 0, 17 ], [ 1, 12 ], "refinement_of" ], [ [ 0, 18 ], [ 0, 19 ], "is_equal" ], [ [ 0, 18 ], [ 1, 16 ], "is_equal" ], [ [ 0, 19 ], [ 1, 16 ], "is_equal" ], [ [ 1, 11 ], [ 1, 10 ], "refinement_of" ], [ [ 1, 10 ], [ 1, 12 ], "is_equal" ], [ [ 1, 11 ], [ 1, 12 ], "refinement_of" ], [ [ 1, 13 ], [ 1, 14 ], "is_equal" ] ], "intra_model_edges": [ [ [ 0, 12 ], [ 0, 0 ], "controllers" ], [ [ 0, 13 ], [ 0, 0 ], "controllers" ], [ [ 0, 14 ], [ 0, 0 ], "controllers" ], [ [ 0, 15 ], [ 0, 0 ], "subject" ], [ [ 0, 0 ], [ 0, 16 ], "outcome" ], [ [ 0, 16 ], [ 0, 1 ], "subject" ], [ [ 0, 1 ], [ 0, 17 ], "outcome" ], [ [ 0, 16 ], [ 0, 2 ], "subject" ], [ [ 0, 2 ], [ 0, 14 ], "outcome" ], [ [ 0, 16 ], [ 0, 3 ], "subject" ], [ [ 0, 3 ], [ 0, 12 ], "outcome" ], [ [ 0, 14 ], [ 0, 4 ], "subject" ], [ [ 0, 4 ], [ 0, 18 ], "outcome" ], [ [ 0, 14 ], [ 0, 5 ], "subject" ], [ [ 0, 5 ], [ 0, 13 ], "outcome" ], [ [ 0, 14 ], [ 0, 6 ], "subject" ], [ [ 0, 6 ], [ 0, 19 ], "outcome" ], [ [ 0, 12 ], [ 0, 7 ], "subject" ], [ [ 0, 7 ], [ 0, 18 ], "outcome" ], [ [ 0, 12 ], [ 0, 8 ], "subject" ], [ [ 0, 8 ], [ 0, 13 ], "outcome" ], [ [ 0, 12 ], [ 0, 9 ], "subject" ], [ [ 0, 9 ], [ 0, 19 ], "outcome" ], [ [ 0, 13 ], [ 0, 10 ], "subject" ], [ [ 0, 10 ], [ 0, 19 ], "outcome" ], [ [ 0, 13 ], [ 0, 11 ], "subject" ], [ [ 0, 11 ], [ 0, 18 ], "outcome" ], [ [ 1, 10 ], [ 1, 0 ], "controllers" ], [ [ 1, 11 ], [ 1, 0 ], "controllers" ], [ [ 1, 12 ], [ 1, 0 ], "controllers" ], [ [ 1, 13 ], [ 1, 0 ], "subject" ], [ [ 1, 0 ], [ 1, 14 ], "outcome" ], [ [ 1, 14 ], [ 1, 1 ], "subject" ], [ [ 1, 1 ], [ 1, 10 ], "outcome" ], [ [ 1, 14 ], [ 1, 2 ], "subject" ], [ [ 1, 2 ], [ 1, 11 ], "outcome" ], [ [ 1, 10 ], [ 1, 3 ], "subject" ], [ [ 1, 3 ], [ 1, 12 ], "outcome" ], [ [ 1, 10 ], [ 1, 4 ], "subject" ], [ [ 1, 4 ], [ 1, 15 ], "outcome" ], [ [ 1, 10 ], [ 1, 5 ], "subject" ], [ [ 1, 5 ], [ 1, 16 ], "outcome" ], [ [ 1, 11 ], [ 1, 6 ], "subject" ], [ [ 1, 6 ], [ 1, 15 ], "outcome" ], [ [ 1, 11 ], [ 1, 7 ], "subject" ], [ [ 1, 7 ], [ 1, 16 ], "outcome" ], [ [ 1, 12 ], [ 1, 8 ], "subject" ], [ [ 1, 8 ], [ 1, 16 ], "outcome" ], [ [ 1, 12 ], [ 1, 9 ], "subject" ], [ [ 1, 9 ], [ 1, 15 ], "outcome" ] ] }, "similarity_scores": [ { "models": [ 0, 1 ], "score": 0.625 } ] } `;

const data = JSON.parse(dataStr).graph_comparison_data;

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

			const n = g.nodes.find((d) => d.id === surrogateId) as any;
			if (!n.data.models.includes(modelKey)) {
				n.data.models.push(modelKey);
			}
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
