<template>
	<div class="tera-chatty-container" ref="containerElement">
		<!-- <label for="chatt-input"></label> -->
		<div class="chat-input-container">
			<span class="p-input-icon-right">
				<i class="pi pi-spin pi-spinner" v-if="props.kernelIsBusy" />
				<i class="pi pi-send" v-else />
				<InputText
					:style="{ width: fixedDivWidth + 'px' }"
					class="input"
					ref="inputElement"
					v-model="queryString"
					type="text"
					:disabled="props.kernelIsBusy"
					:placeholder="props.kernelIsBusy ? 'Please wait...' : 'What do you want to do?'"
					@keydown.enter="emit('submit-query', queryString)"
				></InputText>
			</span>
			<ProgressBar v-if="props.kernelIsBusy" mode="indeterminate" style="height: 3px"></ProgressBar>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue';
import ProgressBar from 'primevue/progressbar';
import InputText from 'primevue/inputtext';

const emit = defineEmits(['submit-query']);

const props = defineProps({
	placeholderMessage: {
		type: String,
		required: false,
		default: () => ''
	},
	focusInput: {
		type: Boolean,
		default: false
	},
	styleResults: {
		type: Boolean,
		default: false
	},
	placeholderColor: {
		type: String,
		default: 'black'
	},
	kernelIsBusy: Boolean
});

const queryString = ref('');

const containerElement = ref<HTMLElement | null>(null);
const inputElement = ref<HTMLInputElement | null>(null);
const fixedDivWidth = ref(0);

onMounted(() => {
	if (props.focusInput) {
		inputElement.value?.focus();
	}

	const updateWidth = () => {
		if (containerElement.value) {
			fixedDivWidth.value =
				containerElement.value.offsetWidth - 20
					? containerElement.value?.offsetWidth
					: fixedDivWidth.value;
		}
	};

	updateWidth(); // Update once on mount
	window.addEventListener('resize', updateWidth); // Update on window resize

	onBeforeUnmount(() => {
		window.removeEventListener('resize', updateWidth); // Clean up listener
	});
});
</script>

<style scoped>
::placeholder {
	color: var(--gray-700);
}
.tera-chatty-container {
	display: relative;
	padding-top: 5px;
	padding-bottom: 35px;
}

.chat-input-container {
	position: fixed;
	bottom: 50px;
	height: fit-content;
	z-index: 30000;
}

.input {
	color: black;
	background-color: var(--gray-300);
}

.input:disabled {
	color: black;
	opacity: 100;
	background-color: var(--gray-300);
}
</style>
