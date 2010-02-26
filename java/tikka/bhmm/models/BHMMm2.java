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
import tikka.bhmm.models.BHMMm1;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model" (M2). This
 * is the same as BHMMm1 but with a different initialization setting. Here,
 * half the words are randomly assigned to the content state.
 *
 * @author tsmoon
 */
public class BHMMm2 extends BHMMm1 {

    public BHMMm2(CommandLineOptions options) {
        super(options);
    }

    /**
     * Randomly initialize learning parameters
     */
    @Override
    public void initializeParametersRandom() {
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
                if (mtfRand.nextDouble() > 0.5) {
                    for (int j = 1; j < stateC; j++) {
                        totalprob += stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + beta)
                              / (stateCounts[j] + wbeta))
                              * ((contentStateBySentence[sentenceoff + j] + alpha)
                              / (sentenceCounts[sentenceid] + calpha))
                              * (firstOrderTransitions[stateoff + j] + gamma);
                    }
                    stateid = 1;
                } else {
                    for (int j = stateC; j < stateS; j++) {
                        totalprob += stateProbs[j] =
                              ((stateByWord[wordstateoff + j] + delta)
                              / (stateCounts[j] + wdelta))
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

                if (stateid < stateC) {
                    contentStateBySentence[sentenceoff + stateid]++;
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
