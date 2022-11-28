/**
 * Project
 */

import API from '@/api/api';
import { Project } from '@/types/Project';

/**
 * Get a project per id
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function get(projectId: string): Promise<Project> {
	const response = await API.get(`/projects/${projectId}`);
	return response?.data ?? null;
}

export { get };
