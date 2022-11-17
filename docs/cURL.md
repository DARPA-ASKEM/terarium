# Using cURL to Test Endpoints

cURL can be used to test the endpoints of our API, but requires some special configuration to authenticate with
keycloak. Follow these steps to get yourself up and running.

## Configuration

1. Install [jq](https://stedolan.github.io/jq/) from here, or install with a package manager (eg: `brew install jq`). 
2. Copy `curl-test-endpoint.sh.template` to `curl-test-endpoint.sh`
3. Edit `curl-test-endpoint.sh` setting the following Variables:
* CLIENT_ID
* CLIENT_SECRET
* USERNAME
* PASSWORD
* ACCESS_TOKEN_URL
Or simply override them on the command line; see `--help` of the script file

## Test Endpoint

`bash curl-test-endpoint.sh <URL-to-test>`

