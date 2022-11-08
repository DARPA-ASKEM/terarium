variable "DOCKER_REGISTRY" {
  default = "ghcr.io"
}
variable "DOCKER_ORG" {
  default = "darpa-askem"
}
variable "VERSION" {
  default = "local"
}
variable "PREFIX" {
  default = ""
}
variable "SUFFIX" {
  default = ""
}

# ---------------------------------
function "tag" {
  params = [image_name]
  result = [ "${DOCKER_REGISTRY}/${DOCKER_ORG}/${image_name}:${check_prefix(PREFIX)}${VERSION}${check_suffix(SUFFIX)}" ]
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
  targets = ["webapp", "hmi-server", "hmi-server-native"]
}

group "default" {
  targets = ["webapp-base", "hmi-server-base"]
}

# ---------------------------------
target "_platforms" {
  platforms = ["linux/amd64", "linux/arm64"]
}

target "webapp-base" {
	context = "packages/client/webapp"
	tags = tag("webapp")
	dockerfile = "docker/Dockerfile"
}

target "webapp" {
  inherits = ["_platforms"]
	context = "webapp"
	tags = tag("webapp")
}

target "hmi-server-base" {
	context = "packages/services/hmi-server"
	tags = tag("hmi-server")
	dockerfile = "docker/Dockerfile.jvm"
}

target "hmi-server" {
  inherits = ["_platforms", "hmi-server-base"]
}

target "hmi-server-native" {
  inherits = ["hmi-server-base"]
  dockerfile = "docker/Dockerfile.native"
  tags = tag("hmi-server-native")
}
