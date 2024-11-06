<template>
	<tera-grey-card class="details">
		<ul>
			<li class="multiple">
				<span>
					<label>Framework</label>
					<div class="framework">{{ model?.header?.schema_name }}</div>
				</span>
				<span>
					<label>Model version</label>
					<div>{{ model?.header?.model_version }}</div>
				</span>
				<span>
					<label>Date created</label>
					<div>{{ model?.metadata?.processed_at ?? card?.date }}</div>
				</span>
			</li>
			<li>
				<label>Created by</label>
				<div><tera-show-more-text v-if="authors" :text="authors" :lines="2" /></div>
			</li>
			<li>
				<label>Author email</label>
				<div>{{ card?.authorEmail }}</div>
			</li>
			<li>
				<label>Institution</label>
				<div>
					<tera-show-more-text v-if="card?.authorInst" :text="card?.authorInst" :lines="2" />
				</div>
			</li>
			<li class="multiple">
				<span>
					<label>License</label>
					<div>{{ card?.license }}</div>
				</span>
				<span>
					<label>Complexity</label>
					<div>{{ card?.complexity }}</div>
				</span>
			</li>
			<li>
				<label>Source</label>
				<div>{{ model?.metadata?.processed_by }}</div>
			</li>
		</ul>
	</tera-grey-card>
</template>

<script setup lang="ts">
import TeraGreyCard from '@/components/widgets/tera-grey-card.vue';
import { Model } from '@/types/Types';
import { computed } from 'vue';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';

const props = defineProps<{
	model: Model;
}>();

const card = computed(() => {
	if (props.model.metadata?.card) {
		const cardWithUnknowns = props.model.metadata?.card;
		const cardWithUnknownsArr = Object.entries(cardWithUnknowns);

		for (let i = 0; i < cardWithUnknownsArr.length; i++) {
			const key = cardWithUnknownsArr[i][0];
			if (cardWithUnknowns[key] === 'UNKNOWN') {
				cardWithUnknowns[key] = null;
			}
		}
		return cardWithUnknowns;
	}
	return null;
});

const authors = computed(() => {
	const authorsArray = props.model?.metadata?.annotations?.authors?.map((author) => author.name) ?? [];

	if (card.value?.authorAuthor) authorsArray.unshift(card.value?.authorAuthor);

	return authorsArray.join(', ');
});
</script>

<style scoped>
.details {
	> ul {
		list-style: none;
		padding: 0.5rem 1rem;
		display: flex;
		flex-direction: column;
		gap: var(--gap-2);

		& > li.multiple {
			display: flex;

			& > span {
				flex: 1 0 0;
			}
		}

		& > li label {
			color: var(--text-color-subdued);
			font-size: var(--font-caption);

			& + *:empty:before {
				content: '--';
			}
		}
	}
}
</style>
