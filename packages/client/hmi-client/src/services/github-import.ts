import API from '@/api/api';

export async function getGithubRepositoryContent(repoOwnerAndName: string, path: string) {
	const response = await API.get('/code/repo_content', {
		params: { repoOwnerAndName, path }
	});
	return response?.data ?? null;
}

export async function getGithubCode(repoOwnerAndName: string, path: string) {
	const response = await API.get('/code/repo_file_content', {
		params: { repoOwnerAndName, path }
	});
	return response?.data ?? null;
}
