<script setup lang="ts">
// import Button from '@/components/Button.vue';
import IconList24 from '@carbon/icons-vue/es/list/24';
import IconAddFilled32 from '@carbon/icons-vue/es/add--filled/32';

interface Props {
	category: String;
	projectName: String;
	isNewProjectCard: Boolean;
	p: Number;
}

// Not sure how to get rid of these underlines - I am following these rules: https://vuejs.org/api/sfc-script-setup.html#typescript-only-features
const props = withDefaults(defineProps<Props>(), {
	category: '',
	projectName: '',
	isNewProjectCard: false,
	p: 0
});

function createNewProject() {}
</script>

<template>
	<div v-if="!props.isNewProjectCard" class="project-card">
		<div class="details">
			<div class="name">{{ props?.projectName }} {{ props?.p }}</div>
			<div class="actions">
				<IconList24 />
			</div>
		</div>
	</div>
	<div v-else class="new-project-card">
		<IconAddFilled32 class="icon-add-filled" @onclick="createNewProject" />
		<div class="details">
			<div class="create-new-project">Create New Project</div>
		</div>
	</div>
	<!-- <div v-else class="new-project-card">
        <Button @onclick="createNewProject">Create New Project
            <IconAddFilled32 />
        </Button>
    </div> -->
</template>

<style scoped>
.project-card,
.new-project-card {
	color: var(--un-color-black-80);
	border: 1px solid var(--un-color-black-20);
	background-color: var(--un-color-black-5);
	font-weight: 500;
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	height: 15rem;
	min-width: 20rem;
	border-radius: 0.5rem;
	margin: 0.5rem;
	transition: 0.2s;
	text-align: left;
}

.project-card:hover {
	height: 20rem;
	min-width: 25rem;
	cursor: pointer;
	/* position: fixed; */
}

/** */
/* .new-project-card button {
    font-size: 1.25rem;
    font-weight: 500;
    border-radius: 1.5rem;
    width: 85%;
    margin: auto;
    justify-content: center;
    cursor: pointer;
    background-color: var(--un-color-black-5);
    color: var(--un-color-black-50);
    box-shadow: none;
}

.new-project-card button:hover {
    background-color: var(--un-color-black-30);
    color: var(--un-color-black-5);
}

.new-project-card button svg {
    color: var(--un-color-black-40);
}

.new-project-card button:hover svg {
    color: var(--un-color-black-5);
} */

/** */

.details {
	border-top: 1px solid var(--un-color-black-20);
	/* background-color: var(--un-color-black-10); 
    border-radius: 0 0 0.5rem 0.5rem;*/
}

.details div {
	padding: 0.5rem 1rem;
}

.project-card:hover .details .actions {
	display: block;
}

.project-card .details .actions {
	display: none;

	padding: 0 0 0.25rem 1rem;
}

.details .create-new-project {
	text-align: center;
}

svg {
	color: var(--un-color-black-40);
	cursor: pointer;
}

.new-project-card .icon-add-filled {
	margin: auto;
	transform: scale(3);
}

svg:hover {
	color: var(--un-color-black-30);
}
</style>
