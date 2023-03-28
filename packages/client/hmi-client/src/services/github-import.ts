import { logger } from '@/utils/logger';

// A header might be necessary depending on the request frequency
// const header = {
// 	Authorization: `Bearer ${import.meta.env.VITE_CR_PAT}`
// };
const requestOptions = {
	method: 'GET'
	// header
};

// Might be useful if we want to display some other information about the repository
export async function getGithubRepositoryAttributes(repositoryName: string) {
	try {
		const response = await fetch(`https://api.github.com/repos/${repositoryName}`, requestOptions);
		const result = await response.json();
		if (!response.ok) return null;
		return result;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function getGithubRepositoryContent(repositoryName: string, path: string) {
	try {
		const response = await fetch(
			`https://api.github.com/repos/${repositoryName}/contents/${path}`,
			requestOptions
		);
		const result = await response.json();
		if (!response.ok) return null;
		return result;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function getGithubCode(repositoryName: string, path: string) {
	try {
		const response = await fetch(
			`https://cdn.jsdelivr.net/gh/${repositoryName}@latest/${path}`,
			requestOptions
		);
		const result = await response.text();
		if (!response.ok) return null;
		return result;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
