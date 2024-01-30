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
  targets = ["hmi-client", "hmi-server", "db-migration", "gollm-taskrunner"]
}

group "staging" {
  targets = ["hmi-client", "hmi-server", "db-migration", "gollm-taskrunner"]
}

group "default" {
  targets = ["hmi-client-base", "hmi-server-base", "db-migration-base", "ollm-taskrunner-base"]
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
	context = "." # root of the repo
	dockerfile = "./packages/server/docker/Dockerfile"
	tags = tag("hmi-server", "", "")
}

target "hmi-server" {
  inherits = ["_platforms", "hmi-server-base"]
}

target "db-migration-base" {
	context = "." # root of the repo
	dockerfile = "./packages/db-migration/docker/Dockerfile"
	tags = tag("db-migration", "", "")
}

target "db-migration" {
  inherits = ["_platforms", "db-migration-base"]
}

target "gollm-taskrunner-base" {
	context = "." # root of the repo
	dockerfile = "./packages/taskrunner/docker/Dockerfile.GoLLM"
	tags = tag("gollm-taskrunner-base", "", "")
}

target "gollm-taskrunner" {
  inherits = ["_platforms", "gollm-taskrunner-base"]
}
