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
package tikka.bhmm.model.m5.s2;

import tikka.bhmm.model.base.BHMM;
import tikka.bhmm.apps.CommandLineOptions;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model" (M5). 
 * This is the same as M5, only that it's a second order model.
 *
 * @author tsmoon
 */
public class BHMMm5s2 extends BHMM {

    /**
     * Keeps track of the overall function states that were allocated
     */
    protected int functionStateCount;
    /**
     * Normalization term for function state by global function count multinomial
     */
    protected double falpha;

    public BHMMm5s2(CommandLineOptions options) {
        super(options);
        functionStateCount = 0;
        falpha = alpha * stateF;
    }

    /**
     * Training routine for the inner iterations
     */
    @Override
    protected void trainInnerIter(int itermax, String message) {
        int wordid, sentenceid, stateid;
        int prev = 0, current = 0, next, nnext;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, sentenceoff, stateoff, secondstateoff;

        for (int iter = 0; iter < itermax; ++iter) {
            System.err.println("iteration " + iter);
            current = 0;
            for (int i = 0; i < wordN - 2; ++i) {
                if (i % 100000 == 0) {
                    System.err.println("\tProcessing word " + i);
                }
                wordid = wordVector[i];

                if (wordid == EOSi) {
                    secondOrderTransitions[second[i] * S2 + first[i] * S1 + 0]--;
                    secondOrderTransitions[prev * S2 + current * S1 + 0]++;
                    first[i] = current;
                    second[i] = prev;
                    prev = current = 0;
                } else {
                    sentenceid = sentenceVector[i];
                    stateid = stateVector[i];
                    secondstateoff = prev * S2 + current * S1;
                    stateoff = current * S1;
                    wordstateoff = wordid * S1;
                    sentenceoff = sentenceid * stateC;

                    if (stateid < stateC) {
                        contentStateBySentence[sentenceoff + stateid]--;
                        sentenceCounts[sentenceid]--;
                    } else {
                        functionStateCount--;
                    }
                    stateByWord[wordstateoff + stateid]--;
                    stateCounts[stateid]--;
                    secondOrderTransitions[second[i] * S2 + first[i] * S1 + stateid]--;
                    firstOrderTransitions[first[i] * S1 + stateid]--;

                    next = stateVector[i + 1];
                    nnext = stateVector[i + 2];
                    int j = 1;
                    for (; j < stateC; j++) {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + beta)
                              / (stateCounts[j] + wbeta))
                              * ((contentStateBySentence[sentenceoff + j] + alpha)
                              / (sentenceCounts[sentenceid] + calpha))
                              * (secondOrderTransitions[secondstateoff + j] + gamma)
                              * ((secondOrderTransitions[current * S2 + j * S1 + next] + gamma)
                              / (firstOrderTransitions[stateoff + j] + sgamma))
                              * ((secondOrderTransitions[j * S2 + next * S1 + nnext])
                              / (firstOrderTransitions[j * S1 + next] + sgamma));
                    }
                    for (; j < stateS; j++) {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + delta)
                              / (stateCounts[j] + wdelta))
                              * ((stateCounts[j] + alpha)
                              / (functionStateCount + falpha))
                              * (secondOrderTransitions[secondstateoff + j] + gamma)
                              * ((secondOrderTransitions[current * S2 + j * S1 + next] + gamma)
                              / (firstOrderTransitions[stateoff + j] + sgamma))
                              * ((secondOrderTransitions[j * S2 + next * S1 + nnext])
                              / (firstOrderTransitions[j * S1 + next] + sgamma));
                    }
                    totalprob = annealProbs(1, stateProbs);
                    r = mtfRand.nextDouble() * totalprob;
                    max = stateProbs[1];
                    stateid = 1;
                    while (r > max) {
                        stateid++;
                        max += stateProbs[stateid];
                    }
                    stateVector[i] = stateid;

                    if (stateid < stateC) {
                        contentStateBySentence[sentenceoff + stateid]++;
                        sentenceCounts[sentenceid]++;
                    } else {
                        functionStateCount++;
                    }
                    stateByWord[wordstateoff + stateid]++;
                    stateCounts[stateid]++;
                    firstOrderTransitions[stateoff + stateid]++;
                    secondOrderTransitions[secondstateoff + stateid]++;
                    first[i] = current;
                    second[i] = prev;
                    prev = current;
                    current = stateid;
                }
            }
        }
    }

    /**
     * This resets the sentenceCounts array to zero for all elements. This has
     * to be done since the values are set in initializeCounts.
     */
    public void initializeSentenceCounts() {
        try {
            for (int i = 0;; ++i) {
                sentenceCounts[i] = 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    /**
     * Randomly initialize learning parameters
     */
    @Override
    public void initializeParametersRandom() {
        initializeSentenceCounts();
        int wordid, sentenceid, stateid;
        int prev = 0, current = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, sentenceoff, stateoff, secondstateoff;

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

            if (wordid == EOSi) {
                secondOrderTransitions[prev * S2 + current * S1 + 0]++;
                first[i] = current;
                second[i] = prev;
                prev = current = 0;
            } else {
                sentenceid = sentenceVector[i];
                stateoff = current * S1;
                secondstateoff = prev * S2 + current * S1;
                wordstateoff = S1 * wordid;
                sentenceoff = sentenceid * stateC;

                totalprob = 0;
                int j = 1;
                for (; j < stateC; j++) {
                    totalprob += stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + beta)
                          / (stateCounts[j] + wbeta))
                          * ((contentStateBySentence[sentenceoff + j] + alpha)
                          / (sentenceCounts[sentenceid] + calpha))
                          * (secondOrderTransitions[secondstateoff + j] + gamma);
                }
                for (; j < stateS; j++) {
                    totalprob += stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + delta)
                          / (stateCounts[j] + wdelta))
                          * ((stateCounts[j] + alpha)
                          / (functionStateCount + falpha))
                          * (secondOrderTransitions[stateoff + j] + gamma);
                }
                r = mtfRand.nextDouble() * totalprob;
                max = stateProbs[1];
                stateid = 1;
                while (r > max) {
                    stateid++;
                    max += stateProbs[stateid];
                }
                stateVector[i] = stateid;

                if (stateid < stateC) {
                    contentStateBySentence[sentenceoff + stateid]++;
                    sentenceCounts[sentenceid]++;
                } else {
                    functionStateCount++;
                }
                stateByWord[wordstateoff + stateid]++;
                stateCounts[stateid]++;
                firstOrderTransitions[stateoff + stateid]++;
                secondOrderTransitions[secondstateoff + stateid]++;
                first[i] = current;
                second[i] = prev;
                prev = current;
                current = stateid;
            }
        }
    }
}
