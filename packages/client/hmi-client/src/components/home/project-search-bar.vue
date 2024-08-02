<template>
	<tera-searchbar
		v-model:queryString="tempQueryString"
		@search="updateQueryString"
		:debounce-duration="500"
		placeholder-text="Search"
	/>
</template>
<script lang="ts" setup>
import useSearch from '@/composables/useSearch';
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import TeraSearchbar from '@/components/home/tera-searchbar.vue';

const { queryString } = useSearch();
const tempQueryString = ref(queryString.value);
const route = useRoute();

const updateQueryString = () => {
	queryString.value = tempQueryString.value;
};

onMounted(() => {
	if (route.query.queryString) {
		tempQueryString.value = route.query.queryString as string;
		updateQueryString();
	}
});
</script>
