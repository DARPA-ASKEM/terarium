<template>
	<Teleport to="body">
		<tera-modal
			v-if="isVisible"
			class="save-as-dialog"
			@modal-mask-clicked="emit('close-modal')"
			@modal-enter-press="save"
		>
			<template #header>
				<h4>{{ title }}</h4>
			</template>
			<template #default>
				<form @submit.prevent>
					<label for="new-name">What would you like to call it?</label>
					<InputText
						id="new-name"
						type="text"
						v-model="newName"
						placeholder="Enter a unique name"
					/>
					<slot name="extra-input-fields" />
				</form>
			</template>
			<template #footer>
				<Button label="Save" size="large" @click="save" />
				<Button label="Cancel" class="p-button-secondary" size="large" outlined @click="cancel" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import TeraModal from '@/components/widgets/tera-modal.vue';

const props = defineProps({
	title: {
		type: String,
		default: 'Save as a new asset'
	},
	initialName: {
		type: String,
		default: ''
	},
	isVisible: {
		type: Boolean,
		default: false
	},
	openOnSave: {
		type: Boolean,
		default: false
	}
});

const emit = defineEmits(['save', 'close-modal']);

// TODO: Consider letting the user just know if what they are typing currently is a duplicate (do not prevent them from saving)
// const isValidName = ref<boolean>(true);
// const invalidInputStyle = computed(() => (!isValidName.value ? 'p-invalid' : ''));	v-bind:class="invalidInputStyle"

const newName = ref<string>(props.initialName);

function save() {
	emit('save', newName.value.trim());
}

function cancel() {
	newName.value = '';
	emit('close-modal');
}
</script>

<style scoped>
.save-as-dialog:deep(section) {
	width: 40rem;
}

form {
	margin-top: var(--gap);
}
</style>
