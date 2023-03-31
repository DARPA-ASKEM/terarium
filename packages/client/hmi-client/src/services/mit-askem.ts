import API, { Poller } from '@/api/api';
import { logger } from '@/utils/logger';

export async function codeToAcset(code: string) {
	const response = await API.post(`code/to_acset`, code);
	return response.data ?? null;
}

export async function findVarsFromText(text: string) {
	const id = await API.post(`/code/annotation/find_text_vars`, text);
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
			logger.info(`Extracting variable metadata from text...`);
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
	const response = await API.post(`/code/annotation/link_annos_to_pyacset`, data);
	return response.data ?? null;
}
