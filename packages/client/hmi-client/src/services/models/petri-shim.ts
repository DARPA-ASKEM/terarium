/** FIXME: A temporary shim until the official mdoel-representation comes online - May 2023 * */
import { PetriNet } from '@/petrinet/petrinet-service';
import { Model } from '@/types/Types';

export const shimPetriModel = (model: Model) => {
	const cleanedModel: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};
	if (model) {
		console.log(
			'TODO: Is this needed at all? What format is sim servuce expecting post AMR update??'
		);
		console.log('TODO: I and O conversion if this is required');
		cleanedModel.S = model.model.states.map((s) => ({ name: s.name }));
		cleanedModel.T = model.model.transitions.map((t) => ({ name: t.name }));
		cleanedModel.I = [];
		cleanedModel.O = [];
	}

	return JSON.stringify(cleanedModel);
};
