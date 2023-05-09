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
  targets = ["hmi-client", "hmi-server", "hmi-server-native", "hmi-extraction-server"]
}

# Simplified build without the `native` version for quicker turnaround staging deployments
group "staging" {
  targets = ["hmi-client", "hmi-server", "hmi-extraction-server"]
}

group "default" {
  targets = ["hmi-client-base", "hmi-server-base", "hmi-extraction-server"]
}

# ---------------------------------
target "_platforms" {
  platforms = ["linux/amd64", "linux/arm64"]
}

target "hmi-client-base" {
	context = "packages/client/hmi-client/docker"
	tags = tag("hmi-client", "", "")
	dockerfile = "Dockerfile"
}

target "hmi-client" {
  inherits = ["_platforms", "hmi-client-base"]
}

target "hmi-server-base" {
	context = "packages/services/hmi-server/docker/jvm"
	dockerfile = "Dockerfile.jvm"
	tags = tag("hmi-server", "", "")
}

target "hmi-server" {
  inherits = ["_platforms", "hmi-server-base"]
}

target "hmi-server-native" {
	context = "packages/services/hmi-server/docker/native"
  dockerfile = "Dockerfile.native"
  tags = tag("hmi-server", "", "native")
}

target "hmi-extraction-server-base" {
	context = "packages/services/extraction-server/docker"
	dockerfile = "Dockerfile"
	tags = tag("hmi-extraction-server", "", "")
}

target "hmi-extraction-server" {
  inherits = ["_platforms", "hmi-extraction-server-base"]
}
