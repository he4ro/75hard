#!/bin/sh
#
# Gradle start up script for UN*X
#
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME=`dirname "$0"`
APP_HOME=`cd "$APP_HOME" && pwd`

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

JAVA_EXE=java
if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    exit 1
fi

exec "$JAVA_EXE" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
