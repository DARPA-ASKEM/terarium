import { PetriNet } from '@/petrinet/petrinet-service';

export const shimPetriModel = (model: PetriNet) => {
	const cleanedModel: PetriNet = { S: [], T: [], I: [], O: [] };

	if (model) {
		const { S, T, I, O } = model;
		cleanedModel.S = S.map((s) => ({ sname: s.sname }));
		cleanedModel.T = T.map((t) => ({ tname: t.tname }));
		cleanedModel.I = I;
		cleanedModel.O = O;
	}
	return JSON.stringify(cleanedModel);
};
