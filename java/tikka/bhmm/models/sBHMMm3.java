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
import tikka.bhmm.model.base.*;
import tikka.utils.annealer.SimulatedAnnealer;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model" (M3).
 * There is no conditioning on sentences here. Only the hyperparameters are
 * different from the content states and the function states
 *
 * @author tsmoon
 */
public class sBHMMm3 extends sBHMM {

    /**
     * Hyperparameter normalization constant for statecounts. Is a sum of
     * wbeta+wdelta.
     */
    protected double statenorm;

    public sBHMMm3(CommandLineOptions options) {
        super(options);
    }

    @Override
    public void tagTest() {
        statenorm = wbeta + wdelta;

        int wordid, stateid;
        int current = 0, next;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, stateoff;

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];
            stateoff = current * stateS;
            wordstateoff = wordid * stateS;

            totalprob = 0;

            int j = 0;
            for (; j < stateC; j++) {
                try {
                    totalprob += stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + beta)
                          / (stateCounts[j] + statenorm))
                          * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma);
                } catch (ArrayIndexOutOfBoundsException e) {
                    totalprob += stateProbs[j] =
                          (beta / (stateCounts[j] + statenorm))
                          * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma);
                }
            }
            for (; j < stateS; j++) {
                try {
                    totalprob += stateProbs[j] =
                          ((stateByWord[wordstateoff + j] + delta)
                          / (stateCounts[j] + statenorm))
                          * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma);
                } catch (ArrayIndexOutOfBoundsException e) {
                    totalprob += stateProbs[j] =
                          (delta / (stateCounts[j] + statenorm))
                          * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma);
                }
            }
            r = mtfRand.nextDouble() * totalprob;
            max = stateProbs[0];
            stateid = 0;
            while (r > max) {
                stateid++;
                max += stateProbs[stateid];
            }
            stateVector[i] = stateid;
            first[i] = current;
            current = stateid;
        }

        Annealer annealer = new SimulatedAnnealer();
        annealer.setTemperatureReciprocal(1);

        testBurnInIter(testSetBurninIterations, annealer);
    }

    @Override
    protected void testBurnInIter(int itermax, Annealer annealer) {
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
                stateid = stateVector[i];
                stateoff = current * stateS;
                wordstateoff = wordid * stateS;

                try {
                    next = stateVector[i + 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    next = 0;
                }

                int j = 0;
                for (; j < stateC; j++) {
                    try {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + beta)
                              / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma)
                              * (firstOrderTransitions[j * stateS + next] + gamma);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        stateProbs[j] =
                              (beta / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma)
                              * (firstOrderTransitions[j * stateS + next] + gamma);
                    }
                }
                for (; j < stateS; j++) {
                    try {
                        stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + delta)
                              / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma)
                              * (firstOrderTransitions[j * stateS + next] + gamma);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        stateProbs[j] =
                              (delta / (stateCounts[j] + statenorm))
                              * (firstOrderTransitions[stateoff + j] + gamma) / (stateCounts[j] + sgamma)
                              * (firstOrderTransitions[j * stateS + next] + gamma);
                    }
                }

                totalprob = annealer.annealProbs(stateProbs);
                r = mtfRand.nextDouble() * totalprob;
                max = stateProbs[0];
                stateid = 0;
                while (r > max) {
                    stateid++;
                    max += stateProbs[stateid];
                }
                stateVector[i] = stateid;
                first[i] = current;
                current = stateid;
            }
        }
    }
}
