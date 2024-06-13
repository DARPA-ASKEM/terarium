<template>
	<section style="padding: 2rem; display: flex; flex-direction: column">
		<button style="width: 200px" @click="show = !show">Toggle</button>
		<div v-if="show">
			<inner :id="'Inner 01'" />

			<wrapper>
				<div name="abc">
					<inner :id="'inside - 01'" />
				</div>
				<div name="def">
					<inner :id="'inside - 02'" />
				</div>
			</wrapper>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Wrapper from './wrapper.vue';
import Inner from './inner.vue';

const show = ref(true);
</script>
