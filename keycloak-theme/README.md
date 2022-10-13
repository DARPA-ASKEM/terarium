# TERArium Keycloak Theme
The basis of this CSS was built around the classes/ids of Keycloak's base theme. 

If new features are added to the login page there may be some instances where you should make some divs more or less visible.

## Building & Deploying
If any changes are made to the theme the Docker image needs to be rebuild by using 
`sh
./build.sh
`
>Make sure that you update the version number.

Once build make sure that it is pushed to the appropriate repository.

Finally update the Kubernetes `gateway-keycloak-deployment.yaml` file with the appropriate new version if not using latest and restart the Kubernates stack.
>The `gateway.sh` script file may need to be updated as well to automatically pull the latest version of the image.

## Theming
Currently only the login screen is being rethemed. Additional views can be rethemed as well such as `account`, `admin`, `email` and `welcome` screens. This is a basic approach that can be reused for other screens. If others are made they should be placed as subdirectories under the `terarium` directory so that the image generated copies them accordingly.
