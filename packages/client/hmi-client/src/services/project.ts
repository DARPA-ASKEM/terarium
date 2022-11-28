/**
 * Project
 */

import API from '@/api/api';
import { Project } from '@/types/Project';

/**
 * Get a project per id
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function get(projectId: string): Promise<Project | null> {
	try {
		const response = await API.get(`/projects/${projectId}`);
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		console.error(error);
		return null;
	}
}

export { get };
