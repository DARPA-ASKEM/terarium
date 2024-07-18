module.exports = {
  '!(Types.ts).{ts,vue}': () => 'yarn run format',
  '*.java': () => 'npx prettier --write "**/*.java'
}
