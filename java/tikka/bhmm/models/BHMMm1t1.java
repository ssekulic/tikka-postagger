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

import tikka.bhmm.apps.CommandLineOptions;
import tikka.utils.annealer.Annealer;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model" (M7). This
 * takes M4 and makes the transitions to be local as well.
 *
 * @author tsmoon
 */
public class BHMMm1t1 extends BHMMm1 {

    public BHMMm1t1(CommandLineOptions options) {
        super(options);
    }

    /**
     * Training routine for the inner iterations
     */
    @Override
    protected void trainInnerIter(int itermax, Annealer annealer) {
        int wordid, sentenceid, stateid;
        int current = 0, next;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, sentenceoff, stateoff;

        double prevprior;

        for (int iter = 0; iter < itermax; ++iter) {
            System.err.println("iteration " + iter);
            current = 0;
            for (int i = 0; i < wordN; ++i) {
                if (i % 100000 == 0) {
                    System.err.println("\tProcessing word " + i);
                }
                wordid = wordVector[i];

                if (wordid == EOSi) {
                    firstOrderTransitions[current * stateS + 0]++;
                    first[i] = current;
                    current = 0;
                } else {
                    sentenceid = sentenceVector[i];
                    stateid = stateVector[i];
                    stateoff = current * stateS;
                    wordstateoff = stateS * wordid;
                    sentenceoff = stateC * sentenceid;

                    if (current < stateC) {
                        prevprior = psi;
                    } else {
                        prevprior = gamma;
                    }

                    if (stateid < stateC) {
                        contentStateBySentence[sentenceoff + stateid]--;
                        sentenceCounts[sentenceid]--;
                    }
                    stateByWord[wordstateoff + stateid]--;
                    stateCounts[stateid]--;
                    firstOrderTransitions[first[i] * stateS + stateid]--;

                    try {
                        next = stateVector[i + 1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        next = 0;
                    }

                    int j = 1;
                    for (; j < stateC; j++) {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + beta)
                              / (stateCounts[j] + wbeta))
                              * ((contentStateBySentence[sentenceoff + j] + alpha)
                              / (sentenceCounts[sentenceid] + calpha))
                              * (firstOrderTransitions[stateoff + j] + prevprior) / (stateCounts[j] + spsi)
                              * (firstOrderTransitions[j * stateS + next] + psi);
                    }
                    for (; j < stateS; j++) {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + delta)
                              / (stateCounts[j] + wdelta))
                              * (firstOrderTransitions[stateoff + j] + prevprior) / (stateCounts[j] + sgamma)
                              * (firstOrderTransitions[j * stateS + next] + gamma);
                    }
                    totalprob = annealer.annealProbs(1, stateProbs);
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
                    }
                    stateByWord[wordstateoff + stateid]++;
                    stateCounts[stateid]++;
                    firstOrderTransitions[stateoff + stateid]++;
                    first[i] = current;
                    current = stateid;
                }
            }
        }
    }

    @Override
    public void initializeCountArrays() {
        super.initializeCountArrays();
        spsi = psi * stateS;
    }

    /**
     * Randomly initialize learning parameters
     */
    @Override
    public void initializeParametersRandom() {
        initializeSentenceCounts();
        int wordid, sentenceid, stateid;
        int current = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, sentenceoff, stateoff;

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

            if (wordid == EOSi) {
                firstOrderTransitions[current * stateS + 0]++;
                first[i] = current;
                current = 0;
            } else {
                sentenceid = sentenceVector[i];
                stateoff = current * stateS;
                wordstateoff = stateS * wordid;
                sentenceoff = sentenceid * stateC;

                totalprob = 0;
                int j = 1;
                for (; j < stateC; j++) {
                    totalprob += stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + beta)
                          / (stateCounts[j] + wbeta))
                          * ((contentStateBySentence[sentenceoff + j] + alpha)
                          / (sentenceCounts[sentenceid] + calpha))
                          * (firstOrderTransitions[stateoff + j] + gamma);
                }
                for (; j < stateS; j++) {
                    totalprob += stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + delta)
                          / (stateCounts[j] + wdelta))
                          * (firstOrderTransitions[stateoff + j] + gamma);
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
                }
                stateByWord[wordstateoff + stateid]++;
                stateCounts[stateid]++;
                firstOrderTransitions[stateoff + stateid]++;
                first[i] = current;
                current = stateid;
            }
        }
    }
}
