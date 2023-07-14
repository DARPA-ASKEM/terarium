import { Model } from '@/types/Types';

export const extractModelMatrices = (model: Model) => {
	// const matrix: string | null[] = [];
	console.log(model);

	// const { transitions } = model.model;
	// const numOfDimensions = model.model.states.length;

	// const inputs: string[] = [];
	// const outputs: string[] = [];
	// const transition_ids: string[] = [];

	// // Get matrix size, # of input = # of "rows", # of outputs = # of "columns"
	// for (let i = 0; i < transitions.length; i++) {

	//     const { input, output, id } = transitions[i];

	//     inputs.push(input);
	//     outputs.push(output);
	//     transition_ids.push(...id);
	// }

	// console.log(inputs)
	// console.log(outputs)
	// console.log(matrix);

	// for (let i = 0; i < numOfDimensions; i++) {

	// }

	// for (let i = 0; i < transitions.length; i++) {
	//     for (let j = 0; j < inputs.length; j++) {

	//         let value = null;

	//         for (let k = 0; k < outputs.length; k++) {
	//             if (
	//                 (transitions[i].input === inputs[j]) &&
	//                 (transitions[i].output === outputs[k])
	//             ) {
	//                 value = transitions[i].id;
	//             }
	//         }
	//         matrix.push(value);
	//     }
	//     matrix.push()
	// }

	// // Get matrix dimensions
	// return matrix;
};
