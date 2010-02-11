#! /bin/bash

JARS=`echo ./lib/*.jar | tr ' ' ':'`
CP=build/classes:$JARS

java -classpath $CP -Xmx1024m tikka.apps.Tagger $1
