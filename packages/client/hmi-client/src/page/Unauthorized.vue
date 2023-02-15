<template>
	<section class="unauthorized">
		<h4>Unauthorized</h4>
		<ol>
			<li>You have been authenticated but not authorized to use this application.</li>
			<li>
				Contact <a href="https://askemgroup.slack.com/team/U03JQSSHZ45">Uncharted</a> to be granted
				access.
			</li>
			<li>
				Once you have been authroized please <a href="#" @click="auth.logout">Sign-in</a> again.
			</li>
		</ol>
	</section>
</template>

<script setup lang="ts">
import useAuthStore from '@/stores/auth';

const auth = useAuthStore();
</script>

<style>
.unauthorized {
	align-items: center;
	display: flex;
	flex-direction: column;
	justify-content: center;
	padding: 2rem;

	width: 100%;
}

.unauthorized ol {
	margin-top: 2em;
	list-style-position: inside;
	list-style-type: decimal;
}

.unauthorized li {
	margin-bottom: 1em;
}
</style>
