<template>
	<Button action @click="goToTheia">
		<IconScript16 />
		New model from code
	</Button>
</template>

<script setup lang="ts">
/**
 * Model Sidebar Panel
 * Display a file tree like structure of models available in the current Project.
 */

import { useRouter } from 'vue-router';
import Button from '@/components/Button.vue';
import IconScript16 from '@carbon/icons-vue/es/script/16';

const router = useRouter();
const goToTheia = () => router.push('/theia');
</script>
