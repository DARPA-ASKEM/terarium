import { ref } from 'vue';

const data = ref<Map<string, object>>(new Map<string, object>());
export function useDragEvent() {
	function getDragData(key: string) {
		return data.value.get(key);
	}
	function setDragData(key: string, value: object) {
		data.value.set(key, value);
	}
	return { getDragData, setDragData };
}
