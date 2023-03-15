// A header might be necessary depending on the request frequency
// const header = {
// 	Authorization: `Bearer ${import.meta.env.VITE_CR_PAT}`
// };
const requestOptions = {
	method: 'GET'
	// header
};

export async function getGithubUrls(query: string = 'SIR model') {
	// Search for potential repositories
	const response = await fetch(
		`https://api.github.com/search/repositories?q=${query}`, // {&page,per_page,sort,order}
		requestOptions
	);

	const result = await response.json();
	return result.items.slice(0, 3); // Just show 3 for the prototype
}

export async function getGithubRepositoryContent(contentsUrl: string) {
	const response = await fetch(contentsUrl, requestOptions);

	const result = await response.json();

	return result;
}

export async function getGithubCode(downloadUrl: string) {
	const jsdelivrUrl = downloadUrl
		.replace('https://raw.githubusercontent.com', 'https://cdn.jsdelivr.net/gh') // Switch to jsdeliver since it's a proper CDN
		.replace('/master', '@latest')
		.replace('/main', '@latest'); // Get latest version

	// Grab the chosen file
	const response = await fetch(jsdelivrUrl, requestOptions);

	const result = await response.text(); // Once a file is chosen from a repository copy its contents into the editor
	return result;
}
