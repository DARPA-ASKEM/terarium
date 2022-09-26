<template>
	<div>Home placeholder</div>
	<h1>Test Client App</h1>
	<p><a href="/logout">logout</a></p>

	<h2>User rights required</h2>
	<p>
		<a href="#" @click="uncloak('/api/user/me')">/api/user</a>
		endpoint
		<span id="user-me"></span>
	</p>

	<h2>Admin right required</h2>
	<p>
		<a href="#" @click="uncloak('/api/admin')">/api/admin</a>
		endpoint
		<span id="admin"></span>
	</p>
</template>

<script setup lang="ts">
import { uncloak } from '../utils/uncloak';
</script>
