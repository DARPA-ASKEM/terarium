// module.exports = {
//   '!(Types.ts).{ts,vue}': () => 'yarn run format',
//   '*.java': () => 'npx prettier --write "**/*.java"'
// }

module.exports = {
  '!(Types.ts).{ts,vue}': [
		'prettier --write',
		'eslint --fix --cache'
	],
  '*.java': [
		'prettier --write'
	]
}
