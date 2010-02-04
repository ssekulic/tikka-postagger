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
package tikka.models.hhl.m1;

import tikka.exceptions.EmptyCountException;
import tikka.models.hhl.HDPHMMLDA;
import tikka.structures.DoubleStringPair;
import tikka.structures.StringDoublePair;
import tikka.structures.distributions.AffixStateDP;
import tikka.structures.distributions.DirichletBaseDistribution;
import tikka.structures.distributions.HierarchicalDirichletBaseDistribution;
import tikka.structures.distributions.StemAffixStateDP;
import tikka.structures.distributions.StemAffixStateHDP;
import tikka.structures.distributions.StemAffixTopicDP;
import tikka.structures.distributions.StemAffixTopicHDP;
import tikka.structures.lexicons.Lexicon;
import tikka.utils.CommandLineOptions;

import tikka.utils.ec.util.MersenneTwisterFast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import tikka.opennlp.io.DirWriter;

/**
 * This is a pure HDPHMMLDA model. This model assumes that only a few states
 * (designated by {@link #topicSubStates}) generate topic words and the remaining
 * states generate state words. Stems are conditioned on affixes. Affixes are conditioned
 * on states. Stems are conditioned on either the state or the topic given
 * the state.
 * 
 * @author tsmoon
 */
public class HDPHMMLDAm1 extends HDPHMMLDA {

    /**
     * Default constructor.
     * 
     * @param options   Options from the command line.
     */
    public HDPHMMLDAm1(CommandLineOptions options) throws IOException {
        super(options);
    }

    /**
     * 
     */
    public HDPHMMLDAm1() {
    }

    /**
     * Initialize the distributions that will be used in this model.
     */
    @Override
    protected void initalizeDistributions() {
        stemTopicHierarchicalBaseDistribution =
                new HierarchicalDirichletBaseDistribution(stemLexicon,
                stemBoundaryProb, betaStemBase);
        dirichletAffixBaseDistribution = new DirichletBaseDistribution(
                affixLexicon, affixBoundaryProb, muAffix) {

            @Override
            public int dec(String s) throws EmptyCountException {
                throw new UnsupportedOperationException("Don't use this!");
            }

            @Override
            public int inc(String s) {
                throw new UnsupportedOperationException("Don't use this!");
            }
        };

        dirichletStemBaseDistribution = new DirichletBaseDistribution(
                stemLexicon, stemBoundaryProb, muStem) {

            @Override
            public int dec(String s) throws EmptyCountException {
                throw new UnsupportedOperationException("Don't use this!");
            }

            @Override
            public int inc(String s) {
                throw new UnsupportedOperationException("Don't use this!");
            }
        };

        stemAffixTopicHDP = new StemAffixTopicHDP(
                stemTopicHierarchicalBaseDistribution, stemLexicon, betaStem);
        stemAffixStateDP = new StemAffixStateDP(
                dirichletStemBaseDistribution, stemLexicon, muStem);
        affixStateDP = new AffixStateDP(dirichletAffixBaseDistribution,
                affixLexicon, muAffix);
    }

    /**
     * Initializes from a pretrained, loaded model. Use this if the model has
     * been loaded from a pretrained model.
     */
    public void initializeFromModel() {

        /**
         * Initialize random number generator
         */
        mtfRand = new MersenneTwisterFast(randomSeed);

        /**
         * Revive some constants that will be used often
         */
        wbeta = beta * wordW;
        wgamma = gamma * wordW;
        spsi = stateS * psi;
        S3 = stateS * stateS * stateS;
        S2 = stateS * stateS;

        /**
         * Revive the annealing regime
         */
        temperature = initialTemperature;
        temperatureReciprocal = 1 / temperature;
        innerIterations = iterations;
        outerIterations =
                (int) Math.round(
                (initialTemperature - targetTemperature) / temperatureDecrement) + 1;

        /**
         * These are not saved in the model so must be revived
         */
        stemVector = new int[wordN];
        affixVector = new int[wordN];

        first = new int[wordN];
        second = new int[wordN];
        third = new int[wordN];

        idxToWord = new HashMap<Integer, String>();
        for (String word : wordIdx.keySet()) {
            idxToWord.put(wordIdx.get(word), word);
        }

        StemToIdx = new HashMap<String, Integer>();
        AffixToIdx = new HashMap<String, Integer>();
        stemLexicon = new Lexicon(StemToIdx);
        affixLexicon = new Lexicon(AffixToIdx);

        topicCounts = new int[topicK];
        topicProbs = new double[topicK];
        for (int i = 0; i < topicK; ++i) {
            topicCounts[i] = 0;
            topicProbs[i] = 0.;
        }

        TopicByWord = new int[topicK * wordW];
        try {
            for (int i = 0;; ++i) {
                TopicByWord[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        DocumentByTopic = new int[documentD * topicK];
        try {
            for (int i = 0;; ++i) {
                DocumentByTopic[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        stateCounts = new int[stateS];
        stateProbs = new double[stateS];
        for (int i = 0; i < stateS; ++i) {
            stateCounts[i] = 0;
            stateProbs[i] = 0;
        }

        StateByWord = new int[stateS * wordW];
        try {
            for (int i = 0;; ++i) {
                StateByWord[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        thirdOrderTransitions = new int[stateS * stateS * stateS * stateS];
        secondOrderTransitions = new int[stateS * stateS * stateS];
        try {
            for (int i = 0;; ++i) {
                thirdOrderTransitions[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }
        try {
            for (int i = 0;; ++i) {
                secondOrderTransitions[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        initalizeDistributions();

        int current = 0, prev = 0, pprev = 0;
        int wordid = 0, docid = 0, topicid = 0, stateid = 0, splitid = 0, stemid =
                0, affixid = 0;
        int docoff, wordstateoff, wordtopicoff;
        String word = "", stem = "", affix = "";

        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

            if (wordid == EOSi) {
                thirdOrderTransitions[pprev * S3 + prev * S2 + current *
                        stateS + 0]++;
                first[i] = current;
                second[i] = prev;
                third[i] = pprev;
                pprev = prev = current = 0;
            } else {

                wordid = wordVector[i];
                docid = documentVector[i];
                docoff = topicK * docid;
                wordstateoff = wordid * stateS;
                wordtopicoff = wordid * topicK;

                word = idxToWord.get(wordid);

                topicid = topicVector[i];
                stateid = stateVector[i];
                splitid = splitVector[i];
                stem = word.substring(0, splitid);
                affix = word.substring(splitid, word.length());
                stemid = stemLexicon.getOrPutIdx(stem);
                affixid = affixLexicon.getOrPutIdx(affix);
                stemVector[i] = stemid;
                affixVector[i] = affixid;

                if (stateid < topicSubStates) {
                    stemAffixTopicHDP.inc(topicid, affixid, stemid);
                    DocumentByTopic[docoff + topicid]++;
                    topicCounts[topicid]++;
                    TopicByWord[wordtopicoff + topicid]++;
                } else {
                    stemAffixStateDP.inc(stateid, affixid, stemid);
                    StateByWord[wordstateoff + stateid]++;
                }

                affixStateDP.inc(stateid, affixid);
                stateVector[i] = stateid;

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
     * Train the model.
     */
    @Override
    public void train() {
        /**
         * Declaring temporary variables for training
         */
        int wordid = 0, docid = 0, topicid = 0, stateid = 0, splitid = 0, stemid =
                0, affixid = 0;
        int current = 0, prev = 0, pprev = 0, next = 0,
                nnext = 0, nnnext = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int docoff, wordstateoff, wordtopicoff, thirdstateoff, secondstateoff;
        String word = "", stem = "", affix = "";

        double[] splitProbs = new double[100];

        /**
         * Initialize by assigning random topic indices to words
         */
        for (int i = 0; i < wordN; ++i) {
            wordid = wordVector[i];

            if (wordid == EOSi) {
                thirdOrderTransitions[pprev * S3 + prev * S2 + current *
                        stateS + 0]++;
                first[i] = current;
                second[i] = prev;
                third[i] = pprev;
                pprev = prev = current = 0;
            } else {

                wordid = wordVector[i];
                docid = documentVector[i];
                docoff = topicK * docid;
                wordstateoff = wordid * stateS;
                wordtopicoff = wordid * topicK;

                word = idxToWord.get(wordid);
                int wlength = word.length();

                topicid = mtfRand.nextInt(topicK);
                topicVector[i] = topicid;

                splitid = mtfRand.nextInt(wlength + 1);
                stem = word.substring(0, splitid);
                affix = word.substring(splitid, wlength);
                stemid = stemLexicon.getOrPutIdx(stem);
                affixid = affixLexicon.getOrPutIdx(affix);
                stemVector[i] = stemid;
                affixVector[i] = affixid;

                /**
                 * Randomly assign half to topic states and half to non-topic
                 * states
                 */
                if (mtfRand.nextBoolean()) {
                    stateid = mtfRand.nextInt(topicSubStates - 1) + 1;
                    stemAffixTopicHDP.inc(topicid, affixid, stemid);
                    DocumentByTopic[docoff + topicid]++;
                    topicCounts[topicid]++;
                    TopicByWord[wordtopicoff + topicid]++;
                } else {
                    stateid =
                            mtfRand.nextInt(stateS - topicSubStates) + topicSubStates;
                    stemAffixStateDP.inc(stateid, affixid, stemid);
                }
                affixStateDP.inc(stateid, affixid);
                stateVector[i] = stateid;
                StateByWord[wordstateoff + stateid]++;

                thirdstateoff = pprev * S3 + prev * S2 + current * stateS +
                        stateid;
                secondstateoff = prev * S2 + current * stateS + stateid;

                stateCounts[stateid]++;
                secondOrderTransitions[secondstateoff]++;
                thirdOrderTransitions[thirdstateoff]++;
                first[i] = current;
                second[i] = prev;
                third[i] = pprev;
                pprev = prev;
                prev = current;
                current = stateid;
            }
        }

        for (int outiter = 0; outiter < outerIterations; ++outiter) {
            System.err.print("\nouter iteration " + outiter + ":");
            System.err.print("annealing temperature " + temperature);
            stabilizeTemperature();
            for (int initer = 0; initer < innerIterations; ++initer) {
                System.err.print("\ninner iteration " + initer);
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
                        word = idxToWord.get(wordVector[i]);
                        docid = documentVector[i];
                        stateid = stateVector[i];
                        topicid = topicVector[i];
                        stemid = stemVector[i];
                        affixid = affixVector[i];
                        stem = stemLexicon.getString(stemid);
                        affix = affixLexicon.getString(affixid);

                        docoff = docid * topicK;
                        wordstateoff = wordid * stateS;
                        wordtopicoff = wordid * topicK;

                        /**
                         * Decrement counts of current assignment from topics, states,
                         * switches, stems, and affixes.
                         */
                        if (stateid < topicSubStates) {
                            stemAffixTopicHDP.dec(topicid, affixid, stemid);
                            DocumentByTopic[docoff + topicid]--;
                            topicCounts[topicid]--;
                            TopicByWord[wordtopicoff + topicid]--;
                        } else {
                            stemAffixStateDP.dec(stateid, affixid, stemid);
                            StateByWord[wordstateoff + stateid]--;
                        }
                        affixStateDP.dec(stateid, affixid);
                        stateCounts[stateid]--;
                        secondOrderTransitions[second[i] * S2 + first[i] * stateS + stateid]--;
                        thirdOrderTransitions[third[i] * S3 + second[i] * S2 + first[i] * stateS + stateid]--;

                        /**
                         * Drawing new topicid
                         */
                        try {
                            for (int j = 0;; ++j) {
                                topicProbs[j] =
                                        DocumentByTopic[docoff + j] + alpha;
                                if (stateid < topicSubStates) {
                                    topicProbs[j] *= stemAffixTopicHDP.prob(j,
                                            affixid, stem);
                                }
                            }
                        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        }
                        totalprob = annealProbs(topicProbs);
                        r = mtfRand.nextDouble() * totalprob;
                        max = topicProbs[0];

                        topicid = 0;
                        while (r > max) {
                            topicid++;
                            max += topicProbs[topicid];
                        }

                        topicVector[i] = topicid;

                        /**
                         * Drawing new stateid
                         */
                        next = stateVector[i + 1];
                        nnext = stateVector[i + 2];
                        nnnext = stateVector[i + 3];
                        thirdstateoff =
                                pprev * S3 + prev * S2 + current * stateS;
                        secondstateoff = prev * S2 + current * stateS;

                        try {
                            for (int j = 1; j < topicSubStates; ++j) {
                                stateProbs[j] =
                                        stemAffixTopicHDP.prob(topicid, affixid,
                                        stem) *
                                        affixStateDP.prob(j, affix) *
                                        (thirdOrderTransitions[thirdstateoff + j] + psi) *
                                        (((thirdOrderTransitions[prev * S3 + current * S2 + j * stateS + next] + psi) /
                                        (secondOrderTransitions[secondstateoff + j] + spsi)) *
                                        ((thirdOrderTransitions[current * S3 + j * S2 + next * stateS + nnext] + psi) /
                                        (secondOrderTransitions[current * S2 + j * stateS + next] + spsi)) *
                                        ((thirdOrderTransitions[j * S3 + next * S2 + nnext * stateS + nnnext] + psi) /
                                        (secondOrderTransitions[j * S2 + next * stateS + nnext] + spsi)));
                            }
                            for (int j = topicSubStates;; ++j) {
                                stateProbs[j] =
//                                    ((StateByWord[wordstateoff + j] + beta) / (stateCounts[j] + wbeta)) *
                                        stemAffixStateDP.prob(stateid, affixid,
                                        stem) *
                                        affixStateDP.prob(j, affix) *
                                        (thirdOrderTransitions[thirdstateoff + j] + psi) *
                                        (((thirdOrderTransitions[prev * S3 + current * S2 + j * stateS + next] + psi) /
                                        (secondOrderTransitions[secondstateoff + j] + spsi)) *
                                        ((thirdOrderTransitions[current * S3 + j * S2 + next * stateS + nnext] + psi) /
                                        (secondOrderTransitions[current * S2 + j * stateS + next] + spsi)) *
                                        ((thirdOrderTransitions[j * S3 + next * S2 + nnext * stateS + nnnext] + psi) /
                                        (secondOrderTransitions[j * S2 + next * stateS + nnext] + spsi)));
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
                        int wlength = word.length();
                        for (int j = 0; j < wlength + 1; ++j) {
                            stem = word.substring(0, j);
                            affix = word.substring(j, wlength);
                            stemid = stemLexicon.getIdx(stem);
                            affixid = affixLexicon.getIdx(affix);

                            if (stateid < topicSubStates) {
                                splitProbs[j] = stemAffixTopicHDP.prob(topicid,
                                        affixid, stem) *
                                        affixStateDP.probNumerator(stateid,
                                        affix);
                            } else {
                                splitProbs[j] = stemAffixStateDP.prob(stateid,
                                        affixid, stem) *
                                        affixStateDP.probNumerator(stateid,
                                        affix);
                            }
                        }
                        totalprob = annealProbs(splitProbs, wlength + 1);
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

                        /**
                         * Increment counts of current assignment from topics, states,
                         * switches, stems, and affixes.
                         */
                        if (stateid < topicSubStates) {
                            stemAffixTopicHDP.inc(topicid, affixid, stemid);
                            DocumentByTopic[docoff + topicid]++;
                            topicCounts[topicid]++;
                            TopicByWord[wordtopicoff + topicid]++;
                        } else {
                            stemAffixStateDP.inc(stateid, affixid, stemid);
                            StateByWord[wordstateoff + stateid]++;
                        }
                        affixStateDP.inc(stateid, affixid);
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
            temperature -= temperatureDecrement;
            temperatureReciprocal = 1 / temperature;
        }
    }

    /**
     * Normalize the sample counts for words over topics and states by summing over possible
     * segmentations. The parameters for the segmentation were learned during
     * the training stage.
     */
    @Override
    protected void normalizeWords() {
        Double sum = 0.;
        double[] StateByWordProbs = new double[wordW * stateS];
        try {
            for (int i = 0;; ++i) {
                StateByWordProbs[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        double[] TopicByWordProbs = new double[wordW * topicK];
        try {
            for (int i = 0;; ++i) {
                TopicByWordProbs[i] = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        }

        double[] nonexistentStateAffixProbs = affixStateDP.
                getNonexistentStateAffixProbs();
        for (int wordid = 0; wordid < wordW; ++wordid) {
            String word = idxToWord.get(wordid);
            int wordtopicoff = wordid * topicK;
            int wlength = word.length();
            String[] stems = new String[wlength + 1];
            String[] affixes = new String[wlength + 1];
            int[] affixids = new int[wlength + 1];
            for (int i = 1; i < topicSubStates; ++i) {
                for (int j = 0; j < wlength + 1; ++j) {
                    String stem = word.substring(0, j);
                    String affix = word.substring(j, wlength);
                    stems[j] = stem;
                    affixes[j] = affix;
                    affixids[j] = affixLexicon.getIdx(affix);
                }
                double ssum = 0;
                for (int j = 0; j < topicK; ++j) {
                    double tsum = 0;
                    for (int k = 0; k < wlength + 1; ++k) {
                        double stemProb =
                                stemAffixTopicHDP.prob(j, affixids[k], stems[k]);
                        double affixProb = 0;
                        if (affixids[k] == -1) {
                            affixProb =
                                    nonexistentStateAffixProbs[affixes[k].length()];
                        } else {
                            affixProb = affixStateDP.prob(j, affixes[k]);
                        }
                        tsum += stemProb * affixProb;
                    }
                    TopicByWordProbs[wordtopicoff + j] = tsum;
                    ssum += tsum * topicProbs[j];
                }
                StateByWordProbs[wordid * stateS + i] = ssum;
            }
        }

        TopWordsPerState = new StringDoublePair[stateS][];
        for (int i = 1; i < stateS; ++i) {
            TopWordsPerState[i] = new StringDoublePair[outputPerClass];
        }

        sum = 0.;
        for (int i = 1; i < topicSubStates; ++i) {
            sum += stateProbs[i] = stateCounts[i] + wbeta;
            ArrayList<DoubleStringPair> topWords =
                    new ArrayList<DoubleStringPair>();
            for (int j = 0; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                        StateByWordProbs[j * stateS + i], idxToWord.get(
                        j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                TopWordsPerState[i][j] =
                        new StringDoublePair(
                        topWords.get(j).stringValue,
                        topWords.get(j).doubleValue / stateProbs[i]);
            }
        }

        for (int i = topicSubStates; i < stateS; ++i) {
            sum += stateProbs[i] = stateCounts[i] + wbeta;
            ArrayList<DoubleStringPair> topWords =
                    new ArrayList<DoubleStringPair>();
            for (int j = 0; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                        StateByWord[j * stateS + i] + beta, idxToWord.get(
                        j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                TopWordsPerState[i][j] =
                        new StringDoublePair(
                        topWords.get(j).stringValue,
                        topWords.get(j).doubleValue / stateProbs[i]);
            }
        }

        for (int i = 1; i < stateS; ++i) {
            stateProbs[i] /= sum;
        }

        TopWordsPerTopic = new StringDoublePair[topicK][];
        for (int i = 0; i < topicK; ++i) {
            TopWordsPerTopic[i] = new StringDoublePair[outputPerClass];
        }
        for (int i = 0; i < topicK; ++i) {
            ArrayList<DoubleStringPair> topWords =
                    new ArrayList<DoubleStringPair>();
            for (int j = 0; j < wordW; ++j) {
                topWords.add(new DoubleStringPair(
                        TopicByWordProbs[j * topicK + i],
                        idxToWord.get(j)));
            }
            Collections.sort(topWords);
            for (int j = 0; j < outputPerClass; ++j) {
                try {
                    TopWordsPerTopic[i][j] =
                            new StringDoublePair(
                            topWords.get(j).stringValue,
                            topWords.get(j).doubleValue);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    /**
//     * Print text that has been segmented/tagged in a sample to output.
//     *
//     * @param outDir Root of path to generate output to
//     * @throws IOException
//     */
//    @Override
//    public void printAnnotatedText(String outDir) throws IOException {
//    }
}
