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
import tikka.bhmm.model.base.BHMM;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model" (M3).
 * There is no conditioning on sentences here. Only the hyperparameters are
 * different from the content states and the function states
 *
 * @author tsmoon
 */
public class BHMMm3 extends BHMM {

    /**
     * Hyperparameter normalization constant for statecounts. Is a sum of
     * wbeta+wdelta.
     */
    protected double statenorm;

    public BHMMm3(CommandLineOptions options) {
        super(options);
    }

    /**
     * This sets the hyperparameters at the beginning of the random initialization
     * routine. Pulled out so it's easily noted
     */
    protected void setHyper() {
        statenorm = wbeta + wdelta;
        /**
         * This overwrites the default normalization terms so that I don't have
         * to rewrite the normalization routine in the base class.
         */
        wbeta = wdelta = statenorm;
    }

    /**
     * Training routine for the inner iterations
     */
    @Override
    protected void trainInnerIter(int itermax, Annealer annealer) {
        int wordid, stateid;
        int current = 0, next;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, stateoff;

        for (int iter = 0; iter < itermax; ++iter) {
            System.err.println("iteration " + iter);
            current = 0;
            for (int i = 0; i < wordN; ++i) {
                if (i % 100000 == 0) {
                    System.err.println("\tProcessing word " + i);
                }
                wordid = wordVector[i];

//                if (wordid == EOSi) {
//                    firstOrderTransitions[current * stateS + 0]++;
//                    first[i] = current;
//                    current = 0;
//                } else {
                    stateid = stateVector[i];
                    stateoff = current * stateS;
                    wordstateoff = stateS * wordid;

                    stateByWord[wordstateoff + stateid]--;
                    stateCounts[stateid]--;
                    firstOrderTransitions[first[i] * stateS + stateid]--;

                    try {
                        next = stateVector[i + 1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        next = 0;
                    }

                    int j = 0;
                    for (; j < stateC; j++) {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + beta)
                              / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma)
                              * (firstOrderTransitions[j * stateS + next] + gamma);
                    }
                    for (; j < stateS; j++) {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + delta)
                              / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma)
                              * (firstOrderTransitions[j * stateS + next] + gamma);
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

                    stateByWord[wordstateoff + stateid]++;
                    stateCounts[stateid]++;
                    firstOrderTransitions[stateoff + stateid]++;
                    first[i] = current;
                    current = stateid;
//                }
            }
        }
    }

    /**
     * Randomly initialize learning parameters
     */
    @Override
    public void initializeParametersRandom() {
        setHyper();

        int wordid, stateid;
        int current = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, stateoff;

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

//            if (wordid == EOSi) {
//                firstOrderTransitions[current * stateS + 0]++;
//                first[i] = current;
//                current = 0;
//            } else {
                stateoff = current * stateS;
                wordstateoff = stateS * wordid;

                totalprob = 0;
                if (mtfRand.nextDouble() > 0.5) {
                    for (int j = 0; j < stateC; j++) {
                        totalprob += stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + beta)
                              / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma);
                    }
                    stateid = 0;
                } else {
                    for (int j = stateC; j < stateS; j++) {
                        totalprob += stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + delta)
                              / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma);
                    }
                    r = mtfRand.nextDouble() * totalprob;
                    stateid = stateC;
                }
                r = mtfRand.nextDouble() * totalprob;
                max = stateProbs[stateid];
                while (r > max) {
                    stateid++;
                    max += stateProbs[stateid];
                }
                stateVector[i] = stateid;

                stateByWord[wordstateoff + stateid]++;
                stateCounts[stateid]++;
                firstOrderTransitions[stateoff + stateid]++;
                first[i] = current;
                current = stateid;
//            }
        }
    }
}
