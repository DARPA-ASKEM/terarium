import { PetriNet } from '@/petrinet/petrinet-service';

export const shimPetriModel = (model: PetriNet) => {
	const cleanedModel: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};
	if (model) {
		cleanedModel.S = model.S.map((s) => ({ sname: s.sname }));
		cleanedModel.T = model.T.map((t) => ({ tname: t.tname }));
		cleanedModel.I = model.I;
		cleanedModel.O = model.O;
	}
	return JSON.stringify(cleanedModel);
};
