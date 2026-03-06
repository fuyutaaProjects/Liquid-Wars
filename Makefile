GRADLE = ./gradlew

.PHONY: all
all: build

.PHONY: build
build:
	$(GRADLE) build

.PHONY: clean
clean:
	$(GRADLE) clean

.PHONY: run
run:
	$(GRADLE) run
