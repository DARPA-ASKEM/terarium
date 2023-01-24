<template>
	<section class="unauthorized">
		<h2>Unauthorized</h2>
		<p>
			You have been authenticated but not authorized to use this application, please contact
			Uncharted to be granted access. Once you have been authroized please
			<a href="#" @click="auth.logout">Sign-in</a> again.
		</p>
	</section>
</template>

<script setup lang="ts">
import useAuthStore from '@/stores/auth';

const auth = useAuthStore();
</script>

<style>
.unauthorized {
	padding: 2rem;
}
</style>
