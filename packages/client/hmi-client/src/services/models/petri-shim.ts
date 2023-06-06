/** FIXME: A temporary shim until the official mdoel-representation comes online - May 2023 * */
import { PetriNet } from '@/petrinet/petrinet-service';
import { ITypedModel } from '@/types/Model';

export const shimPetriModel = (model: ITypedModel<PetriNet>) => {
	const { S, T, I, O } = model.content;
	const cleanedModel: PetriNet = {
		S: S.map((s) => ({ sname: s.sname })),
		T: T.map((t) => ({ tname: t.tname })),
		I,
		O
	};
	return JSON.stringify(cleanedModel);
};
