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

# ---------------------------------
function "tag" {
  params = [image_name]
  result = [ "${DOCKER_REGISTRY}/${DOCKER_ORG}/${image_name}:${check_prefix(PREFIX)}${VERSION}" ]
}

function "check_prefix" {
  params = [tag]
  result = notequal("",tag) ? "${tag}-": ""
}

# ---------------------------------
group "prod" {
  targets = ["theme"]
}

group "default" {
  targets = ["theme"]
}

# ---------------------------------
target "_platforms" {
  platforms = ["linux/amd64", "linux/arm64"]
	dockerfile = "Dockerfile"
}

target "theme" {
  inherits = ["_platforms"]
	context = "keycloak-theme"
	tags = tag("terarium-theme")
}
