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
package tikka.structures.distributions;

import tikka.structures.DoubleStringPair;
import tikka.structures.StringDoublePair;
import tikka.structures.lexicons.Lexicon;

import tikka.structures.lexicons.ThreeDimLexicon;
import tikka.structures.lexicons.ThreeDimProbLexicon;
import tikka.structures.lexicons.TwoDimProbLexicon;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The class of the hierarchical Dirichlet process for stems over topics.
 * The counts are maintained in {@link #clsMorphCounts}.
 *
 * @author tsmoon
 */
public class StemStateDP extends ThreeDimDirichletProcess {

    /**
     * Alias for {@link #clsMorphCounts}.
     */
    protected ThreeDimLexicon clsStemCounts;
    /**
     * Alias for {@link #clsMorphProbs}.
     */
    protected ThreeDimProbLexicon clsStemProbs;
    /**
     * 2D array of {@link Infltrait.structures.StringDoublePair}. Defined
     * for {@code C} states and {@code N} affixes which have {@code N} highest
     * likelihoods in the given state. Allocated and populated in
     * {@link #normalize(int)}.
     */
    protected StringDoublePair[][] TopStemsPerState;

    /**
     * 
     * @param baseDistribution
     * @param lexicon
     * @param hyper
     */
    public StemStateDP(DirichletBaseDistribution baseDistribution,
            Lexicon lexicon, double hyper) {
        super(baseDistribution, lexicon, hyper);
        clsStemCounts = clsMorphCounts;
        clsStemProbs = clsMorphProbs;
    }

    /**
     * Normalize sample counts.
     *
     * @param stateS    Total number of states. This excludes the first
     *                  state 0, which is the sentence boundary.
     * @param outputPerState    How many affixes to print per state in
     *                  the output.
     * @param stateProbs    Array of probabilties for each state. The first
     *                  cell should be ignored.
     */
    @Override
    public void normalize(int topicS, int stateS, int outputPerState,
            double[] stateProbs) {

        double sum = 0;
        for (int i = 1; i < topicS; ++i) {
            sum += stateProbs[i] = getCumCount(i) + hyper;
        }
        for (int i = 1; i < topicS; ++i) {
            stateProbs[i] /= sum;
        }

        setNonexistentStateAffixProbs(stateProbs, 1, topicS);

        sum = 0;
        for (int i = topicS; i < stateS; ++i) {
            sum += stateProbs[i] = getCumCount(i) + hyper;
        }
        for (int i = topicS; i < stateS; ++i) {
            stateProbs[i] /= sum;
        }

        setNonexistentTopicStateAffixProbs(stateProbs, topicS, stateS);

        int maxid = 0;
        for (int affixid : lexicon.keySet()) {
            if (affixid > maxid) {
                maxid = affixid;
            }
        }
        maxid++;

        affixTopicStateProbs = new double[maxid];
        for (int i = 0; i < maxid; ++i) {
            affixTopicStateProbs[i] = 0;
        }

        TopStemsPerState = new StringDoublePair[stateS][];
        for (int i = 1; i < stateS; ++i) {
            TopStemsPerState[i] = new StringDoublePair[outputPerState];
        }

        for (int i = 1; i < topicS; ++i) {
//            TwoDimProbLexicon affixProbLexicon = new TwoDimProbLexicon();
//            clsStemProbs.put(i, affixProbLexicon);

            ArrayList<DoubleStringPair> topAffixes =
                    new ArrayList<DoubleStringPair>();
            for (int affixid : clsStemCounts.get(i).keySet()) {
//                try {
                double p = prob(i, affixid);
//                    affixProbLexicon.put(affixid, p);
                topAffixes.add(new DoubleStringPair(p, lexicon.getString(
                        affixid)));
                double val = p * stateProbs[i];
                affixTopicStateProbs[affixid] += val;
//                } catch (ArrayIndexOutOfBoundsException e) {
//                    e.printStackTrace();
//                }
            }
            Collections.sort(topAffixes);
            for (int j = 0; j < outputPerState; ++j) {
                try {
                    TopStemsPerState[i][j] = new StringDoublePair(
                            topAffixes.get(j).stringValue,
                            topAffixes.get(j).doubleValue);
                } catch (IndexOutOfBoundsException e) {
//                    e.printStackTrace();
                }
            }
        }

        for (int i = topicS; i < stateS; ++i) {
            TwoDimProbLexicon affixProbLexicon = new TwoDimProbLexicon();
            clsStemProbs.put(i, affixProbLexicon);

            ArrayList<DoubleStringPair> topAffixes =
                    new ArrayList<DoubleStringPair>();
            for (int affixid : clsStemCounts.get(i).keySet()) {
//                try {
                double p = prob(i, affixid);
                affixProbLexicon.put(affixid, p);
                topAffixes.add(new DoubleStringPair(p, lexicon.getString(
                        affixid)));
//                } catch (ArrayIndexOutOfBoundsException e) {
//                    e.printStackTrace();
//                }
            }
            Collections.sort(topAffixes);
            for (int j = 0; j < outputPerState; ++j) {
                try {
                    TopStemsPerState[i][j] = new StringDoublePair(
                            topAffixes.get(j).stringValue,
                            topAffixes.get(j).doubleValue);
                } catch (IndexOutOfBoundsException e) {
//                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Print normalized probability tables for affixes by topic.
     * 
     * @param stateS    Total number of states. This excludes the first
     *                  state 0, which is the sentence boundary.
     * @param outputPerState    How many affixes to print per state in
     *                  the output.
     * @param stateProbs Array of probabilties for each state. The first
     *                  cell should be ignored.
     * @param out   Destination of output
     */
    @Override
    public void print(int topicS, int stateS, int outputPerState,
            double[] stateProbs,
            BufferedWriter out) throws IOException {
        int startt = topicS, M = 4, endt = M;

        out.write("***** Affix Probabilities by State *****\n\n");
        while (startt < stateS) {
            for (int i = startt; i < endt; ++i) {
                String header = "State_" + i;
                header = String.format("%25s\t%6.5f\t", header,
                        stateProbs[i]);
                out.write(header);
            }

            out.newLine();
            out.newLine();

            for (int i = 0; i < outputPerState; ++i) {
                for (int c = startt; c < endt; ++c) {
                    String line = String.format("%25s\t%7s\t", "", "");
                    try {
                        line = String.format("%25s\t%6.5f\t",
                                TopStemsPerState[c][i].stringValue,
                                TopStemsPerState[c][i].doubleValue);
                    } catch (NullPointerException e) {
//                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    out.write(line);
                }
                out.newLine();
            }

            out.newLine();
            out.newLine();

            startt = endt;
            endt = java.lang.Math.min(stateS, startt + M);
        }
    }
}