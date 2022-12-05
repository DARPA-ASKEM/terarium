version=3.81

PROJECT_DIR ?= .



.SECONDEXPANSION:

.PHONY: all
all:
	@echo "make <cmd>"
	@echo ""
	@echo "Commands:"
	@echo "  clean                    - remove bin, dist and node_modules directories"
	@echo "  build-all                - compile all sources"
	@echo "  build-<target>           - compile sources for a specific target - see below for list of targets"
	@echo "  images                   - build docker images of all targets (except native)"
	@echo "  image-<target>           - build docker image of a specific target - see below for list of targets"



## Targets
TARGETS :=



TARGETS += hmi-server
clean-hmi-server: clean-hmi-server-base
	rm -rf $(PROJECT_DIR)/packages/services/hmi-server/docker/jvm/build

build-hmi-server: clean-hmi-server
	./gradlew :packages:services:hmi-server:build -Dquarkus.package.type=jar

image-hmi-server:
	docker buildx bake hmi-server-base



TARGETS += hmi-server-native
clean-hmi-server-native: clean-hmi-server-base
	rm -rf $(PROJECT_DIR)/packages/services/hmi-server/docker/native/build

build-hmi-server-native: clean-hmi-server-native
	./gradlew :packages:services:hmi-server:build -Dquarkus.package.type=native

image-hmi-server-native:
	docker buildx bake hmi-server-native


TARGETS += hmi-client
clean-hmi-client:
	rm -rf $(PROJECT_DIR)/packages/client/graph-scaffolder/build
	rm -rf $(PROJECT_DIR)/packages/client/graph-scaffolder/dist
	rm -rf $(PROJECT_DIR)/packages/client/hmi-client/dist
	rm -rf $(PROJECT_DIR)/packages/client/hmi-client/docker/dist

build-hmi-client: clean-hmi-client yarn-install
	yarn workspace graph-scaffolder tsc --build
	yarn workspace hmi-client build

image-hmi-client:
	docker buildx bake hmi-client-base

## Clean
.PHONY: clean
clean: $(TARGETS:%=clean-%)
	rm -rf $(PROJECT_DIR)/node_modules

.PHONY: clean-hmi-server-base
clean-hmi-server-base:
	./gradlew :packages:services:hmi-server:clean



## Images
.PHONY: images
images:
	docker buildx bake


## Utilities
yarn-install:
	yarn install
