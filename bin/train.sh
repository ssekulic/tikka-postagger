#! /bin/sh

. tikka-env
$JAVA_CMD tikka.apps.Train $@

# DATA=/scratch/tsmoon/tikka/data
# PSYCH_LDA=$DATA/psych_bow
# PSYCH=$DATA/psych
# L=learningcurve0008
# M=m1
# D=psych


# java -classpath $CP -Xmx1024m tikka.apps.Train -e $M -c conll2k \
#     -d $PSYCH/train/$L -pi 2 -pr 0.5 -pt 0.1 -s 11 \
#     -m ./models/hhl.$D.$L.$M.model -ks 2000 -itr 20 -kl 1 -n ./out/hhl.$D.$L.$M.train.anno
