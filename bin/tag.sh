#! /bin/sh

# JARS=`echo ./lib/*.jar | tr ' ' ':'`
# CP=build/classes:$JARS

# java -classpath $CP -Xmx1024m tikka.apps.Tagger $1

. tikka-env
$JAVA_CMD tikka.apps.Tagger $@
