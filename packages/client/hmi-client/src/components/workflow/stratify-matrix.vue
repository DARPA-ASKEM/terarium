<template>
	<vue-pivottable-ui
		:data="[
			{ color: 'blue', shape: 'circle' },
			{ color: 'red', shape: 'triangle' }
		]"
		:rows="['color']"
		:cols="['shape']"
	>
	</vue-pivottable-ui>
</template>

<script setup lang="ts">
// import 'jquery/dist/jquery.min.js';
// import $ from 'jquery';

import { VuePivottableUi } from 'vue-pivottable';
// import 'vue-pivottable/dist/vue-pivottable.css'
</script>
