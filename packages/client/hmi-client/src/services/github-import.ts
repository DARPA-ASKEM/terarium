import { logger } from '@/utils/logger';

// A header might be necessary depending on the request frequency
// const header = {
// 	Authorization: `Bearer ${import.meta.env.VITE_CR_PAT}`
// };
const requestOptions = {
	method: 'GET'
	// header
};

export async function getGithubRepositoryAttributes(ownerRepo: string) {
	try {
		const response = await fetch(`https://api.github.com/repos/${ownerRepo}`, requestOptions);
		const result = await response.json();
		if (!response.ok) return null;
		return result;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function getGithubRepositoryContent(contentsUrl: string) {
	try {
		const response = await fetch(contentsUrl, requestOptions);
		const result = await response.json();
		if (!response.ok) return null;
		return result;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function getGithubCode(downloadUrl: string) {
	try {
		const jsdelivrUrl = downloadUrl
			.replace('https://raw.githubusercontent.com', 'https://cdn.jsdelivr.net/gh') // Switch to jsdeliver since it's a proper CDN
			.replace('/master', '@latest')
			.replace('/main', '@latest'); // Get latest version
		const response = await fetch(jsdelivrUrl, requestOptions); // Grab the chosen file
		const result = await response.text(); // Once a file is chosen from a repository copy its contents into the editor
		if (!response.ok) return null;
		return result;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
