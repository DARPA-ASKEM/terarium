<template>
	<Button action @click="logout"
		>Logout
		<IconLogout16 />
	</Button>
</template>

<script setup lang="ts">
/**
 * Profile Sidebar Panel
 * Display option specific to the user.
 */

import Button from '@/components/Button.vue';
import IconLogout16 from '@carbon/icons-vue/es/logout/16';
import useAuthStore from '@/stores/auth';

const auth = useAuthStore();

const logout = () => auth.logout();
</script>
