#!/bin/sh

#
# Standard Gradle wrapper launcher script.
# If Android Studio reports the wrapper jar is missing, open this project
# in Android Studio and accept its prompt to regenerate the Gradle wrapper,
# or run `gradle wrapper` once from a machine that has Gradle installed.
#

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_HOME=$(cd "$(dirname "$0")" >/dev/null && pwd)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
