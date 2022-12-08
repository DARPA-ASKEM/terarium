<template>
	<div class="switcher">
		<span v-if="label.length > 0" class="title"> {{ label }} </span>
		<label>
			<input v-model="checkboxModel" :name="name" type="checkbox" @change="handleToggle" />
			<span><small /></span>
		</label>
	</div>
</template>

<script setup lang="ts">
import { ref, toRefs, watch } from 'vue';
import { uniqueId } from 'lodash';

const props = defineProps({
	value: {
		type: Boolean,
		default: true
	},
	name: {
		type: String,
		default: uniqueId()
	},
	label: {
		type: String,
		default: ''
	}
});

const checkboxModel = ref(props.value);
const { value } = toRefs(props);

const emit = defineEmits(['change']);

watch(value, () => {
	checkboxModel.value = props.value;
});

const handleToggle = () => {
	emit('change', checkboxModel.value);
};
</script>

<style scoped>
.switcher {
	display: flex;
	align-items: center;
}

.title {
	padding: 5px;
	text-align: center;
	color: white;
}

label {
	display: flex;
	padding: 0;
	margin: 0;
	cursor: pointer;
	font-weight: normal;
}

label * {
	align-self: middle;
}

input {
	display: none;
}

input + span {
	--toggle-size: 40px;
	content: '';
	position: relative;
	display: inline-block;
	width: var(--toggle-size);
	height: calc(var(--toggle-size) / 2);
	background: var(--label-color);
	border: 1px solid var(--label-color);
	border-radius: 10px;
	transition: all 0.3s ease-in-out;
}

input + span small {
	position: absolute;
	display: block;
	width: 50%;
	height: 100%;
	background: #ffffff;
	border-radius: 50%;
	transition: all 0.3s ease-in-out;
	left: 0;
}

input:checked + span {
	--toggle-on: #4e4e54;
	background: var(--toggle-on);
	border-color: var(--toggle-on);
}

input:checked + span small {
	left: 50%;
	background: #ffffff;
}

input + span::after {
	content: 'Off';
	color: #ffffff;
	position: absolute;
	right: 1px;
	font-size: 12px;
	height: 12px;
}

input:checked + span::after {
	content: 'On';
	left: 1px;
	font-size: 12px;
}
</style>
