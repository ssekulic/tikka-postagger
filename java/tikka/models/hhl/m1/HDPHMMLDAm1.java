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

import tikka.apps.CommandLineOptions;

import tikka.exceptions.EmptyCountException;

import tikka.models.hhl.HDPHMMLDA;

import tikka.opennlp.io.DirReader;
import tikka.opennlp.io.DirWriter;

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

import tikka.utils.ec.util.MersenneTwisterFast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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

//    /**
//     *
//     */
//    public HDPHMMLDAm1() {
//    }
    /**
     * Initialize the distributions that will be used in this model.
     */
    @Override
    protected void initalizeDistributions() {
        /**
         * Note the hyperparameter being passed to the HDP base. it is not
         * muStemBase but wbeta*10. This is to prevent the model from becoming
         * degenerate in the normalization process as well as to make it more
         * consistent with models that do not account for morphology
         */
        stemTopicHierarchicalBaseDistribution =
              new HierarchicalDirichletBaseDistribution(stemLexicon,
              stemBoundaryProb, wbeta * 10);

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

        /**
         * Note the hyperparameter being passed to the DP base. it is not
         * muStem but wgamma. This is to prevent the model from becoming
         * degenerate in the normalization process as well as to make it more
         * consistent with models that do not account for morphology
         */
        dirichletStemBaseDistribution = new DirichletBaseDistribution(
              stemLexicon, stemBoundaryProb, wgamma) {

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
              stemTopicHierarchicalBaseDistribution, stemLexicon, wbeta);
        stemAffixStateDP = new StemAffixStateDP(
              dirichletStemBaseDistribution, stemLexicon, wgamma);
        affixStateDP = new AffixStateDP(dirichletAffixBaseDistribution,
              affixLexicon, muAffix);
    }

    /**
     * Initializes from a pretrained, loaded model. Use this if the model has
     * been loaded from a pretrained model.
     */
    @Override
    public void initializeFromLoadedModel(CommandLineOptions options) {
        super.initializeFromLoadedModel(options);

        initalizeDistributions();

        int current = 0, prev = 0, pprev = 0;
        int wordid = 0, docid = 0, topicid = 0, stateid = 0, splitid = 0, stemid =
              0, affixid = 0;
        int docoff, wordstateoff, wordtopicoff;
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

                docid = documentVector[i];
                topicid = topicVector[i];
                stateid = stateVector[i];
                splitid = splitVector[i];

                docoff = topicK * docid;
                wordstateoff = wordid * stateS;
                wordtopicoff = wordid * topicK;

                word = trainIdxToWord.get(wordid);

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
                }

                affixStateDP.inc(stateid, affixid);
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
        int wordid = 0, docid = 0, topicid = 0, stateid = 0, splitid = 0,
              stemid = 0, affixid = 0;
        int current = 0, prev = 0, pprev = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int docoff, wordstateoff, wordtopicoff, thirdstateoff, secondstateoff;
        int wlength = 0;
        String word = "", stem = "", affix = "";

        double[] splitProbs = new double[MAXLEN];

        /**
         * Initialize by assigning random topic indices to words
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

                docid = documentVector[i];
                docoff = topicK * docid;
                wordstateoff = wordid * stateS;
                wordtopicoff = wordid * topicK;

                if (mtfRand.nextDouble() > 0.5) {
                    stateid = stateVector[i] = mtfRand.nextInt(
                          topicSubStates - 1) + 1;
                } else {
                    stateid = stateVector[i] = topicSubStates;
                }

                totalprob = 0;
                try {
                    for (int j = 0;; ++j) {
                        topicProbs[j] = DocumentByTopic[docoff + j]
                              + alpha;
                        if (stateVector[i] < topicSubStates) {
                            topicProbs[j] *=
                                  (TopicByWord[wordtopicoff + j] + beta)
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

                thirdstateoff = pprev * S3 + prev * S2 + current * stateS;
                secondstateoff = prev * S2 + current * stateS;
                totalprob = 0;
                if (stateVector[i] == topicSubStates) {
                    double topicprob = (TopicByWord[wordtopicoff + topicid] + beta)
                          / (topicCounts[topicid] + wbeta);
                    for (int j = 1; j < topicSubStates; ++j) {
                        totalprob +=
                              stateProbs[j] =
                              topicprob
                              * (thirdOrderTransitions[thirdstateoff + j] + psi);
                    }
                    try {
                        for (int j = topicSubStates;; j++) {
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
                }

                word = trainIdxToWord.get(wordid);
                wlength = word.length();
                totalprob = 0;
                for (int j = 0; j < wlength + 1; ++j) {
                    stem = word.substring(0, j);
                    affix = word.substring(j, wlength);
                    stemid = stemLexicon.getIdx(stem);
                    affixid = affixLexicon.getIdx(affix);

                    if (stateid < topicSubStates) {
                        totalprob += splitProbs[j] = stemAffixTopicHDP.prob(
                              topicid, affixid, stem)
                              * affixStateDP.probNumerator(stateid, affix);
                    } else {
                        totalprob += splitProbs[j] = stemAffixStateDP.prob(
                              stateid, affixid, stem)
                              * affixStateDP.probNumerator(stateid, affix);
                    }
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

                if (stateVector[i] < topicSubStates) {
                    stemAffixTopicHDP.inc(topicid, affixid, stemid);
                    TopicByWord[wordtopicoff + topicid]++;
                    DocumentByTopic[docoff + topicid]++;
                    topicCounts[topicid]++;
                } else {
                    stemAffixStateDP.inc(stateid, affixid, stemid);
                }
                affixStateDP.inc(stateid, affixid);
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
        int wordid = 0, docid = 0, topicid = 0, stateid = 0, splitid = 0,
              stemid = 0, affixid = 0;
        int current = 0, prev = 0, pprev = 0, next = 0,
              nnext = 0, nnnext = 0;
        double max = 0, totalprob = 0;
        double r = 0;
        int docoff, wordstateoff, wordtopicoff, thirdstateoff, secondstateoff;
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
                    docid = documentVector[i];
                    stateid = stateVector[i];
                    topicid = topicVector[i];
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
                    }
                    affixStateDP.dec(stateid, affixid);
                    stateCounts[stateid]--;
                    StateByWord[wordstateoff + stateid]--;
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
                                totalprob = 0;
                                for (int k = 0; k < splitmax; ++k) {
                                    totalprob += stemAffixTopicHDP.prob(j, affixidxes[k], stems[k])
                                          * affixStateDP.probNumerator(stateid, affixes[k]);
                                }
                                topicProbs[j] *= totalprob;
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
                    thirdstateoff = pprev * S3 + prev * S2 + current * stateS;
                    secondstateoff = prev * S2 + current * stateS;

                    try {
                        for (int j = 1; j < topicSubStates; ++j) {
                            totalprob = 0;
                            for (int k = 0; k < splitmax; ++k) {
                                totalprob += stemAffixTopicHDP.prob(topicid, affixidxes[k], stems[k])
                                      * affixStateDP.prob(j, affixes[k]);
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
                        for (int j = topicSubStates;; ++j) {
                            totalprob = 0;
                            for (int k = 0; k < splitmax; ++k) {
                                totalprob += stemAffixStateDP.prob(stateid, affixidxes[k], stems[k])
                                      * affixStateDP.prob(j, affixes[k]);
                            }
                            stateProbs[j] = totalprob
                                  //                                    ((StateByWord[wordstateoff + j] + beta) / (stateCounts[j] + wbeta)) *
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
                        if (stateid < topicSubStates) {
                            splitProbs[j] = stemAffixTopicHDP.prob(topicid,
                                  affixidxes[j], stems[j])
                                  * affixStateDP.probNumerator(stateid,
                                  affixes[j]);
                        } else {
                            splitProbs[j] = stemAffixStateDP.prob(stateid,
                                  affixidxes[j], stems[j])
                                  * affixStateDP.probNumerator(stateid,
                                  affixes[j]);
                        }
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

        double[] nonexistentStateAffixProbs = affixStateDP.getNonexistentStateAffixProbs();
        for (int wordid = 0; wordid < wordW; ++wordid) {
            String word = trainIdxToWord.get(wordid);
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
                      StateByWordProbs[j * stateS + i], trainIdxToWord.get(
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
                      StateByWord[j * stateS + i] + beta, trainIdxToWord.get(
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
                      trainIdxToWord.get(j)));
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
        int wordid = 0, docid = 0, topicid = 0, stateid = 0;
        double totalprob = 0;
        int sampleoff = outiter * wordW;
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
                docid = documentVector[i];
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
                if (stateid < topicSubStates) {
                    for (int k = 0; k < splitmax; ++k) {
                        totalprob += stemAffixTopicHDP.prob(topicid, affixidxes[k], stems[k])
                              * affixStateDP.prob(stateid, affixes[k]);
                    }
                } else {
                    for (int k = 0; k < splitmax; ++k) {
                        totalprob += stemAffixStateDP.prob(stateid, affixidxes[k], stems[k])
                              * affixStateDP.prob(stateid, affixes[k]);
                    }
                }
                SampleProbs[sampleoff + i] = totalprob;
            }
        }
    }


    /**
     * Set the arrays testWordTopicProbs and testWordStateProbs
     */
    @Override
    protected void setWordClassProbArrays() {
        double totalprob = 0;
        int wordstateoff, wordtopicoff;
        String word = "";
        int wlength = 0, splitmax = 0;
        String[] stems = new String[MAXLEN], affixes = new String[MAXLEN];
        int[] stemidxes = new int[MAXLEN], affixidxes = new int[MAXLEN];

        /**
         * Assign probabilities to each word given either topic or state.
         * This will not change in the test run so we store this in table
         * at the beginning.
         */
        testWordTopicProbs = new double[wordW * topicK];
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
            wordtopicoff = i * topicK;
            wordstateoff = i * stateS;
            for (int j = 0; j < topicK; ++j) {
                totalprob = 0;
                for (int l = 1; l < topicSubStates; ++l) {
                    for (int k = 0; k < splitmax; ++k) {
                        totalprob += stemAffixTopicHDP.prob(j, affixidxes[k], stems[k])
                              * affixStateDP.prob(l, affixes[k]);
                    }
                }
                testWordTopicProbs[wordtopicoff + j] = totalprob;
            }
            for (int j = 1; j < stateS; ++j) {
                totalprob = 0;
                for (int k = 0; k < splitmax; ++k) {
                    totalprob += stemAffixStateDP.prob(j, affixidxes[k], stems[k])
                          * affixStateDP.prob(j, affixes[k]);
                }
                testWordStateProbs[wordstateoff + j] = totalprob;
            }
        }
    }
}
