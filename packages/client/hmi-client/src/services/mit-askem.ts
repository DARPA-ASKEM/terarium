import API, { Poller } from '@/api/api';

export async function codeToAcset(code: string) {
	const response = await API.post(`code/to-acset`, code);
	return response.data ?? null;
}

export async function findVarsFromText(text: string) {
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
	return metadata.data;
}

export async function getlinkedAnnotations(data) {
	const response = await API.post(`/code/annotation/link-annos-to-pyacset`, data);
	return response.data ?? null;
}
