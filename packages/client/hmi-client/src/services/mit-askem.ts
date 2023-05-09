import API, { Poller } from '@/api/api';

export async function codeToAcset(code: string) {
	const response = await API.post(`code/to-acset`, code);
	return response.data ?? null;
}

export async function findVarsFromText(text: string): Promise<FindVarsFromTextResponseType | null> {
	const id = await API.post(`/code/annotation/find-text-vars`, text);
	const poller = new Poller<object>()
		.setInterval(2000)
		.setThreshold(90)
		.setPollAction(async () => {
			const response = await API.get(`/code/response?id=${id.data}`);
			if (response) {
				return {
					data: response.data.data,
					progress: null,
					error: null
				};
			}
			return {
				data: null,
				progress: null,
				error: null
			};
		});
	const metadata = await poller.start();
	if (metadata.data === null) {
		return null;
	}
	return metadata.data as FindVarsFromTextResponseType;
}

export async function getlinkedAnnotations(data) {
	const response = await API.post(`/code/annotation/link-annos-to-pyacset`, data);
	return response.data ?? null;
}

// Sample return
// {
// 	'type': 'variable',
// 		'name': 'S',
// 			'id': 'v0',
// 				'text_annotations': [' Susceptible (uninfected)'],
// 					'dkg_annotations': [['ncit:C171133', 'COVID-19 Infection'],
// 					['ido:0000514', 'susceptible population']]
// }
export interface FindVarsFromTextResponseType {
	name: string;
	id: string;
	text_annotations: string[];
	dkg_annotations: [string, string][];
}
