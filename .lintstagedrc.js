module.exports = {
	// Lint then format TypeScript and Vue files
  '!(Types.ts).{ts,vue}': [
		'eslint --fix --cache',
		'prettier --write'
	],
	// Format all Java files
  '*.java': [
		'prettier --write'
	]
}
