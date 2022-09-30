require('@rushstack/eslint-patch/modern-module-resolution');

module.exports = {
  root: true,
	"env": {
    "browser": true,
    "es2021": true
  },
  extends: [
		"eslint:recommended",
		"plugin:vue/vue3-recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:import/typescript",
		"airbnb-base",
    'prettier' // Turns off the formatting rules from the linter since formatting is handled by prettier
  ],
  parser: 'vue-eslint-parser',
	"parserOptions": {
    "ecmaVersion": "latest",
    "sourceType": "module",
    "parser": "@typescript-eslint/parser"
  },
  "settings": {
    "import/resolver": {
      "typescript": {} // this loads <rootdir>/tsconfig.json to eslint
    }
  },
	rules: {
		"import/extensions": [
			"error",
			"ignorePackages",
			{
				"": "never",
				"js": "never",
				"jsx": "never",
				"ts": "never",
				"tsx": "never"
			}
	  ],
		// to enforce using type for object type definitions, can be type or interface
    "@typescript-eslint/consistent-type-definitions": ["error", "type"],
		// others
		'vue/multi-word-component-names': 'off',
		'vuejs-accessibility/click-events-have-key-events': 'off',
		'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
		"import/no-unresolved": "off",
		"vue/no-v-html": "off"
	}
};
