<template>
	<section class="drag-n-drop">
		<div class="dropzone-container" @drop="onDrop" @dragover="onDragOver" @dragleave="onDragLeave">
			<input
				id="fileInput"
				type="file"
				ref="fileInput"
				@change="onFileChange"
				multiple
				class="hidden-input"
				accept=".pdf"
			/>
			<label for="fileInput" class="file-label">
				<div v-if="dragOver">Release mouse button to add files to import</div>
				<div v-else>To import PDFs drag and drop them here or <u>click here</u>.</div>
			</label>
			<br />

			<div v-if="files.length > 0">
				<div class="preview-container mt-4" v-if="files.length">
					<div v-for="file in files" :key="file.name" class="file-preview" scrolling="no">
						<importPreview
							:file="file"
							:show-preview="props.showPreview"
							:is-processing="isProcessing"
							@remove-file="removeFile(files.indexOf(file))"
						></importPreview>
					</div>
				</div>
				<br />
				<div class="card flex justify-content-center">
					<Dropdown
						v-model="extractionMode"
						:options="modes"
						optionLabel="name"
						placeholder="Extraction Mode"
						class="w-full md:w-14rem"
					/>
				</div>
			</div>
			<br />
			<Button
				v-if="canImport"
				type="button"
				class="import-button"
				@click="processFiles(files)"
				label="Import Data"
			></Button>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref, defineProps, computed } from 'vue';
import API from '@/api/api';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { AcceptedTypes } from '@/types/common';
import importPreview from './file-preview.vue';

const emit = defineEmits(['import-completed']);
const files = ref(<File[]>[]);
const fileInput = ref(<HTMLInputElement | null>null);
const dragOver = ref(false);
const isProcessing = ref(false);
const processResponse = ref(<{ file: string; response: { text: string } }[]>[]);

const extractionMode = ref({ name: 'pymupdf' });
const modes = ref([{ name: 'pypdf2' }, { name: 'pdfminer' }, { name: 'pymupdf' }]);

const props = defineProps({
	// show preview
	showPreview: {
		type: Boolean,
		required: true,
		default: true
	},
	// list of accepted types of files
	acceptTypes: {
		type: Array<AcceptedTypes>,
		required: true,
		default: ['application/pdf'],
		validator: (value: Array<string>) =>
			[AcceptedTypes.JPEG, AcceptedTypes.JPG, AcceptedTypes.PNG, AcceptedTypes.PDF].every((v) =>
				value.includes(v)
			)
	},
	// custom import action can be passed in as prop
	importAction: {
		type: Function,
		required: false,
		/**
		 * Default import action which just logs the click event.
		 * @param {Array<File>} currentFiles
		 * @returns {any}
		 */
		default: async (currentFiles: Array<File>): Promise<{ file: string; response: string }[]> => {
			try {
				const result: string[] = [];
				for (let i = 0; i < currentFiles.length; i++) {
					result.push(currentFiles[i].name);
				}
				const resp = await API.post(`/logs/`, {
					logs: [{ level: 'info', message: `Sending file(s) for Importing: ${result}` }]
				});
				const { status } = resp;
				if (status !== 200) console.warn('POST to /logs did not return a 200');
			} catch (error) {
				console.error(error);
			}
			return [{ file: 'Default Import Action', response: 'Nothing' }];
		}
	}
});

/**
 * Add file event
 * @param {File[]|undefined} addedFiles
 * @returns {any}
 */
const addFiles = (addedFiles: File[] | undefined) => {
	if (addedFiles !== undefined && addedFiles.length > 0) {
		for (let i = 0; i < addedFiles.length; i++) {
			// only add files that weren't added before
			if (props.acceptTypes.indexOf(addedFiles[i].type as AcceptedTypes) > -1) {
				const index = files.value.findIndex((item) => item.name === addedFiles[i].name);
				if (index === -1) {
					files.value.push(addedFiles[i]);
				} else {
					console.log(`${addedFiles[i].name} File already added!`);
				}
			}
		}
	}
};

/**
 * File selection event
 * @returns {any}
 */
const onFileChange = () => {
	if (fileInput.value !== null) {
		addFiles(fileInput.value ? Array.from(fileInput.value.files as FileList) : undefined);
	}
};

/**
 * On file drop event
 * @param {DragEvent} event
 * @returns {any}
 */
const onDrop = (event: DragEvent) => {
	event.preventDefault();
	dragOver.value = false;
	const addedFiles = event.dataTransfer?.files ? Array.from(event.dataTransfer?.files) : undefined;
	addFiles(addedFiles);
};

/**
 * File drag over event
 * @param {DragEvent} event
 * @returns {any}
 */
const onDragOver = (event: DragEvent) => {
	event.preventDefault();
	dragOver.value = true;
};

/**
 * File drag leave event
 * @param {DragEvent} event
 * @returns {any}
 */
const onDragLeave = (event: DragEvent) => {
	event.preventDefault();
	dragOver.value = false;
};

/**
 * remove file from list
 * @param {number} index
 * @returns {any}
 */
const removeFile = (index: number) => {
	files.value.splice(index, 1);
};

async function processFiles(f) {
	isProcessing.value = true;
	processResponse.value = await props.importAction(f, extractionMode.value.name, true);
	console.log(processResponse.value);
	isProcessing.value = false;
	emit('import-completed', processResponse.value);
	files.value = [];
}

const canImport = computed(() => files.value.length > 0);
</script>

<style scoped>
.drag-n-drop {
	/* flex: 0.5; */
	width: 100%;
	overflow-y: auto;
	height: 100%;
	overflow-x: hidden;
}
.dropzone-container {
	flex-direction: column;
	display: flex;
	padding: 15px;
	border: 3px dashed #e2e8f0;
}

.hidden-input {
	opacity: 0;
	overflow: hidden;
	position: absolute;
	width: 1px;
	height: 1px;
}

.file-label {
	font-size: 20px;
	display: flex;
	flex-direction: column;
	cursor: pointer;
}

.preview-container {
	display: flex;
	flex-direction: column;
	margin-top: 2rem;
	border-radius: 0.25rem;
	cursor: pointer;
	border: 1px solid var(--surface-border);
}

.file-preview {
	display: flex;
	flex-direction: column;
	border: 1px solid #a2a2a2;
	padding: 5px;
	margin: 5px;
	/* overflow: hidden !important; */
}

.import-button {
	display: flex;
	flex-direction: column;
	margin-top: 2rem;
	border-radius: 0.25rem;
	cursor: pointer;
	border: 1px solid var(--surface-border);
}
</style>
