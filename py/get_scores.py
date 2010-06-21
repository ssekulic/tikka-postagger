#! /usr/bin/python

import os, sys, re

LCURVE_TUPLE=[ "learningcurve0008", "learningcurve0016", "learningcurve0032", \
                 "learningcurve0064", "learningcurve0128", "learningcurve0256", \
                 "learningcurve0512", "learningcurve1024" ]

score_finder=re.compile(r"^(0\.\d+\s*)+$")

data_id_map = {}

models=("m1","m2","m3","m4","m6")

inpath=os.path.abspath(os.path.expanduser(sys.argv[1]))

for fi in os.listdir(inpath):
    fullpath=os.path.join(inpath,fi)
    if os.path.isfile(fullpath):
        labs = fi.split(".")
        corpus=labs[0]
        if not data_id_map.has_key(corpus):
            data_id_map[corpus] = set([])
        data_id = labs[-2]
        if data_id != "full":
            data_id_map[corpus].add(labs[-2])

for corpus in data_id_map.iterkeys():
    idset = data_id_map[corpus]
    data_id_map[corpus] = {}
    maxid = 0
    for id in idset:
        curid = LCURVE_TUPLE.index(id)
        data_id_map[corpus][id] = curid
        if curid > maxid:
            maxid = curid
    maxid += 1
    data_id_map[corpus]["full"] = maxid

dataline = "model.id,corpus,data.id,function.states,content.states,states,f1to1,fmto1,r1to1,rmto1,fprecision,frecall,ffscore,fvi,rprecision,rrecall,rfscore,rvi"
print dataline

for fi in os.listdir(inpath):
    fullpath=os.path.join(inpath,fi)
    if os.path.isfile(fullpath):
        labs = fi.split(".")
        corpus=labs[0]
        data_id = data_id_map[corpus][labs[-2]]
        model_id=labs[-3]
        function_states=labs[-5]
        content_states=labs[-4]
        states = "%d" % (int(function_states) + int(content_states))
        handle = open(fullpath)
        scores = []
        for line in handle:
            m = score_finder.search(line)
            if m:
                scores=line
                break
        if len(scores) > 0:
            scores = scores.split()

            datam = {"model_id":model_id, "data_id":data_id, "corpus":corpus, \
                         "function_states":function_states, "content_states":content_states, \
                         "states":states}

            dataline = "%(model_id)s,%(corpus)s,%(data_id)s,%(function_states)s,%(content_states)s,%(states)s" % datam
            dataline= ",".join([dataline] + scores)
            print dataline
