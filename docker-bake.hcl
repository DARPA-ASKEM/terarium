variable "DOCKER_REGISTRY" {
  default = "ghcr.io"
}
variable "DOCKER_ORG" {
  default = "darpa-askem"
}
variable "VERSION" {
  default = "local"
}

# ---------------------------------
function "tag" {
  params = [image_name, prefix, suffix]
  result = [ "${DOCKER_REGISTRY}/${DOCKER_ORG}/${image_name}:${check_prefix(prefix)}${VERSION}${check_suffix(suffix)}" ]
}

function "check_prefix" {
  params = [tag]
  result = notequal("",tag) ? "${tag}-": ""
}

function "check_suffix" {
  params = [tag]
  result = notequal("",tag) ? "-${tag}": ""
}

# ---------------------------------
group "prod" {
  targets = ["hmi-client", "hmi-server", "hmi-server-native"]
}

group "default" {
  targets = ["hmi-client-base", "hmi-server-base"]
}

# ---------------------------------
target "_platforms" {
  platforms = ["linux/amd64", "linux/arm64"]
}

target "hmi-client-base" {
	context = "."
	tags = tag("hmi-client", "", "")
	dockerfile = "packages/client/hmi-client/docker/Dockerfile"
}

target "hmi-client" {
  inherits = ["_platforms", "hmi-client-base"]
}

target "hmi-server-base" {
	context = "."
	dockerfile = "packages/services/hmi-server/docker/jvm/Dockerfile.jvm"
	tags = tag("hmi-server", "", "")
}

target "hmi-server" {
  inherits = ["_platforms", "hmi-server-base"]
}

target "hmi-server-native" {
	context = "."
  dockerfile = "packages/services/hmi-server/docker/native/Dockerfile.native"
  tags = tag("hmi-server", "", "native")
}
