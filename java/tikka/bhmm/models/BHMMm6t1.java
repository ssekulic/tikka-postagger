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
 * The "barely hidden markov model" or "bicameral hidden markov model" (M6).
 * This model conditions content words over documents
 *
 * @author tsmoon
 */
public class BHMMm6t1 extends BHMMm6 {

    public BHMMm6t1(CommandLineOptions options) {
        super(options);
    }

    /**
     * Training routine for the inner iterations
     */
    @Override
    protected void trainInnerIter(int itermax, Annealer annealer) {
        int wordid, stateid, docid;
        int current = 0, next;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, stateoff, docoff;

        double prevprior;

        for (int iter = 0; iter < itermax; ++iter) {
            System.err.println("iteration " + iter);
            current = 0;
            for (int i = 0; i < wordN; ++i) {
                if (i % 100000 == 0) {
                    System.err.println("\tProcessing word " + i);
                }
                wordid = wordVector[i];

                docid = documentVector[i];
                stateid = stateVector[i];
                stateoff = current * stateS;
                wordstateoff = wordid * stateS;
                docoff = docid * stateC;

                if (current < stateC) {
                    prevprior = psi;
                } else {
                    prevprior = gamma;
                }

                if (stateid < stateC) {
                    contentStateByDocument[docoff + stateid]--;
                    documentCounts[docid]--;
                }
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
                          / (stateCounts[j] + wbeta))
                          * ((contentStateByDocument[docoff + j] + alpha)
                          / (documentCounts[docid] + calpha))
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
                totalprob = annealer.annealProbs(stateProbs);
                r = mtfRand.nextDouble() * totalprob;
                max = stateProbs[0];
                stateid = 0;
                while (r > max) {
                    stateid++;
                    max += stateProbs[stateid];
                }
                stateVector[i] = stateid;

                if (stateid < stateC) {
                    contentStateByDocument[docoff + stateid]++;
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

        int wordid, docid, stateid;
        int current = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, docoff, stateoff;

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

            docid = documentVector[i];
            stateoff = current * stateS;
            wordstateoff = wordid * stateS;
            docoff = docid * stateC;

            totalprob = 0;
            int j = 0;
            for (; j < stateC; j++) {
                totalprob += stateProbs[j] =
                      ((stateByWord[wordstateoff + j] + beta)
                      / (stateCounts[j] + wbeta))
                      * ((contentStateByDocument[docoff + j] + alpha)
                      / (documentCounts[docid] + calpha))
                      * (firstOrderTransitions[stateoff + j] + gamma);
            }
            for (; j < stateS; j++) {
                totalprob += stateProbs[j] =
                      ((stateByWord[wordstateoff + j] + delta)
                      / (stateCounts[j] + wdelta))
                      * (firstOrderTransitions[stateoff + j] + gamma);
            }
            r = mtfRand.nextDouble() * totalprob;
            max = stateProbs[0];
            stateid = 0;
            while (r > max) {
                stateid++;
                max += stateProbs[stateid];
            }
            stateVector[i] = stateid;

            if (stateid < stateC) {
                contentStateByDocument[docoff + stateid]++;
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
