require('@rushstack/eslint-patch/modern-module-resolution');

module.exports = {
  root: true,
  extends: [
    'plugin:vue/vue3-essential',
    '@vue/eslint-config-airbnb-with-typescript',
    'prettier' // Turns off the formatting rules from the linter since formatting is handled by prettier
  ],
  parser: 'vue-eslint-parser'
};
