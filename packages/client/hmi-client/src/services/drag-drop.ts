import { ref } from 'vue';
/**
 * Use this compoasable to pass data from a dragged object to a destination component that receives the dragged object.
 * This is similar to the behaviour of the {@link https://developer.mozilla.org/en-US/docs/Web/API/DataTransfer DataTransfer} object
 * that is part of the Drag and Drop API. However it has some quirky behaviour and I could not get it to pass data between
 * components.
 */
const data = ref<Map<string, object>>(new Map<string, object>());
export function useDragEvent() {
	function getDragData(key: string) {
		return data.value.get(key);
	}
	function setDragData(key: string, value: object) {
		data.value.set(key, value);
	}
	function deleteDragData(key: string) {
		data.value.delete(key);
	}
	return { getDragData, setDragData, deleteDragData };
}
