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
public class BHMMm7 extends BHMMm5 {

    public BHMMm7(CommandLineOptions options) {
        super(options);
    }

    /**
     * Normalize the sample counts for words given state. Unlike the base class,
     * it marginalizes word probabilities over the topics for the topic state,
     * i.e. state 0.
     */
    @Override
    protected void normalizeStates() {
        topWordsPerState = new StringDoublePair[stateS][];
        for (int i = 0; i < stateS; ++i) {
            topWordsPerState[i] = new StringDoublePair[outputPerClass];
        }

        double sum = 0.;
        double[] marginalwordprobs = new double[wordN];
        try {
            for (int i = 0;; ++i) {
                marginalwordprobs[i] = 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        for (int j = 0; j < wordW; ++j) {
            int wordoff = j * topicK;
            for (int i = 0; i < topicK; ++i) {
                marginalwordprobs[j] += topicProbs[i]
                      * (TopicByWord[wordoff + i] + beta)
                      / (topicCounts[i] + wbeta);
            }
        }

        {
            sum += stateProbs[0] = stateCounts[0] + wdelta;
            ArrayList<DoubleStringPair> topWords =
                  new ArrayList<DoubleStringPair>();
            for (int j = 0; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                      marginalwordprobs[j], trainIdxToWord.get(
                      j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                topWordsPerState[0][j] =
                      new StringDoublePair(
                      topWords.get(j).stringValue,
                      topWords.get(j).doubleValue);
            }
        }

        for (int i = 0; i < stateS; ++i) {
            sum += stateProbs[i] = stateCounts[i] + wdelta;
            ArrayList<DoubleStringPair> topWords =
                  new ArrayList<DoubleStringPair>();
            /**
             * Start at one to leave out EOSi
             */
            for (int j = 0; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                      stateByWord[j * stateS + i] + delta, trainIdxToWord.get(
                      j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                topWordsPerState[i][j] =
                      new StringDoublePair(
                      topWords.get(j).stringValue,
                      topWords.get(j).doubleValue / stateProbs[i]);
            }
        }

        for (int i = 0; i < stateS; ++i) {
            stateProbs[i] /= sum;
        }
    }

    @Override
    public void initializeParametersRandom() {
        int wordid, docid, topicid, stateid;
        int current = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordtopicoff, wordstateoff, cstatedocoff, docoff, stateoff;

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];
            docid = documentVector[i];
            stateoff = current * stateS;
            wordtopicoff = wordid * topicK;
            wordstateoff = wordid * stateS;
            docoff = docid * topicK;
            cstatedocoff = docid * stateC;

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
                          * ((contentStateByDocument[cstatedocoff + j] + alpha)
                          / (documentCounts[docid] + calpha))
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
                contentStateByDocument[cstatedocoff + stateid]++;
                documentCounts[docid]++;
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
        int wordid, docid, topicid, stateid;
        int current = 0, next;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordtopicoff, wordstateoff, cstatedocoff, docoff, stateoff;

        for (int iter = 0; iter < itermax; ++iter) {
            System.err.println("iteration " + iter);
            current = 0;
            for (int i = 0; i < wordN; i++) {
                if (i % 100000 == 0) {
                    System.err.println("\tProcessing word " + i);
                }
                wordid = wordVector[i];
                docid = documentVector[i];
                stateid = stateVector[i];
                topicid = topicVector[i];
                wordstateoff = wordid * stateS;
                wordtopicoff = wordid * topicK;
                docoff = docid * topicK;
                cstatedocoff = docid * stateC;

                if (stateid == 0) {
                    TopicByWord[wordtopicoff + topicid]--;
                    DocumentByTopic[docoff + topicid]--;
                    topicCounts[topicid]--;
                } else if (stateid < stateC) {
                    contentStateByDocument[cstatedocoff + stateid]--;
                    documentCounts[docid]--;
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
                          * ((contentStateByDocument[cstatedocoff + j] + alpha)
                          / (documentCounts[docid] + calpha))
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
                    contentStateByDocument[cstatedocoff + stateid]++;
                    documentCounts[docid]++;
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
        int wordid = 0, stateid = 0, docid, topicid;
        int stateoff, wordstateoff, wordtopicoff, cstatedocoff, docoff;

        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];
            docid = documentVector[i];
            stateid = stateVector[i];
            topicid = topicVector[i];

            stateoff = current * stateS;
            wordstateoff = wordid * stateS;
            wordtopicoff = wordid * topicK;
            docoff = docid * topicK;
            cstatedocoff = docid * stateC;

            if (stateid == 0) {
                TopicByWord[wordtopicoff + topicid]++;
                DocumentByTopic[docoff + topicid]++;
                topicCounts[topicid]++;
            } else if (stateid < stateC) {
                contentStateByDocument[cstatedocoff + stateid]++;
                documentCounts[docid]++;
            }

            stateByWord[wordstateoff + stateid]++;
            stateCounts[stateid]++;
            firstOrderTransitions[stateoff + stateid]++;
            first[i] = current;
            current = stateid;
        }
    }
}
