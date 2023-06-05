/** FIXME: A temporary shim until the official mdoel-representation comes online - May 2023 * */
import { PetriNet } from '@/petrinet/petrinet-service';
import { ITypedModel } from '@/types/Model';

export const shimPetriModel = (model: ITypedModel<PetriNet>) => {
	const cleanedModel: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};
	if (model) {
		cleanedModel.S = model.content.S.map((s) => ({ sname: s.sname }));
		cleanedModel.T = model.content.T.map((t) => ({ tname: t.tname }));
		cleanedModel.I = model.content.I;
		cleanedModel.O = model.content.O;
	}
	return JSON.stringify(cleanedModel);
};
