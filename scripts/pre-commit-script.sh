#!/bin/sh

echo "Running Spotless before commit..."

./gradlew spotlessCheck

status=$?

if [ "$status" != 0 ]; then
  echo "Spotless check failed, commit denied!"
  exit 1
fi

