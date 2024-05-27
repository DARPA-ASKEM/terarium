import axios, { AxiosHeaders } from 'axios';

const BeakerAPi = axios.create({
	baseURL: '/beaker',
	headers: new AxiosHeaders()
});

export const summarizeNotebook = async (notebook: any) => {
	const { data } = await BeakerAPi.post('/summary', { notebook });
	return data;
};
