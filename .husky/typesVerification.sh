echo "Running Types.ts generation test"

MODIFIED_FILES=`git status -s | grep "Types.ts" | wc -l`
if (( MODIFIED_FILES == 0 )); then
  # Types.ts has not been generated.
  ./gradlew generateTypeScript
  MODIFIED_FILES=`git status -s | grep "Types.ts" | wc -l`
  if (( MODIFIED_FILES > 0 )); then
    echo "Types.ts was not pre-generated and has now been created. Please make sure the front end is updated accordingly"
    exit 1
  fi
else
  ORG_FILE_HASH=md5 packages/client/hmi-client/src/types/Types.ts
  ./gradlew generateTypeScript
  NEW_FILE_HASH=md5 packages/client/hmi-client/src/types/Types.ts
  if [ "$ORG_FILE_HASH" != "$NEW_FILE_HASH" ]; then
    echo "Types.ts has been modified. Please make sure the front end is updated accordingly"
    exit 1
  fi
fi

exit 0
