<template>
	<div class="container" v-bind:style="additionalContainerCss">
		<!-- eslint-disable -->
		<div
			ref="detail"
			v-bind:class="{ default: !expanded, expanded: expanded }"
			v-bind:style="`--lines: ${lines}`"
		>
			<div v-bind:style="additionalCss" v-html="text"></div>
		</div>
		<!-- eslint-enable -->
		<a
			v-if="hasMore && triggerShowMore"
			class="anchor"
			:style="additionalAnchorCss"
			@click="onClick"
			>{{ moreText }}</a
		>
		<a v-if="hasMore && expanded" class="anchor" :style="additionalAnchorCss" @click="onClick">{{
			lessText
		}}</a>
	</div>
</template>

<script lang="ts">
export default {
	name: 'VueShowMoreText',
	props: {
		lines: {
			type: Number,
			default: 9999
		},
		text: {
			type: String,
			default: ''
		},
		additionalContainerCss: {
			type: String,
			default: ''
		},
		additionalContentCss: {
			type: String,
			default: ''
		},
		additionalContentExpandedCss: {
			type: String,
			default: ''
		},
		hasMore: {
			type: Boolean,
			default: true
		},
		moreText: {
			type: String,
			default: 'Show more'
		},
		lessText: {
			type: String,
			default: 'Show less'
		},
		additionalAnchorCss: {
			type: String,
			default: ''
		}
	},
	data() {
		return {
			expanded: false,
			triggerShowMore: false
		};
	},
	computed: {
		additionalCss() {
			if (this.expanded) {
				return this.additionalContentCss;
			}
			return this.additionalContentExpandedCss;
		}
	},
	mounted() {
		this.$nextTick(function () {
			this.determineShowMore();
		});
	},
	updated() {
		this.$nextTick(function () {
			this.determineShowMore();
		});
	},
	methods: {
		onClick() {
			this.expanded = !this.expanded;
			this.$emit('click', this.expanded);
		},
		determineShowMore() {
			// 文字列が省略ellipsis(...)されているかをチェックする
			// scrollWidth、scrollHeight：現在スクロール領域の外側に隠れている部分を含む、ボックスのすべてのコンテンツのサイズ。
			// offsetWidth、offsetHeight：すべての境界線を含むビジュアルボックスのサイズ。
			// @ts-ignore
			if (this.$refs.detail && this.$refs.detail.offsetHeight < this.$refs.detail.scrollHeight) {
				this.triggerShowMore = true;
			} else {
				this.triggerShowMore = false;
			}
		}
	}
};
</script>
<style scoped>
.default {
	--lines: 9999;
	white-space: pre-line;
	display: -webkit-box;
	max-width: 100%;
	-webkit-line-clamp: var(--lines);
	-webkit-box-orient: vertical;
	overflow: hidden;
	text-overflow: ellipsis;
}

.expanded {
	display: block;
	display: -webkit-box;
	white-space: pre-line;
}

.anchor {
	display: block;
	text-align: right;
	padding: 8px;
	margin-bottom: 4px;
	color: var(--primary-color);
	cursor: pointer;
}
</style>
