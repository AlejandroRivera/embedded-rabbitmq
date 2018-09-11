PWD=$(shell pwd)
DOCKER_IMAGE=my_maven
DOCKER_OPTS=-t -v $(PWD):/app -w /app -v ~/.m2:/root/.m2 -v $(HOME)/.embeddedrabbitmq:/root/.embeddedrabbitmq

.PHONY: image clean build deploy report

default: build

clean:
	docker run $(DOCKER_OPTS) $(DOCKER_IMAGE) \
			mvn clean

image:
	docker build -t $(DOCKER_IMAGE) .

build: image
	docker run $(DOCKER_OPTS) $(DOCKER_IMAGE) \
			mvn install

deploy: image
	docker run $(DOCKER_OPTS) $(DOCKER_IMAGE) \
			mvn deploy -DskipTests -s maven-settings.xml

report: image
	docker run $(DOCKER_OPTS) $(DOCKER_IMAGE) \
			mvn jacoco:report coveralls:report