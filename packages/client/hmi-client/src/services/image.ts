import API from '@/api/api';

export async function getImage(id: string): Promise<string | null> {
	const response = await API.get(`/images/${id}`);
	return response?.data ?? null;
}

export async function getImages(ids: string[]) {
	const result: string[] = [];
	const promiseList = [] as Promise<string | null>[];
	ids.forEach((id) => {
		promiseList.push(getImage(id));
	});
	const responsesRaw = await Promise.all(promiseList);
	responsesRaw.forEach((r) => {
		if (r) result.push(r);
	});
	return result;
}

export async function addImage(id: string, image: string) {
	const response = await API.put(`/images/${id}`, image, {
		headers: { 'Content-Type': 'application/json' }
	});
	return response?.data ?? null;
}

export async function deleteImage(id: string) {
	const response = await API.delete(`/images/${id}`);
	return response?.data ?? null;
}

export async function deleteImages(ids: string[]) {
	const result: string[] = [];
	const promiseList = [] as Promise<string | null>[];
	ids.forEach((id) => {
		promiseList.push(deleteImage(id));
	});
	const responsesRaw = await Promise.all(promiseList);
	responsesRaw.forEach((r) => {
		if (r) result.push(r);
	});
	return result;
}
