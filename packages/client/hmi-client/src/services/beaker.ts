import axios, { AxiosHeaders } from 'axios';

const BeakerAPI = axios.create({
	baseURL: '/beaker',
	headers: new AxiosHeaders()
});

export const summarizeNotebook = async (notebook: any) => {
	const { data } = await BeakerAPI.post('/summary', { notebook });
	return data;
};
