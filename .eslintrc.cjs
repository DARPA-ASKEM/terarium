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
		'max-classes-per-file': 'off',
		'class-methods-use-this': 'off',
		'func-names': 'off',
		'no-alert': 'off',
		'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
		'no-continue': 'off',
		'no-param-reassign': 'off',
		'no-plusplus': 'off',
		'no-use-before-define': 'off',
		'@typescript-eslint/no-use-before-define': 'off',
		'@typescript-eslint/no-unused-vars': ['warn', { 'argsIgnorePattern': '^_' }],
		'prefer-destructuring': 'off',
		'vuejs-accessibility/click-events-have-key-events': 'off',
		'vuejs-accessibility/label-has-for': 'off',
		'vuejs-accessibility/form-control-has-label': 'off',
		'vuejs-accessibility/no-autofocus': 'off',
		'vue/require-toggle-inside-transition': 'off',
		'vue/multi-word-component-names': 'off',
		'import/prefer-default-export': 'off',
		'no-underscore-dangle': 'off',
		'no-bitwise': 'off'
	},
	"ignorePatterns": [
		"**/src/temp/*"
	]
};
