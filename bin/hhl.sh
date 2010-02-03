#! /bin/bash

JARS=`echo ./lib/*.jar | tr ' ' ':'`
CP=build/classes:$JARS

# java -classpath $CP -Xmx512m BadMetaphor.apps.Train -e m1 -i 10 -d /Users/tsmoon/Corpora/psychtoy -c conll2k -r 2 > hhl.m1.psychtoy.out

#java -classpath $CP -Xmx512m BadMetaphor.apps.Train -e m1 -i 10  -d /Users/tsmoon/Corpora/psych -c conll2k -r 2 > hhl.m1.psychtoy.out

java -classpath $CP -Xmx512m BadMetaphor.apps.Train -e m1 -i 10 -pi 10 -pr 0.8 -pt 0.1 -d /Users/tsmoon/Corpora/psychreview_from_topic_toolbox -c conll2k -r 2 > hhl.m1.psych.out