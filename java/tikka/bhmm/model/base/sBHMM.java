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
package tikka.bhmm.model.base;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import tikka.bhmm.apps.CommandLineOptions;
import tikka.opennlp.io.*;
import tikka.utils.annealer.*;
import tikka.utils.normalizer.*;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model" (M6).
 * Same as model 6, only it's supervised.
 *
 * @author tsmoon
 */
public abstract class sBHMM extends BHMM {

//    protected final int TESTITER = 10;
    protected double accuracy = 0;

    public sBHMM(CommandLineOptions options) {
        super(options);

//        tagMap = TagMapGenerator.generate(options.getTagSet(), 0, stateS);
        stateC = tagMap.getContentTagSize();
        stateF = tagMap.getFunctionTagSize();
        stateS = tagMap.getFullGoldTagSize();        
    }

    /**
     * Initialize arrays that will be used to track the state, topic, split
     * position and switch of each token. The DocumentByTopic array is also
     * rewritten in sampling for test sets.
     *
     * @param dirReader Object to walk through files and directories
     * @param wordIdx   Dictionary from word to index
     * @param idxToWord Dictionary from index to word
     */
    @Override
    protected void initializeTokenArrays(DirReader dirReader,
          HashMap<String, Integer> wordIdx, HashMap<Integer, String> idxToWord) {
        super.initializeTokenArrays(dirReader, wordIdx, idxToWord);

        stateVector = goldTagVector;
    }

    /**
     * Nothing is done here
     */
    @Override
    protected void trainInnerIter(int itermax, Annealer annealer) {
    }

    @Override
    protected void initializeCountArrays() {
        super.initializeCountArrays();

        int wordid, stateid;
        int current = 0;
        int wordstateoff, stateoff;

        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];
            stateid = stateVector[i];
            stateoff = current * stateS;
            wordstateoff = wordid * stateS;

            stateByWord[wordstateoff + stateid]++;
            stateCounts[stateid]++;
            firstOrderTransitions[stateoff + stateid]++;
            current = stateid;
        }
    }

    @Override
    public void initializeFromLoadedModel(CommandLineOptions options) {
        stateS = stateF + stateC;
        wordNormalizer = new WordNormalizerToLower(tagMap);
        testWordIdx = trainWordIdx;
        trainIdxToWord = new HashMap<Integer, String>();
        for (String word : trainWordIdx.keySet()) {
            trainIdxToWord.put(trainWordIdx.get(word), word);
        }
        testIdxToWord = trainIdxToWord;
        super.initializeTokenArrays(testDirReader, testWordIdx, testIdxToWord);

        int[] sbw = stateByWord;
        int[] sc = stateCounts;
        int[] fot = firstOrderTransitions;
        super.initializeCountArrays();
        stateByWord = sbw;
        stateCounts = sc;
        firstOrderTransitions = fot;
    }

    /**
     * Nothing is done here
     */
    @Override
    public void initializeParametersRandom() {
    }

    @Override
    public void evaluate() {
        tagTest();
        Annealer annealer = new MaximumPosteriorDecoder();
        testBurnInIter(1, annealer);

        int total = wordN;
        int correct = 0;
        for (int i = 0; i < total; ++i) {
            if (stateVector[i] == goldTagVector[i]) {
                correct++;
            }
        }
        accuracy = correct / (double) total;
        System.err.println(String.format("%f", accuracy));
    }

    @Override
    public void printEvaluationScore(BufferedWriter out) throws IOException {
        out.write(String.format("%f", accuracy));
    }

    public abstract void tagTest();

    protected abstract void testBurnInIter(int itermax, Annealer annealer);
}
