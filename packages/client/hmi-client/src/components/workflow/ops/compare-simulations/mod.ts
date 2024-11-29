import { CompareSimulationsOperation as operation } from './compare-simulations-operation';
import node from './tera-compare-simulations-node.vue';
import drilldown from './tera-compare-simulations-drilldown.vue';

const name = operation.name;

export { name, operation, node, drilldown };
