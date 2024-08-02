import { ref } from 'vue';

const queryString = ref('');

export default function useSearch() {
	return {
		queryString
	};
}
