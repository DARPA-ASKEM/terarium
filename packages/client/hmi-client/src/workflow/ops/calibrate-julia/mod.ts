import { CalibrationOperationJulia as operation } from './calibrate-operation';
import node from './tera-calibrate-node-julia.vue';
import drilldown from './tera-calibrate-julia.vue';

const name = operation.name;

export { name, operation, node, drilldown };
