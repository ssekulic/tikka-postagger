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
package tikka.models.hhl.m3;

import java.io.BufferedWriter;
import tikka.apps.CommandLineOptions;

import tikka.models.hhl.HDPHMMLDA;

import tikka.structures.DoubleStringPair;
import tikka.structures.StringDoublePair;
import tikka.structures.distributions.DirichletBaseDistribution;
import tikka.structures.distributions.AffixStemStateHDP;
import tikka.structures.distributions.StemStateDP;
import tikka.structures.distributions.StemTopicDP;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This is a pure HDPHMM model. This model assumes that only a few states
 * (designated by {@link #topicSubStates}) generate topic words and the remaining
 * states generate state words. Affixes are conditioned on stems. Stems are
 * based on a DP. Affixes come from an HDP.
 * the state.
 *
 * @author tsmoon
 */
public class HDPHMM extends HDPHMMLDA {

    /**
     * Default constructor.
     *
     * @param options   Options from the command line.
     */
    public HDPHMM(CommandLineOptions options) throws IOException {
        super(options);
    }

    /**
     * Initialize the distributions that will be used in this model.
     */
    @Override
    protected void initalizeDistributions() {

        affixBaseDistribution = new DirichletBaseDistribution(
              affixLexicon, affixBoundaryProb, muAffix);

        stemStateBaseDistribution = new DirichletBaseDistribution(
              stemLexicon, stemBoundaryProb, wgamma);

        affixStemStateHDP = new AffixStemStateHDP(affixBaseDistribution,
              affixLexicon, wgamma, stateS);
        stemStateDP = new StemStateDP(
              stemStateBaseDistribution, stemLexicon, wgamma);
    }

    /**
     * Initializes from a pretrained, loaded model. Use this if the model has
     * been loaded from a pretrained model.
     */
    @Override
    public void initializeFromLoadedModel(CommandLineOptions options) throws
          IOException {
        super.initializeFromLoadedModel(options);

        initalizeDistributions();

        int current = 0, prev = 0, pprev = 0;
        int wordid = 0, stateid = 0, splitid = 0, stemid = 0, affixid = 0;
        int wordstateoff;
        String word = "", stem = "", affix = "";

        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

            if (wordid == EOSi) {
                thirdOrderTransitions[pprev * S3 + prev * S2 + current
                      * stateS + 0]++;
                first[i] = current;
                second[i] = prev;
                third[i] = pprev;
                pprev = prev = current = 0;
            } else {
                stateid = stateVector[i];
                splitid = splitVector[i];

                wordstateoff = wordid * stateS;

                word = trainIdxToWord.get(wordid);

                stem = word.substring(0, splitid);
                affix = word.substring(splitid, word.length());
                stemid = stemLexicon.getOrPutIdx(stem);
                affixid = affixLexicon.getOrPutIdx(affix);
                stemVector[i] = stemid;
                affixVector[i] = affixid;

                stemStateDP.inc(stateid, stemid);
                affixStemStateHDP.inc(stateid, stemid, affixid);
                StateByWord[wordstateoff + stateid]++;
                stateCounts[stateid]++;
                secondOrderTransitions[prev * S2 + current * stateS + stateid]++;
                thirdOrderTransitions[pprev * S3 + prev * S2 + current * stateS + stateid]++;
                first[i] = current;
                second[i] = prev;
                third[i] = pprev;
                pprev = prev;
                prev = current;
                current = stateid;
            }
        }
    }

    /**
     * Randomly set the model parameters for use in training
     */
    @Override
    protected void randomInitializeParameters() {
        /**
         * Declaring temporary variables for training
         */
        int wordid = 0, stateid = 0, splitid = 0, stemid = 0, affixid = 0;
        int current = 0, prev = 0, pprev = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, thirdstateoff, secondstateoff;
        int wlength = 0;
        String word = "", stem = "", affix = "";

        double[] splitProbs = new double[MAXLEN];

        /**
         * Initialize by assigning random state indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

            if (wordid == EOSi) {
                thirdOrderTransitions[pprev * S3 + prev * S2 + current
                      * stateS + 0]++;
                first[i] = current;
                second[i] = prev;
                third[i] = pprev;
                pprev = prev = current = 0;
            } else {

                wordstateoff = wordid * stateS;

                thirdstateoff = pprev * S3 + prev * S2 + current * stateS;
                secondstateoff = prev * S2 + current * stateS;
                totalprob = 0;
                try {
                    for (int j = 1;; j++) {
                        totalprob += stateProbs[j] =
                              (StateByWord[wordstateoff + j] + gamma)
                              / (stateCounts[j] + wgamma)
                              * (thirdOrderTransitions[thirdstateoff + j]
                              + psi);
                    }
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                }

                r = mtfRand.nextDouble() * totalprob;
                max = stateProbs[1];
                stateid = 1;
                while (r > max) {
                    stateid++;
                    max += stateProbs[stateid];
                }
                stateVector[i] = stateid;

                word = trainIdxToWord.get(wordid);
                wlength = word.length();
                totalprob = 0;
                for (int j = 0; j < wlength + 1; ++j) {
                    stem = word.substring(0, j);
                    affix = word.substring(j, wlength);
                    stemid = stemLexicon.getIdx(stem);
                    affixid = affixLexicon.getIdx(affix);

                    totalprob += splitProbs[j] = stemStateDP.probNumerator(
                          stateid, stem)
                          * affixStemStateHDP.prob(stateid, stemid, affix);
                }
                r = mtfRand.nextDouble() * totalprob;
                max = splitProbs[0];
                splitid = 0;
                while (r > max) {
                    splitid++;
                    max += splitProbs[splitid];
                }
                stem = word.substring(0, splitid);
                affix = word.substring(splitid, wlength);
                stemid = stemLexicon.getOrPutIdx(stem);
                affixid = affixLexicon.getOrPutIdx(affix);
                stemVector[i] = stemid;
                affixVector[i] = affixid;
                splitVector[i] = splitid;

                stemStateDP.inc(stateid, stemid);
                affixStemStateHDP.inc(stateid, stemid, affixid);
                StateByWord[wordstateoff + stateid]++;
                secondOrderTransitions[secondstateoff + stateid]++;
                thirdOrderTransitions[thirdstateoff + stateid]++;
                stateCounts[stateid]++;
                first[i] = current;
                second[i] = prev;
                third[i] = pprev;
                pprev = prev;
                prev = current;
                current = stateid;
            }
        }
    }

    /**
     * Training routine for the inner iterations
     */
    @Override
    protected void trainInnerIter(int itermax, String message) {
        /**
         * Declaring temporary variables for training
         */
        int wordid = 0, stateid = 0, splitid = 0, stemid = 0, affixid = 0;
        int current = 0, prev = 0, pprev = 0, next = 0, nnext = 0, nnnext = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int wordstateoff, thirdstateoff, secondstateoff;
        int wlength = 0, splitmax = 0;
        String word = "";
        String[] stems = new String[MAXLEN], affixes = new String[MAXLEN];
        int[] stemidxes = new int[MAXLEN], affixidxes = new int[MAXLEN];

        double[] splitProbs = new double[MAXLEN];

        for (int initer = 0; initer < itermax; ++initer) {
            System.err.print("\n" + message + " " + initer);
            System.err.print("\tprocessing word ");
            current = 0;
            prev = 0;
            pprev = 0;
            for (int i = 0; i < wordN - 3; i++) {

                if (i % 100000 == 0) {
                    System.err.print(i + ",");
                }
                wordid = wordVector[i];

                if (wordid == EOSi) // sentence marker
                {
                    thirdOrderTransitions[third[i] * S3 + second[i] * S2 + first[i] * stateS + 0]--;
                    thirdOrderTransitions[pprev * S3 + prev * S2 + current * stateS + 0]++;
                    first[i] = current;
                    second[i] = prev;
                    third[i] = pprev;
                    current = prev = pprev = 0;
                } else {
                    word = trainIdxToWord.get(wordid);
                    stateid = stateVector[i];
                    stemid = stemVector[i];
                    affixid = affixVector[i];

                    wlength = word.length();
                    splitmax = wlength + 1;
                    for (int k = 0; k < splitmax; ++k) {
                        stems[k] = word.substring(0, k);
                        affixes[k] = word.substring(k, wlength);
                        stemidxes[k] = stemLexicon.getIdx(stems[k]);
                        affixidxes[k] = affixLexicon.getIdx(affixes[k]);
                    }

                    wordstateoff = wordid * stateS;

                    /**
                     * Decrement counts of current assignment from states,
                     * switches, stems, and affixes.
                     */
                    stemStateDP.dec(stateid, stemid);
                    affixStemStateHDP.dec(stateid, stemid, affixid);
                    stateCounts[stateid]--;
                    StateByWord[wordstateoff + stateid]--;
                    secondOrderTransitions[second[i] * S2 + first[i] * stateS + stateid]--;
                    thirdOrderTransitions[third[i] * S3 + second[i] * S2 + first[i] * stateS + stateid]--;

                    /**
                     * Drawing new stateid
                     */
                    next = stateVector[i + 1];
                    nnext = stateVector[i + 2];
                    nnnext = stateVector[i + 3];
                    thirdstateoff = pprev * S3 + prev * S2 + current * stateS;
                    secondstateoff = prev * S2 + current * stateS;

                    try {
                        for (int j = 1;; ++j) {
                            totalprob = 0;
                            for (int k = 0; k < splitmax; ++k) {
                                totalprob += stemStateDP.prob(j, stems[k])
                                      * affixStemStateHDP.prob(j, stemidxes[k], affixes[k]);
                            }
                            stateProbs[j] = totalprob
                                  * (thirdOrderTransitions[thirdstateoff + j] + psi)
                                  * (((thirdOrderTransitions[prev * S3 + current * S2 + j * stateS + next] + psi)
                                  / (secondOrderTransitions[secondstateoff + j] + spsi))
                                  * ((thirdOrderTransitions[current * S3 + j * S2 + next * stateS + nnext] + psi)
                                  / (secondOrderTransitions[current * S2 + j * stateS + next] + spsi))
                                  * ((thirdOrderTransitions[j * S3 + next * S2 + nnext * stateS + nnnext] + psi)
                                  / (secondOrderTransitions[j * S2 + next * stateS + nnext] + spsi)));
                        }
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
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

                    /**
                     * Drawing new stem and affix
                     */
                    for (int j = 0; j < splitmax; ++j) {
                        splitProbs[j] = stemStateDP.probNumerator(stateid, stems[j])
                              * affixStemStateHDP.prob(stateid, stemidxes[j], affixes[j]);
                    }
                    totalprob = annealProbs(splitProbs, splitmax);
                    r = mtfRand.nextDouble() * totalprob;
                    max = splitProbs[0];
                    splitid = 0;
                    while (r > max) {
                        splitid++;
                        max += splitProbs[splitid];
                    }
                    stemid = stemLexicon.getOrPutIdx(stems[splitid]);
                    affixid = affixLexicon.getOrPutIdx(affixes[splitid]);
                    stemVector[i] = stemid;
                    affixVector[i] = affixid;
                    splitVector[i] = splitid;

                    /**
                     * Increment counts of current assignment from states,
                     * switches, stems, and affixes.
                     */
                    stemStateDP.inc(stateid, stemid);
                    StateByWord[wordstateoff + stateid]++;
                    affixStemStateHDP.inc(stateid, stemid, affixid);
                    stateCounts[stateid]++;
                    secondOrderTransitions[secondstateoff + stateid]++;
                    thirdOrderTransitions[thirdstateoff + stateid]++;
                    first[i] = current;
                    second[i] = prev;
                    third[i] = pprev;
                    pprev = prev;
                    prev = current;
                    current = stateid;
                }
            }
        }
    }

    /**
     * Normalize the sample counts for words over topics and states by summing over possible
     * segmentations. The parameters for the segmentation were learned during
     * the training stage.
     */
    @Override
    protected void normalizeWords(double[] StateByWordProbs,
          double[] TopicByWordProbs) {

        /**
         * Calculate word probability per topic and word probability per state
         * (but only for topic states)
         */
        int wlength = 0, splitmax = 0;
        String word = "";
        String[] stems = new String[MAXLEN], affixes = new String[MAXLEN];
        int[] stemidxes = new int[MAXLEN], affixidxes = new int[MAXLEN];

        /**
         * Calculate word probability per state (but only for non-topic states)
         */
        for (int wordid = 1; wordid < wordW; ++wordid) {
            word = trainIdxToWord.get(wordid);
            int wordstateoff = wordid * stateS;

            wlength = word.length();
            splitmax = wlength + 1;
            for (int k = 0; k < splitmax; ++k) {
                stems[k] = word.substring(0, k);
                affixes[k] = word.substring(k, wlength);
                stemidxes[k] = stemLexicon.getIdx(stems[k]);
                affixidxes[k] = affixLexicon.getIdx(affixes[k]);
            }

            for (int i = 1; i < stateS; ++i) {
                double p = 0;
                for (int k = 0; k < splitmax; ++k) {
                    double stemProb = stemStateDP.prob(i, stems[k]);
                    double affixProb = affixStemStateHDP.prob(i, stemidxes[k], affixes[k]);
                    p += stemProb * affixProb;
                }
                StateByWordProbs[wordstateoff + i] = p;
            }
        }

        setTopWordsPerState(StateByWordProbs);
    }

    /**
     * Method for setting probability of tokens per sample.
     *
     * @param outiter Number of sample run
     */
    @Override
    protected void obtainSample(int outiter) {
        /**
         * Declaring temporary variables for training
         */
        int wordid = 0, topicid = 0, stateid = 0;
        double totalprob = 0;
        int wlength = 0, splitmax = 0;
        String word = "";
        String[] stems = new String[MAXLEN], affixes = new String[MAXLEN];
        int[] stemidxes = new int[MAXLEN], affixidxes = new int[MAXLEN];

        for (int i = 0; i < wordN; i++) {

            if (i % 100000 == 0) {
                System.err.print(i + ",");
            }
            wordid = wordVector[i];

            if (wordid != EOSi) // sentence marker
            {
                word = trainIdxToWord.get(wordVector[i]);
                stateid = stateVector[i];
                topicid = topicVector[i];

                wlength = word.length();
                splitmax = wlength + 1;
                for (int k = 0; k < splitmax; ++k) {
                    stems[k] = word.substring(0, k);
                    affixes[k] = word.substring(k, wlength);
                    stemidxes[k] = stemLexicon.getIdx(stems[k]);
                    affixidxes[k] = affixLexicon.getIdx(affixes[k]);
                }

                totalprob = 0;
                if (stateid < 1) {
                    for (int k = 0; k < splitmax; ++k) {
                        totalprob += stemTopicDP.prob(topicid, stems[k])
                              * affixStemStateHDP.prob(stateid, stemidxes[k], affixes[k]);
                    }
                } else {
                    for (int k = 0; k < splitmax; ++k) {
                        totalprob += stemStateDP.prob(stateid, stems[k])
                              * affixStemStateHDP.prob(stateid, stemidxes[k], affixes[k]);
                    }
                }
                SampleProbs[outiter] += Math.log(totalprob);
            }
        }
    }

    /**
     * Set the arrays testWordTopicProbs and testWordStateProbs
     */
    @Override
    protected void setWordClassProbArrays() {
        double totalprob = 0;
        int wordstateoff;
        String word = "";
        int wlength = 0, splitmax = 0;
        String[] stems = new String[MAXLEN], affixes = new String[MAXLEN];
        int[] stemidxes = new int[MAXLEN], affixidxes = new int[MAXLEN];

        /**
         * Assign probabilities to each word given either topic or state.
         * This will not change in the test run so we store this in table
         * at the beginning.
         */
        testWordStateProbs = new double[wordW * stateS];

        for (int i = 0; i < wordW; ++i) {
            word = testIdxToWord.get(i);
            wlength = word.length();
            splitmax = wlength + 1;
            for (int k = 0; k < splitmax; ++k) {
                stems[k] = word.substring(0, k);
                affixes[k] = word.substring(k, wlength);
                stemidxes[k] = stemLexicon.getIdx(stems[k]);
                affixidxes[k] = affixLexicon.getIdx(affixes[k]);
            }
            wordstateoff = i * stateS;
            for (int j = 1; j < stateS; ++j) {
                totalprob = 0;
                for (int k = 0; k < splitmax; ++k) {
                    totalprob += stemStateDP.prob(j, stems[k])
                          * affixStemStateHDP.prob(j, stemidxes[k], affixes[k]);
                }
                testWordStateProbs[wordstateoff + j] = totalprob;
            }
        }
    }

    /**
     * Normalize the training sample
     */
    @Override
    public void normalize() {
        stemStateDP.normalize(1, stateS, outputPerClass, null);
        affixStemStateHDP.normalize(1, stateS, outputPerClass,
              stemStateDP, stemTopicDP);

        double[] StateByWordProbs = new double[wordW * stateS];
        try {
            for (int i = 0;; ++i) {
                StateByWordProbs[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        normalizeWords(StateByWordProbs, null);
        normalizeRawStates();
    }

    /**
     * Print normalized probabilities for each category to out. Print only
     * the top {@link #outputPerClass} per category.
     *
     * @param out   Buffer to write to.
     * @throws IOException
     */
    @Override
    public void printTabulatedProbabilities(BufferedWriter out) throws
          IOException {
        affixStemStateHDP.print(1, stateS, outputPerClass, stateProbs,
              out);
        printNewlines(out, 4);
        stemStateDP.print(1, stateS, outputPerClass, stateProbs, out);
        printNewlines(out, 4);

        printStates(out);
        printNewlines(out, 4);
        printStatesRaw(out);
        out.close();
    }

    /**
     * Sample word segmentations. This is in the last stage of sampling
     * after all classes have been sampled. It is only needed to print the
     * annotated text
     */
    @Override
    protected void sampleTestWordSplitLocations() {
        int wordid = 0, stateid = 0, splitid = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        String word = "";
        int wlength = 0, splitmax = 0;
        String[] stems = new String[MAXLEN], affixes = new String[MAXLEN];
        int[] stemidxes = new int[MAXLEN], affixidxes = new int[MAXLEN];

        double[] splitProbs = new double[MAXLEN];

        System.err.println("\nSampling split locations");
        for (int i = 0; i < wordN; i++) {
            wordid = wordVector[i];
            if (wordid != EOSi) // sentence marker
            {
                stateid = stateVector[i];

                word = testIdxToWord.get(wordid);
                wlength = word.length();
                splitmax = wlength + 1;
                for (int k = 0; k < splitmax; ++k) {
                    stems[k] = word.substring(0, k);
                    affixes[k] = word.substring(k, wlength);
                    stemidxes[k] = stemLexicon.getIdx(stems[k]);
                    affixidxes[k] = affixLexicon.getIdx(affixes[k]);
                }

                for (int j = 0; j < splitmax; ++j) {
                    splitProbs[j] = stemStateDP.prob(stateid, stems[j])
                          * affixStemStateHDP.prob(stateid, stemidxes[j],
                          affixes[j]);
                }
                totalprob = annealProbs(splitProbs, splitmax);
                r = mtfRand.nextDouble() * totalprob;
                max = splitProbs[0];
                splitid = 0;
                while (r > max) {
                    splitid++;
                    max += splitProbs[splitid];
                }
                splitVector[i] = splitid;
            }
        }
    }
}
