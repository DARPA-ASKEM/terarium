import API from '@/api/api';
import { logger } from '@/utils/logger';
import { GithubRepo } from '@/types/Types';

export async function getGithubRepositoryContent(
	repoOwnerAndName: string,
	path: string
): Promise<GithubRepo> {
	try {
		const response = await API.get('/code/repo-content', {
			params: { repoOwnerAndName, path }
		});
		const { status, data } = response;
		if (status !== 200) return { files: {} } as GithubRepo;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return { files: {} } as GithubRepo;
	}
}

export async function getGithubCode(repoOwnerAndName: string, path: string) {
	try {
		const response = await API.get('/code/repo-file-content', {
			params: { repoOwnerAndName, path }
		});
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
