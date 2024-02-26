./gradlew generateTypeScript

MODIFIED_FILES=`git status -s | grep "Types.ts" | wc -l`
if (( MODIFIED_FILES > 0 )); then
  echo "Error! Generated file Types.ts must be generated and committed"
  git status -s
  exit 1
fi

exit 0
