echo "Running Types.ts generation test"


TYPES_FILE="packages/client/hmi-client/src/types/Types.ts"

MODIFIED_FILES=`git status -s | grep "Types.ts" | wc -l`
if (( MODIFIED_FILES == 0 )); then
  # Types.ts has not been generated.  Run the generation and check if it was created
  # If it was then we know it was not pre-generated and we should fail the build
  ./gradlew generateTypeScript
  MODIFIED_FILES=`git status -s | grep "Types.ts" | wc -l`
  if (( MODIFIED_FILES > 0 )); then
    echo "ERROR: Types.ts was not pre-generated and has now been created. Please make sure the front end is updated accordingly"
    exit 1
  fi
else
  # developer has generated Types.ts.  Take a hash of the file and then regenerate it and compare the hashes
  # If the hashes are different then the file has been modified and we should fail the build
  ORG_FILE_HASH=md5 ${TYPES_FILE}
  ./gradlew generateTypeScript
  NEW_FILE_HASH=md5 ${TYPES_FILE}
  if [ "$ORG_FILE_HASH" != "$NEW_FILE_HASH" ]; then
    echo "ERROR: Types.ts has been modified. Please make sure the front end is updated accordingly"
    exit 1
  fi
fi

exit 0
