#!/bin/bash

# Stop execution Errors
set -e

# Run tasks in order
./gradlew :utilities:build
./gradlew :utilities:publishToMavenLocal
./gradlew :beam:build
./gradlew :beam:publishToMavenLocal
./gradlew :app:build
./gradlew :kafka:build
./gradlew :list:build

echo "Build and publish tasks completed successfully"

# ./BUILD_EVENTSTREAM.sh