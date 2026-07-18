#!/bin/sh
# Gradle wrapper launcher. If gradle-wrapper.jar is missing, run `gradle wrapper` once.
DIR=$(cd "$(dirname "$0")" && pwd)
exec java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || \
  { echo "gradle-wrapper.jar manquant — ouvre le projet dans Android Studio ou lance 'gradle wrapper'."; exit 1; }
