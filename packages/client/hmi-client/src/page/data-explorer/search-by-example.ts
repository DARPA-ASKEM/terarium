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
