import API from '@/api/api';

export async function getImage(id: string): Promise<string | null> {
	const response = await API.get(`/images/${id}`);
	return response?.data ?? null;
}

export async function getImages(imageIds: string[]) {
	const result: string[] = [];
	const promiseList = [] as Promise<string | null>[];
	imageIds.forEach((imageId) => {
		promiseList.push(getImage(imageId));
	});
	const responsesRaw = await Promise.all(promiseList);
	responsesRaw.forEach((r) => {
		if (r) result.push(r);
	});
	return result;
}

export async function updateImage(id: string, image: string) {
	console.log(id, image);
	const response = await API.put(`/images/${id}`, image, {
		headers: { 'Content-Type': 'application/json' }
	});
	return response?.data ?? null;
}
