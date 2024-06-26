<template>
	<section class="drag-n-drop">
		<div
			:class="dragOver != true ? 'dropzone-container' : 'dropzone-container-dragOver'"
			@drop="onDrop"
			@dragover="onDragOver"
			@dragleave="onDragLeave"
		>
			<input
				id="fileInput"
				type="file"
				ref="fileInput"
				@change="onFileChange"
				multiple
				:accept="fileInputAcceptedExtensions"
				class="hidden-input"
			/>
			<label for="fileInput" class="file-label">
				<div v-if="dragOver" class="flex row align-items-center gap-3">
					<div><i class="pi pi-file" style="font-size: 2.5rem" /></div>
					<div>Release mouse button to add files to import</div>
				</div>
				<div v-else class="drop-zone">
					<div><i class="pi pi-upload" style="font-size: 2.5rem" /></div>
					<div>
						Drop resources here <br />
						or <span class="text-link">click to open a file browser</span>
					</div>
				</div>
			</label>
		</div>
		<div v-if="importFiles.length > 0">
			<div class="preview-container mt-4" v-if="importFiles.length">
				<div v-for="file in importFiles" :key="file.name" class="file-preview" scrolling="no">
					<TeraDragAndDropFilePreviewer
						:file="file"
						:is-processing="isProcessing"
						:progress="props.progress"
						@remove-file="removeFile(importFiles.indexOf(file))"
					>
					</TeraDragAndDropFilePreviewer>
				</div>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import API from '@/api/api';
import { AcceptedExtensions, AcceptedTypes } from '@/types/common';
import TeraDragAndDropFilePreviewer from './tera-drag-n-drop-file-previewer.vue';

const emit = defineEmits(['import-completed', 'imported-files-updated']);
const importFiles = ref(<File[]>[]);
const fileInput = ref(<HTMLInputElement | null>null);
const dragOver = ref(false);
const isProcessing = ref(false);
const processResponse = ref(<{ file: string; response: { text: string } }[]>[]);

const csvDescription = ref('');

const props = defineProps({
	progress: {
		type: Number,
		default: undefined
	},
	// list of accepted types of files
	acceptTypes: {
		type: Array<AcceptedTypes>,
		required: true,
		validator: (value: Array<string>) =>
			Object.values(AcceptedTypes).every((v) => value.includes(v))
	},
	acceptExtensions: {
		type: Array<AcceptedExtensions>,
		required: true,
		validator: (value: Array<string>) =>
			Object.values(AcceptedExtensions).every((v) => value.includes(v))
	},
	// custom import action can be passed in as prop
	importAction: {
		type: Function,
		required: true,
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

const fileInputAcceptedExtensions = computed(() =>
	props.acceptExtensions.map((v) => `.${v}`).join(',')
);

/**
 * Add file event
 * @param {File[]|undefined} addedFiles
 * @returns {any}
 */
const addFiles = (addedFiles: File[] | undefined) => {
	if (addedFiles !== undefined && addedFiles.length > 0) {
		for (let i = 0; i < addedFiles.length; i++) {
			// if we have a file type specified, check to see if it is an accepted type
			// if we do not have a file type specified, check to see if the name ends with an accepted extension
			const addedFile = addedFiles[i];
			if (
				props.acceptTypes.includes(addedFile.type as AcceptedTypes) ||
				props.acceptExtensions.includes(addedFile.name.split('.').pop() as AcceptedExtensions)
			) {
				// only add files that weren't added before
				const index = importFiles.value.findIndex((item) => item.name === addedFile.name);
				if (index === -1) {
					importFiles.value.push(addedFile);
				} else {
					console.log(`${addedFile.name} File already added!`);
				}
			}
		}
	}
	processFiles(importFiles.value);
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
	importFiles.value.splice(index, 1);
};

async function processFiles(files) {
	isProcessing.value = true;
	const r = await props.importAction(files, csvDescription.value);
	processResponse.value = await Promise.all(r);
	isProcessing.value = false;
	emit('import-completed', processResponse.value);
	files.value = [];
}

watch(
	() => importFiles.value.length,
	() => {
		emit('imported-files-updated', importFiles.value);
	}
);
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
	padding: 1rem;
	border: 1px dashed var(--surface-border);
	border-radius: var(--border-radius);
	background-color: var(--surface-secondary);
}

.dropzone-container-dragOver {
	flex-direction: column;
	display: flex;
	padding: 1rem;
	border: 1px solid var(--primary-color);
	border-radius: var(--border-radius);
	background-color: var(--surface-highlight);
}

.hidden-input {
	opacity: 0;
	overflow: hidden;
	position: absolute;
	width: 1px;
	height: 1px;
}

.file-label {
	font-size: var(--font-body-small);
	flex-direction: column;
	cursor: pointer;
	align-items: center;
	padding: 2.5rem 0 2.5rem 0;
}
label.file-label {
	display: flex;
}

.text-link {
	color: var(--primary-color);
}

.options-container {
	display: flex;
	flex-direction: row;
}

.extraction-mode {
	margin-right: 10px;
}

.preview-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.file-preview {
	display: flex;
	flex-direction: column;
}

.import-button {
	display: flex;
	flex-direction: column;
	margin-top: 2rem;
	border-radius: 0.25rem;
	cursor: pointer;
	border: 1px solid var(--surface-border);
}

.drop-zone {
	display: flex;
	gap: 1rem;
	align-items: center;
}

i {
	color: var(--text-color-secondary);
}
</style>
