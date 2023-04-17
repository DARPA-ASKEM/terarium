require('@rushstack/eslint-patch/modern-module-resolution');

module.exports = {
	root: true,
	extends: [
		'plugin:vue/vue3-essential',
		'@vue/eslint-config-airbnb-with-typescript',
		'prettier' // Turns off the formatting rules from the linter since formatting is handled by prettier
	],
	parser: 'vue-eslint-parser',
	rules: {
		'class-methods-use-this': 'off',
		'func-names': 'off',
		'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
		'no-continue': 'off',
		'no-param-reassign': 'off',
		'no-plusplus': 'off',
		'no-use-before-define': 'off',
		'@typescript-eslint/no-use-before-define': 'off',
		'prefer-destructuring': 'off',
		'vuejs-accessibility/click-events-have-key-events': 'off',
		'vue/multi-word-component-names': 'off',
		'import/prefer-default-export': 'off'
	},
	"ignorePatterns": [
		"**/src/temp/*"
	]
};
