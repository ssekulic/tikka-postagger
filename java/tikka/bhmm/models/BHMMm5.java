///////////////////////////////////////////////////////////////////////////////
//  Copyright (C) 2010 Taesun Moon, The University of Texas at Austin
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 3 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////
package tikka.bhmm.models;

import java.io.BufferedWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

import tikka.bhmm.apps.CommandLineOptions;
import tikka.structures.*;
import tikka.utils.annealer.Annealer;

/**
 * * The "barely hidden markov model" or "bicameral hidden markov model".
 * This is the cd-lda-hmm implementation (i.e. combination of lda and cd and hmm)
 *
 * @author tsmoon
 */
public class BHMMm5 extends BHMMm4 {

    public BHMMm5(CommandLineOptions options) {
        super(options);
    }

    @Override
    public void initializeParametersRandom() {
        int wordid, docid, topicid, sentenceid, stateid;
        int current = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordtopicoff, wordstateoff, docoff, sentenceoff, stateoff;

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];
            sentenceid = sentenceVector[i];
            docid = documentVector[i];
            stateoff = current * stateS;
            wordtopicoff = wordid * topicK;
            wordstateoff = wordid * stateS;
            sentenceoff = sentenceid * stateC;
            docoff = docid * topicK;

            if (mtfRand.nextDouble() > 0.5) {
                stateVector[i] = 1;
            } else {
                stateVector[i] = 0;
            }

            totalprob = 0;
            try {
                for (int j = 0;; ++j) {
                    topicProbs[j] = DocumentByTopic[docoff + j] + alpha;
                    if (stateVector[i] == 1) {
                        topicProbs[j] *= (TopicByWord[wordtopicoff + j] + beta)
                              / (topicCounts[j] + wbeta);
                    }
                    totalprob += topicProbs[j];
                }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            }

            max = topicProbs[0];
            topicid = 0;
            r = mtfRand.nextDouble() * totalprob;
            while (r > max) {
                topicid++;
                max += topicProbs[topicid];
            }
            topicVector[i] = topicid;
            max = 0;

            totalprob = 0;
            if (stateVector[i] == 0) {
                int j = 0;
                totalprob = stateProbs[j] =
                      (TopicByWord[wordtopicoff + topicid] + delta)
                      / (topicCounts[topicid] + wdelta)
                      * (firstOrderTransitions[current * S1 + j] + gamma);
                for (j = 1; j < stateC; j++) {
                    totalprob += stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + beta)
                          / (stateCounts[j] + wbeta))
                          * ((contentStateBySentence[sentenceoff + j] + alpha)
                          / (sentenceCounts[sentenceid] + calpha))
                          * (firstOrderTransitions[stateoff + j] + gamma);
                }
                for (; j < stateS; j++) {
                    totalprob += stateProbs[j] =
                          (stateByWord[wordstateoff + j] + beta)
                          / (stateCounts[j] + wbeta)
                          * (firstOrderTransitions[current * S1 + j]
                          + gamma);
                }
            }

            r = mtfRand.nextDouble() * totalprob;
            stateid = 0;
            max = stateProbs[stateid];
            while (r > max) {
                stateid++;
                max += stateProbs[stateid];
            }
            stateVector[i] = stateid;

            if (stateid == 0) {
                TopicByWord[wordtopicoff + topicid]++;
                DocumentByTopic[docoff + topicid]++;
                topicCounts[topicid]++;
            } else if (stateid < stateC) {
                contentStateBySentence[sentenceoff + stateid]++;
                sentenceCounts[sentenceid]++;
            }
            stateByWord[wordstateoff + stateid]++;
            stateCounts[stateid]++;
            firstOrderTransitions[current * S1 + stateid]++;
            first[i] = current;
            current = stateid;
        }
    }

    @Override
    protected void trainInnerIter(int itermax, Annealer annealer) {
        int wordid, docid, sentenceid, topicid, stateid;
        int current = 0, next;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordtopicoff, wordstateoff, sentenceoff, docoff, stateoff;

        for (int iter = 0; iter < itermax; ++iter) {
            System.err.println("iteration " + iter);
            current = 0;
            for (int i = 0; i < wordN; i++) {
                if (i % 100000 == 0) {
                    System.err.println("\tProcessing word " + i);
                }
                wordid = wordVector[i];
                sentenceid = sentenceVector[i];
                docid = documentVector[i];
                stateid = stateVector[i];
                topicid = topicVector[i];
                wordstateoff = wordid * stateS;
                wordtopicoff = wordid * topicK;
                sentenceoff = sentenceid * stateC;
                docoff = docid * topicK;

                if (stateid == 0) {
                    TopicByWord[wordtopicoff + topicid]--;
                    DocumentByTopic[docoff + topicid]--;
                    topicCounts[topicid]--;
                } else if (stateid < stateC) {
                    contentStateBySentence[sentenceoff + stateid]--;
                    sentenceCounts[sentenceid]--;
                }
                stateByWord[wordstateoff + stateid]--;
                stateCounts[stateid]--;
                firstOrderTransitions[first[i] * S1 + stateid]--;

                try {
                    for (int j = 0;; j++) {
                        topicProbs[j] = DocumentByTopic[docoff + j] + alpha;
                        if (stateid == 1) {
                            topicProbs[j] *= (TopicByWord[wordtopicoff + j] + beta)
                                  / (topicCounts[j] + wbeta);
                        }
                    }
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                }
                totalprob = annealer.annealProbs(topicProbs);
                r = mtfRand.nextDouble() * totalprob;
                max = topicProbs[0];

                topicid = 0;
                while (r > max) {
                    topicid++;
                    max += topicProbs[topicid];
                }
                topicVector[i] = topicid;

                stateoff = current * stateS;
                try {
                    next = stateVector[i + 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    next = 0;
                }

                int j = 0;
                stateProbs[j] =
                      ((TopicByWord[wordtopicoff + topicid] + beta) / (topicCounts[topicid] + wbeta))
                      * (firstOrderTransitions[stateoff + j] + gamma)
                      * ((firstOrderTransitions[S1 + next] + gamma)
                      / (stateCounts[j] + sgamma));
                for (j = 1; j < stateC; j++) {
                    stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + beta)
                          / (stateCounts[j] + wbeta))
                          * ((contentStateBySentence[sentenceoff + j] + alpha)
                          / (sentenceCounts[sentenceid] + calpha))
                          * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma)
                          * (firstOrderTransitions[j * stateS + next] + gamma);
                }
                for (; j < stateS; j++) {
                    stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + delta) / (stateCounts[j] + wdelta))
                          * (firstOrderTransitions[stateoff + j] + gamma)
                          * ((firstOrderTransitions[j * stateS + next] + gamma)
                          / (stateCounts[j] + sgamma));
                }
                totalprob = annealer.annealProbs(stateProbs);
                r = mtfRand.nextDouble() * totalprob;
                stateid = 0;
                max = stateProbs[stateid];
                while (r > max) {
                    stateid++;
                    max += stateProbs[stateid];
                }
                stateVector[i] = stateid;

                if (stateid == 0) {
                    TopicByWord[wordtopicoff + topicid]++;
                    DocumentByTopic[docoff + topicid]++;
                    topicCounts[topicid]++;
                } else if (stateid < stateC) {
                    contentStateBySentence[sentenceoff + stateid]++;
                    sentenceCounts[sentenceid]++;
                }

                stateByWord[wordstateoff + stateid]++;
                stateCounts[stateid]++;
                firstOrderTransitions[current * S1 + stateid]++;
                first[i] = current;
                current = stateid;
            }
        }
    }

    @Override
    public void initializeFromLoadedModel(CommandLineOptions options) throws
          IOException {
        super.initializeFromLoadedModel(options);

        int current = 0;
        int wordid = 0, sentenceid, stateid = 0, docid, topicid;
        int sentenceoff, stateoff, wordstateoff, wordtopicoff, docoff;

        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];
            sentenceid = sentenceVector[i];
            docid = documentVector[i];
            stateid = stateVector[i];
            topicid = topicVector[i];

            sentenceoff = sentenceid * stateC;
            stateoff = current * stateS;
            wordstateoff = wordid * stateS;
            wordtopicoff = wordid * topicK;
            docoff = docid * topicK;


            if (stateid == 0) {
                TopicByWord[wordtopicoff + topicid]++;
                DocumentByTopic[docoff + topicid]++;
                topicCounts[topicid]++;
            } else if (stateid < stateC) {
                contentStateBySentence[sentenceoff + stateid]++;
                sentenceCounts[sentenceid]++;
            }

            stateByWord[wordstateoff + stateid]++;
            stateCounts[stateid]++;
            firstOrderTransitions[stateoff + stateid]++;
            first[i] = current;
            current = stateid;
        }
    }
}
