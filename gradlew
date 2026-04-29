#!/bin/sh
# Gradle wrapper shell script
# Copyright The Gradle Authors — Apache 2.0 License

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(dirname "$0")
APP_HOME=$(cd "$APP_HOME" && pwd)

MAX_FD="maximum"
GRADLE_OPTS="${GRADLE_OPTS:-}"

# OS detection
cygwin=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN*)  cygwin=true ;;
  Darwin*)  darwin=true ;;
  NONSTOP*) nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

JAVACMD="${JAVACMD:-java}"
if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
fi

exec "$JAVACMD" \
  $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
