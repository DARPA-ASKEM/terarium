import { EnsembleModelConfigs } from '@/types/Types';
import { SimulateEnsembleMappingRow, SimulateEnsembleWeight } from './simulate-ensemble-ciemss-operation';

export function clientMappingToCiemssMapping(
	mapping: SimulateEnsembleMappingRow[],
	weights: SimulateEnsembleWeight[]
): EnsembleModelConfigs[] {
	console.log(mapping);
	console.log(weights);
	return [] as EnsembleModelConfigs[];
}
