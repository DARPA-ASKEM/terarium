import { ref } from 'vue';
import { ResultType, SearchByExampleOptions } from '@/types/common';

const searchByExampleOptions = ref<SearchByExampleOptions>({
	similarContent: false,
	forwardCitation: false,
	backwardCitation: false,
	relatedContent: false
});
const searchByExampleItem = ref<ResultType | null>(null);
export function useSearchByExampleOptions() {
	return { searchByExampleOptions, searchByExampleItem };
}

export function extractResourceName(resource): string {
	return resource.name ?? resource.title;
}

const optionBoolsToStrs = {
	similarContent: 'similar content',
	forwardCitation: 'forward citations',
	backwardCitation: 'backward citations',
	relatedContent: 'related resources'
};
export function getSearchByExampleOptionsString(): string {
	const result: string[] = [];
	Object.entries(searchByExampleOptions.value).forEach(([key, value]) => {
		if (value) {
			result.push(optionBoolsToStrs[key]);
		}
	});

	return result.join(', ');
}
