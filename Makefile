version=3.81

BACKUP_TIME := `date -u +%F-%H%M%S`
PROJECT_DIR ?= .
DEPLOY_LOCAL := ${PROJECT_DIR}/deploy/local
DOCKER_IMAGE_TAG ?= dev
DOCKER_IMAGE_ROOT := docker.uncharted.software/terarium

.SECONDEXPANSION:

.PHONY: all
all:
	@echo "make <cmd>"
	@echo ""
	@echo "Commands:"
	@echo "  clean                    - remove bin, dist and node_modules directories"
	@echo "  images                   - build docker images of all targets"
	@echo "  image-<target>           - build docker image of a specific target - see below for list of targets"
	@echo "  publish                  - build and push dock images for deployment"
	@echo "  publish-<target>         - build and push dock image for a specific target - see below for list of targets"
	@echo "  lint                     - lint the source code"
	@echo "  test                     - test the source code"
	@echo "Targets building (but not pushing) docker images of separately:"
	@echo "  $(TARGETS)"
	@echo ""
	@echo "Variables:"
	@echo "  DOCKER_IMAGE_TAG=<tag>         - tag to append to each pushed docker image (default: dev)"



## Targets
TARGETS :=

TARGETS += hmi-server
clean-hmi-server:
	./gradlew :packages:services:hmi-server:clean

image-hmi-server: clean-hmi-server
	./gradlew :packages:services:hmi-server:build -Dquarkus.container-image.build=true -Dquarkus.container-image.tag=$(DOCKER_IMAGE_TAG)

publish-hmi-server: clean-hmi-server
	./gradlew :packages:services:hmi-server:build -Dquarkus.container-image.push=true -Dquarkus.container-image.tag=$(DOCKER_IMAGE_TAG)

publish-native-hmi-server: clean-hmi-server
	./gradlew :packages:services:hmi-server:build -Dquarkus.container-image.push=true -Dquarkus.container-image.tag=$(DOCKER_IMAGE_TAG)-native -Dquarkus.package.type=native


TARGETS += data-server
clean-data-server:
	./gradlew :packages:services:data-server:clean

image-data-server: clean-data-server
	./gradlew :packages:services:data-server:build -Dquarkus.container-image.build=true -Dquarkus.container-image.tag=$(DOCKER_IMAGE_TAG)

publish-data-server: clean-data-server
	./gradlew :packages:services:data-server:build -Dquarkus.container-image.push=true -Dquarkus.container-image.tag=$(DOCKER_IMAGE_TAG)

publish-native-data-server: clean-data-server
	./gradlew :packages:services:data-server:build -Dquarkus.container-image.push=true -Dquarkus.container-image.tag=$(DOCKER_IMAGE_TAG)-native -Dquarkus.package.type=native


TARGETS += webapp
clean-webapp:
	rm -rf $(PROJECT_DIR)/packages/client/webapp/dist
	rm -rf $(PROJECT_DIR)/packages/client/webapp/docker/dist

image-webapp: clean-webapp yarn-install
	yarn workspace graph-scaffolder tsc --build
	yarn workspace webapp build
	mv $(PROJECT_DIR)/packages/client/webapp/dist $(PROJECT_DIR)/packages/client/webapp/docker
	docker build -t $(DOCKER_IMAGE_ROOT)/webapp:$(DOCKER_IMAGE_TAG) $(PROJECT_DIR)/packages/client/webapp/docker

publish-webapp: image-webapp
	docker push $(DOCKER_IMAGE_ROOT)/webapp:$(DOCKER_IMAGE_TAG)

publish-native-webapp:
	# Do nothing



## Clean
.PHONY: clean
clean: $(TARGETS:%=clean-%)
	rm -rf $(PROJECT_DIR)/node_modules



## Images
.PHONY: images
images: $(TARGETS:%=image-%)



## Publish
.PHONY: publish
publish: $(TARGETS:%=publish-%)



## Publish Native
.PHONY: publish-native
publish-native: $(TARGETS:%=publish-native-%)



## Utilities
yarn-install:
	yarn install
