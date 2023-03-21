# Makefile

.PHONY: build

run-dist: build test lint test-report run # Build, check and run app

run: # Run app
	./build/install/app/bin/app

start-development:
	APP_ENV=development ./gradlew run

start-production:
	APP_ENV=production ./gradlew run

build: # Build app
	./gradlew clean
	./gradlew generateMigrations
	./gradlew installDist

test: # Run tests
	./gradlew test

lint: # Check code style
	./gradlew checkstyleMain checkstyleTest

test-report: # Check code coverage
	./gradlew jacocoTestReport

migration: # Generate migrations
	./gradlew generateMigrations
