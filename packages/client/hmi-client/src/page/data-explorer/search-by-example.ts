import { ref } from 'vue';
import { SearchByExampleOptions } from '@/types/common';

const searchByExampleOptions = ref<SearchByExampleOptions>({
	similarContent: false,
	forwardCitation: false,
	backwardCitation: false,
	relatedContent: false
});
export function useSearchByExampleOptions() {
	return { searchByExampleOptions };
}
