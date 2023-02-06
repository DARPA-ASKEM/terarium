<template>
	<div v-if="data" class="metadata-table">
		<div class="row">
			<div class="label">Name</div>
			<div>{{ props.data.name }}</div>
		</div>
		<div class="row">
			<div class="label">ID</div>
			<div>{{ props.data.id }}</div>
		</div>
		<div class="row">
			<div class="label">Type</div>
			<div>{{ props.data.type }}</div>
		</div>
		<div class="row">
			<div class="label">DOI</div>
			<div>{{ props.data.doi }}</div>
		</div>
		<div class="row">
			<div class="label">File</div>
			<div>{{ props.data.file }}</div>
		</div>
		<div class="row">
			<div class="label">Text Annotations</div>
			<div v-for="(text, idx) in props.data.text_annotations" :key="idx">
				{{ text }}
			</div>
		</div>
		<div class="row">
			<div class="label">DKG Annotations</div>
			<div>
				<div class="row" v-for="(dkgAnnoArr, idx) in props.data.dkg_annotations" :key="idx">
					<div>{{ dkgAnnoArr[1] }}</div>
					<div>{{ dkgAnnoArr[0] }}</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="label">Equation Annotations</div>
			<div>
				<div
					class="row"
					v-for="(eqnAnnoKey, idx) in Object.keys(props.data.equation_annotations)"
					:key="idx"
				>
					<div>{{ eqnAnnoKey }}</div>
					<div>
						{{
							props.data.equation_annotations[eqnAnnoKey].length
								? props.data.equation_annotations[eqnAnnoKey][0]
								: 'None'
						}}
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
const props = defineProps<{
	data: any;
}>();
</script>

<style scoped>
.metadata-table {
	display: flex;
	flex-direction: column;
	flex: 1;
	margin: 1em;
}

.row {
	display: flex;
	margin-bottom: 0.5em;
}

.row div {
	flex: 1;
}

.label {
	font-weight: 700;
}
</style>
