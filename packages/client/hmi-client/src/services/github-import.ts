import API from '@/api/api';
import { logger } from '@/utils/logger';
import type { GithubRepo } from '@/types/Types';
import { EventType } from '@/types/Types';
import * as EventService from '@/services/event';
import { useProjects } from '@/composables/project';

export async function getGithubRepositoryContent(repoOwnerAndName: string, path: string): Promise<GithubRepo> {
	try {
		EventService.create(
			EventType.GithubImport,
			useProjects().activeProject.value?.id,
			JSON.stringify({ repoOwnerAndName, path })
		);

		const response = await API.get('/code/repo-content', {
			params: { 'repo-owner-and-name': repoOwnerAndName, path }
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
			params: { 'repo-owner-and-name': repoOwnerAndName, path }
		});
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function getGithubRepo(repoOwnerAndName: string) {
	try {
		const response = await API.get('/code/repo-zip', {
			params: { 'repo-owner-and-name': repoOwnerAndName }
		});
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
