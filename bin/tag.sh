#! /bin/bash -x

L=$1
shift
M=$1
shift
D=$1
shift
P=$1
shift

#remaining parameters                                                                                                                                                                                     
R=''
while (( "$#" )); do
    R="$R $1"
    shift
done

date
cdhmm-tagger.sh -l $TIKKA_DIR/models/cdhmm.$D.$L.$M.model -ot $TIKKA_DIR/out/cdhmm.$D.$L.$M.out \
    -oe $TIKKA_DIR/out/cdhmm.eval.$D.$L.$M.out -n $TIKKA_DIR/out/cdhmm.$D.$L.$M.anno.out \
    -d $P/train/$L $R
date