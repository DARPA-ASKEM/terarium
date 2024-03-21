import API from '@/api/api';
import { extractionStatusUpdateHandler, subscribe } from '@/services/ClientEventService';
import type { Code, Dataset, DocumentAsset, Model } from '@/types/Types';
import { ClientEventType } from '@/types/Types';
import { logger } from '@/utils/logger';
import { AxiosResponse } from 'axios';

/**
 * Transform a list of LaTeX or mathml strings to an AMR
 * @param format string - the type of equations used to define an AMR ('mathml' or 'latex')
 * @param equations string[] - list of LaTeX or mathml strings representing a model
 * @param framework string= - the framework to use for the extraction, default to 'petrinet'
 * @param modelId string= - the model id to use for the extraction
 * @return {Promise<any>}
 */
export const equationsToAMR = async (
	equations: string[],
	framework: string = 'petrinet',
	modelId?: string
): Promise<string | null> => {
	try {
		const response: AxiosResponse<string> = await API.post(`/knowledge/equations-to-model`, {
			model: framework,
			modelId,
			equations
		});
		return response.data;
	} catch (error: unknown) {
		logger.error(error, { showToast: false });
	}
	return null;
};

/**
 * Given a model, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileModel = async (modelId: Model['id'], documentId: string | null = null) => {
	let response: any = null;
	if (documentId && modelId) {
		response = await API.post(`/knowledge/profile-model/${modelId}?document-id=${documentId}`);
	} else {
		response = await API.post(`/knowledge/profile-model/${modelId}`);
	}
	console.log('model profile response', response.data);
	return response.data.id;
};

export const alignModel = async (
	modelId: Model['id'],
	documentId: string
): Promise<Model | null> => {
	const response = await API.post(
		`/knowledge/link-amr?document-id=${documentId}&model-id=${modelId}`
	);
	return response.data ?? null;
};

/**
 * Given a dataset, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileDataset = async (
	datasetId: Dataset['id'],
	documentId: string | null = null
) => {
	let response: any = null;
	if (documentId && datasetId) {
		response = await API.post(
			`/knowledge/profile-dataset/${datasetId}?document-id=${documentId}`
		);
	} else {
		response = await API.post(`/knowledge/profile-dataset/${datasetId}`);
	}
	console.log('data profile response', response.data);
	return response.data.id;
};

/** Extract text and artifacts from a PDF document */
export const extractPDF = async (documentId: DocumentAsset['id']) => {
	console.group('PDF COSMOS Extraction');
	if (documentId) {
		const response = await API.post(`/knowledge/pdf-extractions?document-id=${documentId}`);
		if (response?.status === 202) {
			await subscribe(ClientEventType.Extraction, extractionStatusUpdateHandler);
		} else {
			console.debug('Failed — ', response);
		}
	} else {
		console.debug('Failed — No documentId provided for pdf extraction.');
	}
	console.groupEnd();
};

/** Extract variables from a text document */
export const extractVariables = async (
	documentId: DocumentAsset['id'],
	modelIds: Array<Model['id']>
) => {
	console.group('SKEMA Variable extraction');
	if (documentId) {
		await API.post(
			`/knowledge/variable-extractions?document-id=${documentId}&model-ids=${modelIds}`
		);
	} else {
		console.debug('Failed — No documentId provided for variable extraction.');
	}
	console.groupEnd();
};

export async function codeToAMR(
	codeId: string,
	name: string = '',
	description: string = '',
	dynamicsOnly: boolean = false,
	llmAssisted: boolean = false
): Promise<Model | null> {
	const response = await API.post(
		`/knowledge/code-to-amr?code-id=${codeId}&name=${name}&description=${description}&dynamics-only=${dynamicsOnly}&llm-assisted=${llmAssisted}`
	);
	if (response?.status === 200) {
		return response.data;
	}
	logger.error(`Code to AMR request failed`, { toastTitle: 'Error - knowledge-middleware' });
	return null;
}

export async function codeBlocksToAmr(code: Code, file: File): Promise<Model | null> {
	const formData = new FormData();
	const blob = new Blob([JSON.stringify(code)], {
		type: 'application/json'
	});
	formData.append('code', blob);
	formData.append('file', file);
	const response = await API.post(`/knowledge/code-blocks-to-model`, formData, {
		headers: {
			Accept: 'application/json',
			'Content-Type': 'multipart/form-data'
		}
	});
	if (response?.status === 200) {
		return response.data;
	}
	logger.error(`Code to AMR request failed`, { toastTitle: 'Error - knowledge-middleware' });
	return null;
}
